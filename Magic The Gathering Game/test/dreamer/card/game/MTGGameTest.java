package dreamer.card.game;

import dreamer.card.game.storage.IDataBaseManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import org.junit.*;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class MTGGameTest {

    private static final Logger LOG = Logger.getLogger(MTGGameTest.class.getName());

    public MTGGameTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Lookup.getDefault().lookup(IDataBaseManager.class).setPU("Card_Game_Interface_TestPU");
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
     * Test of updateDatabase method, of class MTGGame.
     */
    @Test
    public void testUpdateDatabase() {
        try {
            final MTGGame instance = new MTGGame();
            synchronized (instance) {
                instance.init();
                Runnable updateRunnable = instance.getUpdateRunnable();
                updateRunnable.run();
                updateRunnable.wait();
                assertFalse(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("CardSet.findAll").isEmpty());
                assertFalse(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("Card.findAll").isEmpty());
                assertFalse(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("CardAttribute.findAll").isEmpty());
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            fail();
        }
    }
}
