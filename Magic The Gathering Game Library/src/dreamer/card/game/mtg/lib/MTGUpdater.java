package dreamer.card.game.mtg.lib;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.*;
import com.reflexit.magiccards.core.storage.database.controller.CardSetJpaController;
import com.reflexit.magiccards.core.storage.database.controller.exceptions.NonexistentEntityException;
import dreamer.card.game.MTGGame;
import dreamer.card.game.core.UpdateRunnable;
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
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class MTGUpdater extends UpdateRunnable {

    private String currentSet = "";
    private final MTGRCPGame game;
    private static Pattern countPattern = Pattern.compile("Search:<span id=\"ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay\"><i>.*</i>  \\((\\d+)\\)</span>");
    private static Pattern lastPagePattern = Pattern.compile("\\Q<span style=\"visibility:hidden;\">&nbsp;&gt;</span></div>");
    private static Pattern spanPattern = Pattern.compile("class=[^>]*>(.*)</span>");
    private static Pattern divPattern = Pattern.compile("class=[^>]*>(.*?)</div>");
    private static Pattern idPattern = Pattern.compile("href=.*/Card/Details.aspx\\?multiverseid=(\\d+)");
    private static Pattern setPattern = Pattern.compile("title=\"(.*) \\((.*)\\)\" src=.*set=(\\w+)");
    private static Pattern namePattern = Pattern.compile(".*>(.*)</a></span>");
    private static Pattern powPattern = Pattern.compile("\\((\\d+/)?(\\d+)\\)");
    private static String LONG_MINUS;
    private final static Charset UTF_8 = Charset.forName("utf-8");
    private final static Map manaMap = new LinkedHashMap();
    private static final Logger LOG = Logger.getLogger(MTGUpdater.class.getName());
    protected final String source = "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=standard&set=%5b%22";
    private CardSet set;
    private boolean dbError = false;
    private ArrayList<Card> newCards = new ArrayList<Card>();

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

    MTGUpdater(final MTGRCPGame game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            //Get the sets
            updateProgressMessage("Gathering stats before start processing...");
            ParseGathererSets parser = new ParseGathererSets();
            parser.load();
            Collection<Editions.Edition> editions = Editions.getInstance().getEditions();
            HashMap parameters = new HashMap();
            parameters.put("name", game.getName());
            Game mtg = null;
            try {
                mtg = (Game) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Game.findByName", parameters).get(0);
            } catch (DBException ex) {
                LOG.log(Level.SEVERE, null, ex);
                dbError = true;
            }
            ArrayList<SetUpdateData> data = new ArrayList<SetUpdateData>();
            boolean needUpdate = false;
            List temp = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findAll");
            LOG.log(Level.INFO, "{0} sets found in database:", temp.size());
            int i = 0;
            for (Iterator it = temp.iterator(); it.hasNext();) {
                CardSet cs = (CardSet) it.next();
                LOG.log(Level.INFO, (++i + " " + cs.getGameName()));
            }
            //Chek to see if there's something new to update.
            for (Iterator iterator = editions.iterator(); iterator.hasNext();) {
                Editions.Edition edition = (Editions.Edition) iterator.next();
                parameters.clear();
                parameters.put("name", edition.getName());
                temp = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", parameters);
                if (temp.isEmpty()) {
                    needUpdate = true;
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            "Update was found, please wait!", NotifyDescriptor.WARNING_MESSAGE));
                    break;
                }
            }
            if (needUpdate) {
                updateProgressMessage("Calculating amount of pages to process...");
                try {
                    for (Iterator iterator = editions.iterator(); iterator.hasNext();) {
                        if (!dbError) {
                            Editions.Edition edition = (Editions.Edition) iterator.next();
                            String from = source + edition.getName().replaceAll(" ", "+") + "%22%5d";
                            int urlAmount = checkAmountOfPagesForSet(from);
                            parameters.clear();
                            parameters.put("name", edition.getName());
                            List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", parameters);
                            long amount = 0;
                            if (!result.isEmpty()) {
                                set = (CardSet) result.get(0);
                                parameters.clear();
                                parameters.put("gameId", set.getCardSetPK().getGameId());
                                amount = (Long) Lookup.getDefault().lookup(IDataBaseCardStorage.class).createdQuery("SELECT count(c) FROM CardSet c WHERE c.cardSetPK.gameId = :gameId", parameters).get(0);
                            }
                            if (result.isEmpty() || amount < urlAmount) {
                                LOG.log(Level.INFO, "Adding set: {0} to processing list", edition.getName());
                                data.add(new SetUpdateData(edition.getName(), from, edition, urlAmount - amount));
                            } else {
                                LOG.log(Level.INFO, "Skipping processing of set: {0}", edition.getName());
                            }
                        } else {
                            break;
                        }
                    }
                } catch (DBException e) {
                    LOG.log(Level.SEVERE, null, e);
                    dbError = true;
                }
                int totalPages = 0;
                for (Iterator<SetUpdateData> it = data.iterator(); it.hasNext();) {
                    SetUpdateData setData = it.next();
                    totalPages += setData.getPagesInSet();
                }
                LOG.log(Level.INFO, "Pages to update: {0}", totalPages);
                setSize(totalPages);
                //Update card cache
                MTGCardCache.setCachingEnabled(true);
                MTGCardCache.setLoadingEnabled(true);
                File cacheDir = new File(System.getProperty("user.home")
                        + System.getProperty("file.separator")
                        + ".Deck Manager"
                        + System.getProperty("file.separator") + "cache");
                //Create game cache dir
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                MTGCardCache.setCacheDir(cacheDir);
                LOG.log(Level.INFO, "Updating cache at: {0}", cacheDir.getAbsolutePath());
                for (Iterator<SetUpdateData> it = data.iterator(); it.hasNext();) {
                    if (!dbError) {
                        SetUpdateData setData = it.next();
                        Edition edition = setData.getEdition();
                        set = (CardSet) Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCardSet(mtg,
                                edition.getName(), edition.getMainAbbreviation(), edition.getReleaseDate());
                        LOG.log(Level.INFO, "Created set: {0}", setData.getName());
                        String mess = "Updating set: " + setData.getName();
                        LOG.log(Level.INFO, mess);
                        updateProgressMessage(mess);
                        setCurrentSet(setData.getName());
                        createCardsForSet(setData.getUrl());
                    } else {
                        break;
                    }
                }
                //Add the cards just added to the image queue
                for (Iterator it = newCards.iterator(); it.hasNext();) {
                    Card card = (Card) it.next();
                    MTGCardCache.getCardImageQueue().add(card);
                }
            }
            reportDone();
        } catch (DBException ex) {
            LOG.log(Level.SEVERE, null, ex);
            dbError = true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public String getActionName() {
        return "Updating " + game.getName() + " game...";
    }

    /**
     * Create pages from set url
     *
     * @param from URL containing pages for the set
     * @throws MalformedURLException
     * @throws IOException
     */
    protected void createCardsForSet(String from) throws MalformedURLException, IOException {
        LOG.log(Level.INFO, "Retrieving from url: {0}", from);
        int i = 0;
        boolean lastPage = false;
        while (lastPage == false) {
            try {
                URL url = new URL(from + "&page=" + i);
                lastPage = loadUrl(url);
                i++;
            } catch (NonexistentEntityException ex) {
                Exceptions.printStackTrace(ex);
                break;
            }
        }
    }

    private boolean loadUrl(URL url) throws IOException, NonexistentEntityException {
        InputStream openStream = url.openStream();
        BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
        boolean res = processFile(st);
        st.close();
        return res;
    }

    private int checkAmountOfPagesForSet(String from) throws IOException {
        URL url = new URL(from);
        InputStream openStream = url.openStream();
        BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
        int amount = getAmountOfPagesForSet(st);
        st.close();
        return amount;
    }

    private int getAmountOfPagesForSet(BufferedReader st) throws IOException {
        String line;
        int amount = 0;
        while ((line = st.readLine()) != null) {
            Matcher cm = countPattern.matcher(line);
            if (cm.find()) {
                amount = Integer.parseInt(cm.group(1));
                break;
            }
        }
        return amount;
    }

    private boolean processFile(BufferedReader st) throws IOException, NonexistentEntityException {
        String line = "";
        int state = 0;
        boolean lastPage = false;
        boolean cards = false;
        while ((state == 0 && (line = st.readLine()) != null) || (state == 1)) {
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
                } catch (DBException ex) {
                    Logger.getLogger(MTGGame.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IOException(ex);
                }
            }
            state = 0;
        }
        if (cards == false) {
            try {
                LOG.log(Level.SEVERE, "Unable to retrieve pages from set: {0}", getCurrentSet());
                //Delete it from database
                HashMap properties = new HashMap();
                properties.put("name", getCurrentSet());
                List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", properties);
                if (!result.isEmpty()) {
                    CardSet temp = (CardSet) result.get(0);
                    CardSetJpaController controller = new CardSetJpaController(((DataBaseCardStorage) Lookup.getDefault().lookup(IDataBaseCardStorage.class)).getEntityManagerFactory());
                    controller.destroy(temp.getCardSetPK());
                }
                return true;
            } catch (DBException ex) {
                Exceptions.printStackTrace(ex);
                return true;
            }
        }
        return lastPage;
    }

    private void parseRecord(String line) throws DBException {
        try {
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
            for (String temp : sets) {
                String edition = getMatch(setPattern, temp, 1);
                String rarity = getMatch(setPattern, temp, 2);
                String setId = getMatch(idPattern, temp, 1);
                if (edition.length() <= 1) {
                    continue;
                }
                edition = edition.trim();
                HashMap parameters = new HashMap();
                List result;
                CardType ct;
                Card c;
                if (id.equals(setId)) {
                    card.setSet(edition);
                    card.setRarity(rarity.trim());
                    //Handle card type, it might be new
                    parameters.put("name", type);
                    try {
                        result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardType.findByName", parameters);
                        if (result.isEmpty()) {
                            ct = (CardType) Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCardType(type);
                        } else {
                            ct = (CardType) result.get(0);
                        }
                    } catch (DBException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                        return;
                    }
                    //Create the card
                    try {
                        c = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCard(ct, card.getName(), card.getOracleText() == null ? "".getBytes() : card.getOracleText().getBytes());
                        newCards.add(c);
                        LOG.log(Level.INFO, "Created card: {0}", c.getName());
                        Lookup.getDefault().lookup(IDataBaseCardStorage.class).addCardToSet(c, set);
                    } catch (DBException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                        return;
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
                        attributes.put("CardId", "" + card.getCardId());
                        Lookup.getDefault().lookup(IDataBaseCardStorage.class).addAttributesToCard(c, attributes);
                    } catch (DBException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                        return;
                    }
                    //Add it to the set
                } else {
                    // other printings
                    MagicCard card2 = (MagicCard) card.clone();
                    if (card2 != null) {
                        card2.setId(setId);
                        card2.setSet(edition);
                        card2.setRarity(rarity.trim());
                        throw new DBException("Is this used?");
                    }
                }
            }
            increaseProgress();
        } catch (IllegalStateException e) {
            LOG.log(Level.WARNING, null, e);
        }
    }

    private String getMatch(Pattern textPattern, String typeF) {
        return getMatch(textPattern, typeF, 1);
    }

    private String getMatch(Pattern textPattern, String line, int g) {
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

    private String fixText(String str1) {
        String str = str1;
        str = str.replaceAll("</p><p>", "<br>");
        str = str.replaceFirst("<p>", "");
        str = str.replaceFirst("</p>", "");
        return str;
    }

    public String htmlToString(String str) {
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

    /**
     * @return the currentSet
     */
    public String getCurrentSet() {
        return currentSet;
    }

    /**
     * @param currentSet the currentSet to set
     */
    public void setCurrentSet(String currentSet) {
        this.currentSet = currentSet;
    }
}
