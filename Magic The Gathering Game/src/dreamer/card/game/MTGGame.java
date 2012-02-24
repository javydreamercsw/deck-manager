package dreamer.card.game;

import dreamer.card.game.storage.IDataBaseManager;
import dreamer.card.game.storage.database.persistence.Card;
import dreamer.card.game.storage.database.persistence.CardType;
import dreamer.card.game.storage.database.persistence.Game;
import dreamer.card.game.storage.database.persistence.controller.exceptions.PreexistingEntityException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mtg.card.MagicCard;
import mtg.card.sync.ParseGathererSets;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = ICardGame.class)
public class MTGGame extends DefaultCardGame {

    private final static Charset UTF_8 = Charset.forName("utf-8");
    private static Pattern countPattern = Pattern.compile("Search:<span id=\"ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay\"><i>.*</i>  \\((\\d+)\\)</span>");
    private static Pattern lastPagePattern = Pattern.compile("\\Q<span style=\"visibility:hidden;\">&nbsp;&gt;</span></div>");
    private static int countCards;
    private static Pattern spanPattern = Pattern.compile("class=[^>]*>(.*)</span>");
    private static Pattern divPattern = Pattern.compile("class=[^>]*>(.*?)</div>");
    private static Pattern idPattern = Pattern.compile("href=.*/Card/Details.aspx\\?multiverseid=(\\d+)");
    private static Pattern setPattern = Pattern.compile("title=\"(.*) \\((.*)\\)\" src=.*set=(\\w+)");
    private static Pattern namePattern = Pattern.compile(".*>(.*)</a></span>");
    private static Pattern powPattern = Pattern.compile("\\((\\d+/)?(\\d+)\\)");
    private static String LONG_MINUS;
    final static Map manaMap = new LinkedHashMap();
    private static final Logger LOG = Logger.getLogger(MTGGame.class.getName());

    static {
        manaMap.put("\\Q{500}", "{0.5}");
        manaMap.put("\\{(\\d)([BUGRW])\\}", "{$1/$2}");
        manaMap.put("\\{([BUGRW])([BUGRW])\\}", "{$1/$2}");
        manaMap.put("\\Q{tap}", "{T}");
        manaMap.put("\\Q{untap}", "{Q}");
    }

