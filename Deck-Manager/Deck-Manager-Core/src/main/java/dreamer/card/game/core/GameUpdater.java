package dreamer.card.game.core;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.ICardType;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.Card;
import com.reflexit.magiccards.core.storage.database.CardHasCardAttribute;
import com.reflexit.magiccards.core.storage.database.CardSet;
import com.reflexit.magiccards.core.storage.database.Game;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public abstract class GameUpdater extends UpdateRunnable {

    private static final Logger LOG
            = Logger.getLogger(GameUpdater.class.getName());
    protected boolean dbError = false;
    protected boolean localUpdated = false;
    protected boolean remoteUpdated = false;

    public GameUpdater(ICardGame game) {
        super(game);
    }

    @Override
    public void updateLocal() {
        if (!localUpdated) {
            //This section updates from the deployed database
            //Create game cache dir
            File cacheDir = Places.getCacheSubdirectory(".Deck Manager");
            //Check if database is present, if not copy the default database (to avoid long initial update that was 45 minutes long on my test)
            File dbDir = new File(MessageFormat.format("{0}{1}data", cacheDir.getAbsolutePath(), System.getProperty("file.separator")));
            dbDir.mkdirs();
            File db = InstalledFileLocator.getDefault().locate("card_manager.h2.db", "dreamer.card.game.mtg.lib", false);
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
                    dbProperties.put(PersistenceUnitProperties.JDBC_URL, MessageFormat.format("jdbc:h2:file:{0}{1}{2};AUTO_SERVER=TRUE", db.getParentFile().getAbsolutePath(), System.getProperty("file.separator"), dbName));
                    dbProperties.put(PersistenceUnitProperties.TARGET_DATABASE, "org.eclipse.persistence.platform.database.H2Platform");
                    dbProperties.put(PersistenceUnitProperties.JDBC_PASSWORD, "test");
                    dbProperties.put(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
                    dbProperties.put(PersistenceUnitProperties.JDBC_USER, "deck_manager");
                    emf = Persistence.createEntityManagerFactory("Card_Game_InterfacePU", dbProperties);
                    HashMap parameters = new HashMap();
                    parameters.put("name", getGame().getName());
                    Game game = (Game) namedQuery("Game.findByName", parameters, emf).get(0);
                    if (game.getCardSetList().size() > Lookup.getDefault().lookup(IDataBaseCardStorage.class).getSetsForGame(game).size()) {
                        for (Iterator<CardSet> it = game.getCardSetList().iterator(); it.hasNext();) {
                            if (!dbError) {
                                ICardSet set = it.next();
                                LOG.log(Level.FINE, "Checkig set: {0}", set.getName());
                                if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardSetExists(set.getName(), getGame())) {
                                    Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCardSet(game, set.getName(), Editions.getInstance().getEditionByName(set.getName()).getMainAbbreviation(), new Date());
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
                                    Game g;
                                    parameters.clear();
                                    parameters.put("name", getGame().getName());
                                    g = (Game) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Game.findByName", parameters).get(0);
                                    ICardSet target = null;
                                    for (ICardSet cs : g.getCardSetList()) {
                                        if (cs.getName().equals(set.getName())) {
                                            target = cs;
                                            break;
                                        }
                                    }
                                    if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardExists(card.getName(), target)) {
                                        List<CardHasCardAttribute> attributes = ((Card) card).getCardHasCardAttributeList();
                                        card = Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCard(cardType, card.getName(), ((Card) card).getText(), set.getName());
                                        LOG.log(Level.FINE, "Added card: {0}", card.getName());
                                        for (CardHasCardAttribute attr : attributes) {
                                            if (Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardAttribute(card, attr.getCardAttribute().getName()) == null) {
                                                Lookup.getDefault().lookup(IDataBaseCardStorage.class).addAttributeToCard(card, attr.getCardAttribute().getName(), attr.getValue());
                                                LOG.log(Level.FINE, "Added attribute: {0} with value: {1}!", new Object[]{attr.getCardAttribute().getName(), attr.getValue()});
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
                                        LOG.log(Level.FINE, "Card set: {0} already has card {1}", new Object[]{set.getName(), card.getName()});
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
            localUpdated = true;
        }
    }
}
