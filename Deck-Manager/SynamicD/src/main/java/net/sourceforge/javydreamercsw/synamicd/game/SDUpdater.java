package net.sourceforge.javydreamercsw.synamicd.game;

import com.reflexit.magiccards.core.cache.ICacheData;
import com.reflexit.magiccards.core.model.CardFileUtils;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import dreamer.card.game.core.GameUpdater;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.Card;
import com.reflexit.magiccards.core.storage.database.CardSet;
import com.reflexit.magiccards.core.storage.database.CardType;
import com.reflexit.magiccards.core.storage.database.Game;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.javydreamercsw.synamicd.card.ISDCard;
import net.sourceforge.javydreamercsw.synamicd.card.SDCard;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = DataBaseStateListener.class)
public class SDUpdater extends GameUpdater implements DataBaseStateListener {

    private static final Logger LOG
            = Logger.getLogger(SDUpdater.class.getSimpleName());
    public static final String SD_URL_BASE = "http://sdrelaunched.com/pages.php";
    public static Charset UTF_8 = Charset.forName("utf-8");
    private static final Pattern editionStartPattern
            = Pattern.compile("<a class=\"slinks\" href=\".*?\">(.*?)</a>");
    private static final String editionURL
            = "http://sdrelaunched.com/pages.php?mset=%n&ptype=0&rtype=0";
    public static final String SS = "Super Star";
    private static final String MOVE = "Move";
    private static final String SPECIAL = "Special";
    private static final String MOMENTUM = "Momentum";

    public SDUpdater() {
        super(new SDGame());
    }