    static {
        try {
            LONG_MINUS = new String(new byte[]{(byte) 0xe2, (byte) 0x80, (byte) 0x94}, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @Override
    public String getName() {
        return "Magic the Gathering";
    }

    @Override
    public void updateDatabase() {
        try {
            String source = "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=standard&set=%5b%22";
            //Get the sets
            ParseGathererSets parser = new ParseGathererSets();
            parser.load();
            Collection<Editions.Edition> editions = Editions.getInstance().getEditions();
            HashMap parameters = new HashMap();
            parameters.put("name", getName());
            Game mtg = (Game) Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("Game.findByName", parameters).get(0);
            for (Iterator iterator = editions.iterator(); iterator.hasNext();) {
                Editions.Edition edition = (Editions.Edition) iterator.next();
                Lookup.getDefault().lookup(IDataBaseManager.class).createCardSet(
                        mtg, edition.getName(), edition.getMainAbbreviation(), edition.getReleaseDate());
                LOG.log(Level.INFO, "Created set: {0}", edition.getName());
                //Now create the cards for the set
                String url = source + edition.getName().replaceAll(" ", "+") + "%22%5d";
                LOG.log(Level.FINE, "Retrieving from url: {0}", url);
                createCardsForSet(url);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create pages from set url
     *
     * @param from URL containing pages for the set
     */
    private void createCardsForSet(String from) throws MalformedURLException, IOException {
        int i = 0;
        boolean lastPage = false;
        int totalPages = 0;
        while (lastPage == false) {
            URL url = new URL(from + "&page=" + i);
            lastPage = loadUrl(url);
            i++;
            totalPages += countCards;
        }
        LOG.log(Level.INFO, "Pages processed: {0}", totalPages);
    }

    private static boolean loadUrl(URL url) throws IOException {
        InputStream openStream = url.openStream();
        BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
        boolean res = processFile(st);
        st.close();
        return res;
    }

    private static boolean processFile(BufferedReader st) throws IOException {
        String line = "";
        int state = 0;
        boolean lastPage = false;
        boolean cards = false;
        while ((state == 0 && (line = st.readLine()) != null) || (state == 1)) {
            Matcher cm = countPattern.matcher(line);
            if (cm.find()) {
                countCards = Integer.parseInt(cm.group(1));
            }
            if (lastPagePattern.matcher(line).find()) {
                lastPage = true;
            }
            if (line.matches(".*class=\"cardItem .*")) {
                try {
                    String tr = "";
                    do {
                        if (line.matches(".*</tr>.*")) {
                            state = 1;
                            break;
                        }
                        tr += line + " ";
                    } while ((line = st.readLine()) != null);
                    parseRecord(tr);
                    cards = true;
                    continue;
                } catch (Exception ex) {
                    Logger.getLogger(MTGGame.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IOException(ex);
                }
            }
            state = 0;
        }
        if (cards == false) {
            throw new IOException("No results");
        }
        return lastPage;
    }

    private static void parseRecord(String line) throws Exception {
        MagicCard card = new MagicCard();
        // split by td
        String[] rows = line.split("<td");
        String[] fields = rows[2].split("<span|<div");
        String id = getMatch(idPattern, fields[3]);
        card.setId(id);
        card.setName(getMatch(namePattern, fields[3]));
        String cost = getMatch(spanPattern, fields[4]);
        card.setCost(cost);
        String type = getMatch(spanPattern, fields[6]);
        String powerCombo = type;
        String pow = getMatch(powPattern, powerCombo, 1).replaceFirst("/", "");
        String tou = getMatch(powPattern, powerCombo, 2);
        type = type.replaceAll("\\(.*", "").trim();
        card.setType(type);
        String text = fixText(getMatch(divPattern, fields[7]));
        card.setOracleText(text);
        card.setPower(pow);
        card.setToughness(tou);
        String[] sets = rows[3].split("<a onclick");
        for (String set : sets) {
            String edition = getMatch(setPattern, set, 1);
            String rarity = getMatch(setPattern, set, 2);
            String setId = getMatch(idPattern, set, 1);
            if (edition.length() <= 1) {
                continue;
            }
            edition = edition.trim();
            HashMap parameters = new HashMap();
            List result;
            CardType ct = null;
            Card c = null;
            if (id.equals(setId)) {
                card.setSet(edition);
                card.setRarity(rarity.trim());
                //Handle card type, it might be new
                parameters.put("name", type);
                try {
                    result = Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("CardType.findByName", parameters);
                    if (result.isEmpty()) {
                        ct = Lookup.getDefault().lookup(IDataBaseManager.class).createCardType(type);
                    } else {
                        ct = (CardType) result.get(0);
                    }
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                //Create the card
                try {
                    c = Lookup.getDefault().lookup(IDataBaseManager.class).createCard(ct,
                            card.getName(),
                            card.getOracleText() == null ? "".getBytes() : card.getOracleText().getBytes());
                } catch (PreexistingEntityException ex) {
                    //Do nothing
                    return;
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                //Add the card attributes
                try {
                    HashMap<String, String> attributes = new HashMap<String, String>();
                    attributes.put("Rarity", card.getRarity());
                    attributes.put("Cost", card.getCost());
                    attributes.put("Color Type", card.getColorType());
                    attributes.put("Language", card.getLanguage());
                    attributes.put("Part", card.getPart());
                    attributes.put("Power", card.getPower());
                    attributes.put("Rulings", card.getRulings());
                    attributes.put("Toughness", card.getToughness());
                    attributes.put("Type", card.getType());
                    Lookup.getDefault().lookup(IDataBaseManager.class).addAttributesToCard(c, attributes);
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                //Add it to the set
            } else {
                // other printings
                MagicCard card2 = (MagicCard) card.clone();
                if (card2 != null) {
                    card2.setId(setId);
                    card2.setSet(edition);
                    card2.setRarity(rarity.trim());
                    throw new Exception("Is this used?");
                }
            }
        }
    }

    private static String getMatch(Pattern textPattern, String typeF) {
        return getMatch(textPattern, typeF, 1);
    }

    private static String getMatch(Pattern textPattern, String line, int g) {
        Matcher matcher;
        matcher = textPattern.matcher(line);
        String text = "";
        if (matcher.find()) {
            text = matcher.group(g);
            if (text == null) {
                text = "";
            }
        }
        String res = htmlToString(text).trim();
        if (res.length() == 0) {
            res = " ";
        }
        return res;
    }

    private static String fixText(String str1) {
        String str = str1;
        str = str.replaceAll("</p><p>", "<br>");
        str = str.replaceFirst("<p>", "");
        str = str.replaceFirst("</p>", "");
        return str;
    }

    public static String htmlToString(String str) {
        str = str.replaceAll("\\Q " + LONG_MINUS, "-");
        str = str.replaceAll("&nbsp;", " ");
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&apos;", "'");
        str = str.replaceAll("&quot;", "\"");
        if (str.contains("img")) {
            str = str.replaceAll("<img [^<]*name=([^&]*)&[^>]*/>", "{$1}");
            for (Iterator iterator = manaMap.keySet().iterator(); iterator.hasNext();) {
                String alt = (String) iterator.next();
                String to = (String) manaMap.get(alt);
                str = str.replaceAll(alt, to);
            }
        }
        return str;
    }
}
