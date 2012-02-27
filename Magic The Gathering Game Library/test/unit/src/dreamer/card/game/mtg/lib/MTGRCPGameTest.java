/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamer.card.game.mtg.lib;

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
public class MTGRCPGameTest {

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
            Thread thread = new Thread(instance.getUpdateRunnable());
            thread.start();
            thread.join();
            assertFalse(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("CardSet.findAll").isEmpty());
            assertFalse(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("Card.findAll").isEmpty());
            assertFalse(Lookup.getDefault().lookup(IDataBaseManager.class).namedQuery("CardAttribute.findAll").isEmpty());
        } catch (Exception ex) {
            Logger.getLogger(MTGRCPGameTest.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }
}