    @Override
    public void updateRemote() {
        if (!remoteUpdated) {
            synchronized (this) {
                if (!dbError) {
                    Game sd;
                    HashMap parameters = new HashMap();
                    parameters.put("name", getGame().getName());
                    try {
                        sd = (Game) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Game.findByName", parameters).get(0);
                        URL url = new URL(SD_URL_BASE);
                        InputStream openStream = url.openStream();
                        try (BufferedReader st
                                = new BufferedReader(new InputStreamReader(openStream, UTF_8))) {
                            String tempHtml = CardFileUtils.readFileAsString(st);
                            List<SDSet> sets = parse(tempHtml);
                            for (SDSet set : sets) {
                                ICardSet temp = null;
                                if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardSetExists(set.getName(), sd)) {
                                    LOG.log(Level.INFO, "Creating new Set: {0}", set.getName());
                                    temp = Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCardSet(sd, set.getName(), set.getName(), new Date());
                                } else {
                                    for (ICardSet x : sd.getCardSetList()) {
                                        if (x.getName().equals(set.getName())) {
                                            temp = x;
                                            break;
                                        }
                                    }
                                }
                                if (temp != null) {
                                    for (ISDCard card : set.getCards()) {
                                        card.setSetName(set.getName());
                                        String cardType;
                                        if (card.getHp() != null) {
                                            cardType = SS;
                                        } else if (card.getDamage() != null) {
                                            cardType = MOVE;
                                        } else if (card.getCost() != null) {
                                            cardType = SPECIAL;
                                        } else {
                                            cardType = MOMENTUM;
                                        }
                                        addCardToSet(card, temp.getName(), card.getRarity(), cardType);
                                    }
                                }
                            }
                        }
                    } catch (DBException | MalformedURLException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                        dbError = true;
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                        dbError = true;
                    }
                }
            }
            remoteUpdated = true;
        }
    }

    private Card addCardToSet(ISDCard card, String edition, String rarity, String type) throws DBException {
        Card c = null;
        if (!dbError) {
            HashMap parameters = new HashMap();
            List result;
            CardType ct;
            card.setSetName(edition);
            card.setRarity(rarity.trim());
            card.setCardType(type);
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
            Game sd;
            parameters.clear();
            parameters.put("name", getGame().getName());
            sd = (Game) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Game.findByName", parameters).get(0);
            ICardSet temp = null;
            for (ICardSet x : sd.getCardSetList()) {
                if (x.getName().equals(set.getName())) {
                    temp = x;
                    break;
                }
            }
            if (temp != null) {
                //Create the card
                if (Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardExists(card.getName(), temp)) {
                    parameters.clear();
                    parameters.put("name", card.getName());
                    c = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findByName", parameters).get(0);
                } else {
                    try {
                        parameters.clear();
                        parameters.put("name", card.getName());
                        result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findByName", parameters);
                        //Small work around duplicated card names.
                        c = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCard(ct,
                                card.getName() + (result.isEmpty() ? "" : " " + edition), card.getText() == null ? "".getBytes() : card.getText().getBytes(), edition);
                        LOG.log(Level.FINE, "Created card: {0}", c.getName());
                    } catch (DBException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
                //Add the card attributes
                try {
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put("Rarity", card.getRarity());
                    attributes.put("Cost", card.getCost());
                    attributes.put("Damage", card.getDamage());
                    attributes.put("Language", card.getLanguage());
                    attributes.put("Type", card.getCardType());
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
        }
        return c;
    }

    @Override
    public String getActionName() {
        return MessageFormat.format("Updating {0} game...", getGame().getName());
    }

    public static void main(String[] args) {
        SDUpdater updater = new SDUpdater();
        try {
            Lookup.getDefault().lookup(IDataBaseCardStorage.class).initialize();
        } catch (DBException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
        if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).gameExists(new SDGame().getName())) {
            try {
                Lookup.getDefault().lookup(IDataBaseCardStorage.class).createGame(new SDGame().getName());
            } catch (DBException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        updater.updateLocal();
        updater.updateRemote();
    }

    private List<ISDCard> parseEdition(String link) throws IOException {
        List<ISDCard> cards = new ArrayList<>();
        Document doc = Jsoup.connect(link).get();
        Element content = doc.getElementById("page_container");
        for (Element child : content.children()) {
            SDCard temp = new SDCard();
            //Page, get rarity
            LOG.log(Level.FINE, "Rarity: {0}", child.attr("rarity"));
            temp.setRarity(child.attr("rarity"));
            int divCount = 0;
            for (Element child2 : child.children()) {
                LOG.fine(child2.nodeName());
                switch (child2.nodeName()) {
                    case "h2":
                        //Page name
                        temp.setName(child2.text());
                        break;
                    case "div":
                        switch (divCount) {
                            case 0:
                                //This is the page details
                                for (Element child3 : child2.children()) {
                                    switch (child3.nodeName()) {
                                        case "label":
                                            //Keywords
                                            String keywords = child3.text();
                                            StringTokenizer st
                                                    = new StringTokenizer(keywords, ",");
                                            while (st.hasMoreTokens()) {
                                                temp.addKeyword(st.nextToken().trim());
                                            }
                                            break;
                                        case "b":
                                            String costStart = "<b>COST:</b>";
                                            String damageStart = "<b>DMG:</b>";
                                            String hpStart = "<b>HP:</b>";
                                            //Here there's lot of data, sadly is not properly formatted so we need to massage it.
                                            String text = child2.html();
                                            if (text.contains(hpStart)) {
                                                //Extract cost
                                                String hp = text.substring(text.indexOf(hpStart) + hpStart.length(),
                                                        text.indexOf("<br />", text.indexOf(hpStart)));
                                                hp = hp.replaceAll("\n", "");
                                                temp.setHp(hp.trim());
                                            }
                                            if (text.contains(costStart)) {
                                                //Extract cost
                                                String cost = text.substring(text.indexOf(costStart) + costStart.length(),
                                                        text.indexOf("<br />", text.indexOf(costStart)));
                                                cost = cost.replaceAll("\n", "");
                                                if (cost.contains("<img")) {
                                                    //Process momentum icons
                                                    cost = cost.replaceAll("<img src=\"http://sdrelaunched.com/moicons/icn_", "");
                                                    cost = cost.replaceAll(".jpg\" />", "");
                                                }
                                                temp.setCost(cost.trim());
                                            }
                                            if (text.contains(damageStart)) {
                                                //Extract Damage
                                                String damage = text.substring(text.indexOf(damageStart) + costStart.length(),
                                                        text.indexOf("<br />", text.indexOf(damageStart)));
                                                damage = damage.replaceAll("\n", "");
                                                temp.setDamage(damage.trim());
                                            }
                                            break;
                                        case "span":
                                            temp.setMoveType(child3.text());
                                            break;
                                    }
                                }
                                divCount++;
                                break;
                            case 1:
                                //Page text
                                temp.setText(child2.text());
                                divCount++;
                                break;
                            default:
                                throw new RuntimeException("Unexpected page section!");
                        }
                        break;
                }
            }
            cards.add(temp);
        }
        return cards;
    }

    private List<SDSet> parse(String html) throws IOException {
        synchronized (this) {
            String edition;
            Matcher matcher;
            List<SDSet> sets = new ArrayList<>();
            int start = html.indexOf("Editions:");
            String temp = html.substring(start, html.indexOf("</div>", start));
            matcher = editionStartPattern.matcher(temp);
            while (matcher.find()) {
                edition = matcher.group(1);
                boolean exists = false;
                for (SDSet s : sets) {
                    LOG.log(Level.INFO, "Set Name: {0} Edition: {1}",
                            new Object[]{s.getName(), edition});
                    if (s.getName().equals(edition)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    LOG.log(Level.INFO, "Edition: {0}", edition);
                    List<ISDCard> cards = parseEdition(editionURL.replaceAll("%n", edition.toLowerCase().replaceAll(" ", "%20")));
                    SDSet set = new SDSet(edition);
                    set.addCard(cards);
                    sets.add(set);
                }
            }
            return sets;
        }
    }
}
