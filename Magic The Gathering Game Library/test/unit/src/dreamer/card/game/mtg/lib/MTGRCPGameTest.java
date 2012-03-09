package dreamer.card.game.mtg.lib;

import com.reflexit.magiccards.core.cache.ICacheData;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.Card;
import com.reflexit.magiccards.core.storage.database.CardSet;
import com.reflexit.magiccards.core.storage.database.Game;
import dreamer.card.game.core.Tool;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.*;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class MTGRCPGameTest {

    public MTGRCPGameTest() {
    }

    @BeforeClass
    public static void setUpClass() throws DBException {
        Lookup.getDefault().lookup(IDataBaseCardStorage.class).setPU("Card_Game_Interface_TestPU");
    }

    @AfterClass
    public static void tearDownClass() throws DBException {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of updateDatabase method, of class MTGRCPGame.
     */
    @Test
    public void testUpdateDatabase() {
        try {
            System.out.println("updateDatabase");
            MTGRCPGame instance = new MTGRCPGame();
            assertTrue(Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findAll").isEmpty());
            assertTrue(Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardAttribute.findAll").isEmpty());
            MTGUpdater updater = (MTGUpdater) instance.getUpdateRunnable();
            //Just use one set for the test, is too long otherwise
            updater.createCardsForSet(updater.source + "Alliances" + "%22%5d");
            assertFalse(Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findAll").isEmpty());
            assertFalse(Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardAttribute.findAll").isEmpty());
        } catch (MalformedURLException ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } catch (IOException ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } catch (DBException ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    @Test
    public void testCardCache() {
        System.out.println("test cache");
        File testDir = new File(System.getProperty("user.home")
                + System.getProperty("file.separator") + "cache");
        try {
            MTGRCPGame instance = new MTGRCPGame();
            String setName = "Alliances";
            MTGUpdater updater = (MTGUpdater) instance.getUpdateRunnable();
            HashMap parameters = new HashMap();
            parameters.put("name", instance.getName());
            Editions.getInstance().addEdition(setName, "_" + setName);
            Game mtg = (Game) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Game.findByName", parameters).get(0);
            //Create set
            CardSet cs = (CardSet) Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCardSet(mtg, setName, "_" + setName, new Date());
            //Just use one set for the test, is too long otherwise
            updater.createCardsForSet(updater.source + setName + "%22%5d");
            //Add all cards to the set
            Lookup.getDefault().lookup(IDataBaseCardStorage.class).addCardsToSet((List<Card>) Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findAll"), cs);
            MTGCardCache.setCachingEnabled(true);
            MTGCardCache.setLoadingEnabled(true);
            for (Iterator<ICardCache> it = instance.getCardCacheImplementations().iterator(); it.hasNext();) {
                ICardCache cache = it.next();
                cache.setCacheDir(testDir);
            }
            System.out.println("Test cache dir at: " + testDir.getAbsolutePath());
            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findAll").iterator(); it.hasNext();) {
                Card card = (Card) it.next();
                Lookup.getDefault().lookup(ICacheData.class).add(card);
            }
            System.out.println("Downloading card images...");
            Thread thread = new Thread(Lookup.getDefault().lookup(ICardCache.class).getCacheTask());
            thread.start();
            //Wait 10 seconds, just need a sample of cards to make sure its working
            Thread.currentThread().wait(10000);
            assertTrue(testDir.exists());
            assertTrue(testDir.listFiles().length > 0);
        } catch (InterruptedException ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } catch (MalformedURLException ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } catch (IOException ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } catch (DBException ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        } finally {
            //Clean up
            Tool.deleteFolder(testDir);
        }
    }
}
