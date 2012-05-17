package dreamer.card.game.mtg.lib;

import com.reflexit.magiccards.core.cache.ICacheData;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.ICardType;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
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
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import mtg.card.MagicCard;
import mtg.card.sync.ParseGathererSets;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = DataBaseStateListener.class)
public class MTGUpdater extends UpdateRunnable implements DataBaseStateListener {

    private String currentSet = "";
    private static Pattern countPattern = Pattern.compile("Search:<span id=\"ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay\".*><i>.*</i>  \\((\\d+)\\)</span>");
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
    protected final String source = "http://gatherer.wizards.com/Pages/Search/Default.aspx?set=%5b%22";
    private boolean dbError = false;

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

    public MTGUpdater() {
        super(new MTGRCPGame());
    }

    @Override
    public void updateLocal() {
        //This section updates from the deployed database
        //Create game cache dir
        File cacheDir = Places.getCacheSubdirectory(".Deck Manager");
        //Check if database is present, if not copy the default database (to avoid long initial update that was 45 minutes long on my test)
        File dbDir = new File(cacheDir.getAbsolutePath()
                + System.getProperty("file.separator") + "data");
        dbDir.mkdirs();
        File db = InstalledFileLocator.getDefault().locate("card_manager.h2.db",
                "dreamer.card.game.mtg.lib", false);
        LOG.fine("Updating database...");
        EntityManagerFactory emf = null;
        try {
            if (db != null && db.exists()) {
                //Connect to the module's DB
                String dbName = db.getName();
                if (dbName.indexOf(".") > 0) {
                    dbName = dbName.substring(0, dbName.indexOf("."));
                }
                final Map<String, String> dbProperties = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getConnectionSettings();
                dbProperties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:file:"
                        + db.getParentFile().getAbsolutePath()
                        + System.getProperty("file.separator") + dbName + ";AUTO_SERVER=TRUE");
                dbProperties.put(PersistenceUnitProperties.TARGET_DATABASE, "org.eclipse.persistence.platform.database.H2Platform");
                dbProperties.put(PersistenceUnitProperties.JDBC_PASSWORD, "test");
                dbProperties.put(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
                dbProperties.put(PersistenceUnitProperties.JDBC_USER, "deck_manager");
                emf = Persistence.createEntityManagerFactory("Card_Game_InterfacePU",
                        dbProperties);
                HashMap parameters = new HashMap();
                parameters.put("name", getGame().getName());
                Game game = (Game) namedQuery("Game.findByName", parameters, emf).get(0);
                ParseGathererSets parser = new ParseGathererSets();
                try {
                    parser.load();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                if (game.getCardSetList().size() > Lookup.getDefault().lookup(IDataBaseCardStorage.class).getSetsForGame(game).size()) {
                    for (Iterator<CardSet> it = game.getCardSetList().iterator(); it.hasNext();) {
                        if (!dbError) {
                            ICardSet set = it.next();
                            LOG.log(Level.FINE, "Checkig set: {0}", set.getName());
                            if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardSetExists(set.getName())) {
                                Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCardSet(game, set.getName(),
                                        Editions.getInstance().getEditionByName(set.getName()).getMainAbbreviation(), new Date());
                            }
                            LOG.log(Level.FINE, "{0} cards to check!", set.getCards().size());
                            for (Iterator it2 = set.getCards().iterator(); it2.hasNext();) {
                                ICard card = (ICard) it2.next();
                                ICardType cardType = ((Card) card).getCardType();
                                if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardTypeExists(cardType.getName())) {
                                    cardType = Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCardType(cardType.getName());
                                    LOG.log(Level.FINE, "Added card type: {0}", cardType.getName());
                                } else {
                                    LOG.log(Level.FINE, "Card type: {0} already exists!", cardType.getName());
                                    parameters.clear();
                                    parameters.put("name", cardType.getName());
                                    cardType = (ICardType) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardType.findByName", parameters).get(0);
                                }
                                if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardExists(card.getName())) {
                                    List<CardHasCardAttribute> attributes = ((Card) card).getCardHasCardAttributeList();
                                    card = Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCard(cardType,
                                            card.getName(), ((Card) card).getText());
                                    LOG.log(Level.FINE, "Added card: {0}", card.getName());
                                    for (Iterator<CardHasCardAttribute> it3 = attributes.iterator(); it3.hasNext();) {
                                        CardHasCardAttribute attr = it3.next();
                                        if (Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardAttribute(card, attr.getCardAttribute().getName()) == null) {
                                            Lookup.getDefault().lookup(IDataBaseCardStorage.class).addAttributeToCard(card,
                                                    attr.getCardAttribute().getName(), attr.getValue());
                                            LOG.log(Level.FINE, "Added attribute: {0} with value: {1}!",
                                                    new Object[]{attr.getCardAttribute().getName(), attr.getValue()});
                                        }
                                    }
                                } else {
                                    parameters.clear();
                                    parameters.put("name", card.getName());
                                    card = (ICard) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findByName", parameters).get(0);
                                    LOG.log(Level.FINE, "Card: {0} already exists!", card.getName());
                                }
                                if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).setHasCard(set, card)) {
                                    parameters.clear();
                                    parameters.put("name", set.getName());
                                    CardSet temp = (CardSet) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", parameters).get(0);
                                    Lookup.getDefault().lookup(IDataBaseCardStorage.class).addCardToSet(card, temp);
                                    LOG.log(Level.FINE, "Added card: {0} to set {1}", new Object[]{card.getName(), temp.getName()});
                                } else {
                                    LOG.log(Level.FINE, "Card set: {0} already has card {1}",
                                            new Object[]{set.getName(), card.getName()});
                                }
                            }
                        }
                    }
                }
            }
        } catch (DBException ex) {
            LOG.log(Level.SEVERE, null, ex);
            dbError = true;
        } finally {
            //Close connections
            if (emf != null) {
                emf.close();
            }
        }
    }

    @Override
    public void updateRemote() {
        if (!dbError) {
            //Now update from the internet
            try {
                ParseGathererSets parser = new ParseGathererSets();
                parser.load();
                Collection<Editions.Edition> editions = Editions.getInstance().getEditions();
                ArrayList<SetUpdateData> data = new ArrayList<SetUpdateData>();
                //This section updates from the internet
                //Get the sets
                updateProgressMessage("Gathering stats before start processing...");
                HashMap parameters = new HashMap();
                parameters.put("name", getGame().getName());
                Game mtg = null;
                try {
                    mtg = (Game) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Game.findByName", parameters).get(0);
                } catch (DBException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                    dbError = true;
                }
                List temp = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findAll");
                LOG.log(Level.FINE, "{0} sets found in database:", temp.size());
                int i = 0;
                if (LOG.isLoggable(Level.FINE)) {
                    for (Iterator it = temp.iterator(); it.hasNext();) {
                        CardSet cs = (CardSet) it.next();
                        LOG.log(Level.FINE, (++i + " " + cs.getName()));
                    }
                }
                //Chek to see if there's something new to update.
                ArrayList<Editions.Edition> setsToLoad = new ArrayList<Editions.Edition>();
                for (Iterator iterator = editions.iterator(); iterator.hasNext();) {
                    Editions.Edition edition = (Editions.Edition) iterator.next();
                    parameters.clear();
                    parameters.put("name", edition.getName());
                    temp = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", parameters);
                    if (temp.isEmpty()) {
                        LOG.log(Level.WARNING, "Unable to find set: {0}", edition.getName());
                        setsToLoad.add(edition);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (!setsToLoad.isEmpty()) {
                    updateProgressMessage("Calculating amount of pages to process...");
                    try {
                        for (Iterator iterator = setsToLoad.iterator(); iterator.hasNext();) {
                            if (!dbError) {
                                Editions.Edition edition = (Editions.Edition) iterator.next();
                                String from = source + edition.getName().replaceAll(" ", "+") + "%22%5d";
                                int urlAmount = checkAmountOfPagesForSet(from);
                                parameters.clear();
                                parameters.put("name", edition.getName());
                                List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", parameters);
                                long amount = 0;
                                CardSet set;
                                if (!result.isEmpty()) {
                                    set = (CardSet) result.get(0);
                                    parameters.clear();
                                    parameters.put("gameId", set.getCardSetPK().getGameId());
                                    amount = (Long) Lookup.getDefault().lookup(IDataBaseCardStorage.class).createdQuery("SELECT count(c) FROM CardSet c WHERE c.cardSetPK.gameId = :gameId", parameters).get(0);
                                }
                                if (result.isEmpty() || amount < urlAmount) {
                                    LOG.log(Level.FINE, "Adding set: {0} to processing list because {1}.",
                                            new Object[]{edition.getName(), result.isEmpty()
                                                ? "it was not in the database" : "is missing pages in the database"});
                                    data.add(new SetUpdateData(edition.getName(), from, edition, urlAmount - amount));
                                } else {
                                    LOG.log(Level.FINE, "Skipping processing of set: {0}", edition.getName());
                                }
                            } else {
                                break;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
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
                    LOG.log(Level.FINE, "Pages to update: {0}", totalPages);
                    //Update card cache
                    if (totalPages > 0) {
                        setSize(totalPages);
                        //Create all sets first
                        for (Iterator<SetUpdateData> it = data.iterator(); it.hasNext();) {
                            if (!dbError) {
                                SetUpdateData setData = it.next();
                                Edition edition = setData.getEdition();
                                if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardSetExists(edition.getName())) {
                                    Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCardSet(mtg,
                                            edition.getName(), edition.getMainAbbreviation(), edition.getReleaseDate());
                                    LOG.log(Level.FINE, "Created set: {0}", edition.getName());
                                }
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        for (Iterator<SetUpdateData> it = data.iterator(); it.hasNext();) {
                            if (!dbError) {
                                SetUpdateData setData = it.next();
                                String mess = "Updating set: " + setData.getName();
                                LOG.log(Level.FINE, mess);
                                updateProgressMessage(mess);
                                setCurrentSet(setData.getName());
                                createCardsForSet(setData.getUrl());
                            } else {
                                break;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                    try {
                        if (!dbError) {
                            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findAll").iterator(); it.hasNext();) {
                                CardSet cs = (CardSet) it.next();
                                for (ICardCache cache : getGame().getCardCacheImplementations()) {
                                    cache.getSetIcon((ICardSet) cs);
                                }
                            }
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            } catch (DBException ex) {
                LOG.log(Level.SEVERE, null, ex);
                dbError = true;
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String getActionName() {
        return "Updating " + getGame().getName() + " game...";
    }

    /**
     * Create pages from set url
     *
     * @param from URL containing pages for the set
     * @throws MalformedURLException
     * @throws IOException
     */
    protected void createCardsForSet(String from) throws MalformedURLException, IOException {
        LOG.log(Level.FINE, "Retrieving from url: {0}", from);
        int i = 0;
        boolean lastPage = false;
        while (lastPage == false) {
            try {
                URL url = new URL(from + "&page=" + i);
                lastPage = loadUrl(url);
                i++;
            } catch (NonexistentEntityException ex) {
                LOG.log(Level.SEVERE, null, ex);
                dbError = true;
                break;
            }
        }
    }

    private boolean loadUrl(URL url) throws IOException, NonexistentEntityException {
        InputStream openStream;
        InputStreamReader is = null;
        BufferedReader st = null;
        boolean res = false;
        try {
            openStream = url.openStream();
            LOG.log(Level.FINE, "Loading from: {0}", url.toString());
            is = new InputStreamReader(openStream, UTF_8);
            st = new BufferedReader(is);
            res = processFile(st);
        } finally {
            if (st != null) {
                st.close();
            }
            if (is != null) {
                is.close();
            }
        }
        return res;
    }

    private int checkAmountOfPagesForSet(String from) throws IOException {
        InputStream openStream;
        InputStreamReader is = null;
        BufferedReader st = null;
        int amount = 0;
        try {
            URL url = new URL(from);
            openStream = url.openStream();
            is = new InputStreamReader(openStream, UTF_8);
            st = new BufferedReader(is);
            amount = getAmountOfPagesForSet(st);
        } finally {
            if (st != null) {
                st.close();
            }
            if (is != null) {
                is.close();
            }
        }
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
        while ((state == 0 && (line = st.readLine()) != null) || (state == 1) && !dbError) {
            if (lastPagePattern.matcher(line).find()) {
                lastPage = true;
            }
            if (line.matches(".*class=\"cardItem .*")) {
                try {
                    StringBuilder tr = new StringBuilder();
                    do {
                        if (line.matches(".*</tr>.*")) {
                            state = 1;
                            break;
                        }
                        tr.append(line).append(" ");
                    } while ((line = st.readLine()) != null);
                    parseRecord(tr.toString());
                    cards = true;
                    continue;
                } catch (DBException ex) {
                    Logger.getLogger(MTGGame.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IOException(ex);
                }
            }
            state = 0;
        }
        if (!cards) {
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
                LOG.log(Level.SEVERE, null, ex);
                dbError = true;
                return true;
            }
        }
        return lastPage;
    }

    private void parseRecord(String line) throws DBException {
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
            if (id.equals(setId)) {
                addCardToSet(card, edition, rarity, type);
            } else {
                if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardSetExists(edition)) {
                    LOG.log(Level.WARNING, "Is this a printing for card: {0} ID: {1} Set: {2}?",
                            new Object[]{card.getName(), id, edition});
                }
            }
        }
    }

    private Card addCardToSet(MagicCard card, String edition, String rarity, String type) throws DBException {
        Card c = null;
        if (!dbError) {
            HashMap parameters = new HashMap();
            List result;
            CardType ct;
            card.setSetName(edition);
            card.setRarity(rarity.trim());
            parameters.clear();
            parameters.put("name", card.getSetName());
            CardSet set = (CardSet) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", parameters).get(0);
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
                return null;
            }
            //Create the card
            if (Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardExists(card.getName())) {
                parameters.clear();
                parameters.put("name", card.getName());
                c = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findByName", parameters).get(0);
            } else {
                try {
                    c = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCard(ct, 
                            card.getName(), card.getOracleText() == null ? "".getBytes() : card.getOracleText().getBytes());
                    LOG.log(Level.FINE, "Created card: {0}", c.getName());
                } catch (DBException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
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
                //This only adds it if it doesn't exist
                Lookup.getDefault().lookup(IDataBaseCardStorage.class).addAttributesToCard(c, attributes);
                //Add the card to the set
                Lookup.getDefault().lookup(IDataBaseCardStorage.class).addCardToSet(c, set);
            } catch (DBException ex) {
                LOG.log(Level.SEVERE, null, ex);
                dbError = true;
                return null;
            }
            parameters.clear();
            parameters.put("name", card.getName());
            c = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findByName", parameters).get(0);
            //Add to caching list
            c.setSetName(edition);
            Lookup.getDefault().lookup(ICacheData.class).add(c);
            increaseProgress();
        }
        return c;
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
