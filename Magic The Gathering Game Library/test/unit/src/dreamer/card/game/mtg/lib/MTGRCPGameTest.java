package dreamer.card.game.mtg.lib;

import dreamer.card.game.storage.IDataBaseManager;
import dreamer.card.game.storage.cache.ICardCache;
import dreamer.card.game.storage.database.persistence.Card;
import java.io.File;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtg.card.sync.MTGCardCache;
import static org.junit.Assert.*;
import org.junit.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class MTGRCPGameTest {

    private int progress = 0, lastProgress = -1;
    private int size;

    public MTGRCPGameTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Lookup.getDefault().lookup(IDataBaseManager.class).setPU("Card_Game_Interface_TestPU");
        Lookup.getDefault().lookup(IDataBaseManager.class).getEntityManagerFactory();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
            assertTrue(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("Card.findAll").isEmpty());
            assertTrue(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("CardAttribute.findAll").isEmpty());
            MTGUpdater updater = (MTGUpdater) instance.getUpdateRunnable();
            //Just use one set for the test, is too long otherwise
            updater.createCardsForSet(updater.source + "Alliances" + "%22%5d");
            assertFalse(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("Card.findAll").isEmpty());
            assertFalse(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("CardAttribute.findAll").isEmpty());
        } catch (Exception ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }

    @Test
    public void testCardCache() {
        System.out.println("test cache");
        try {
            MTGRCPGame instance = new MTGRCPGame();
            MTGUpdater updater = (MTGUpdater) instance.getUpdateRunnable();
            //Just use one set for the test, is too long otherwise
            updater.createCardsForSet(updater.source + "Alliances" + "%22%5d");
            MTGCardCache.setCachingEnabled(true);
            MTGCardCache.setLoadingEnabled(true);
            File testDir = new File(System.getProperty("user.dir")
                    + System.getProperty("file.separator") + "cache");
            MTGCardCache.setCacheDir(testDir);
            System.out.println("Test cache dir at: " + testDir.getAbsolutePath());
            for (Iterator it = Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("Card.findAll").iterator(); it.hasNext();) {
                Card card = (Card) it.next();
                MTGCardCache.getCardImageQueue().add(card);
            }
            System.out.println("Downloading card images...");
            Thread thread = new Thread(Lookup.getDefault().lookup(ICardCache.class).getCacheTask());
            thread.start();
            Thread.currentThread().wait(10000);
            assertTrue(testDir.exists());
            assertTrue(testDir.listFiles().length > 0);
            //TODO: delete test folder
            testDir.delete();
        } catch (Exception ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }
}
