package dreamer.card.game.mtg.lib.gui;

import dreamer.card.game.mtg.lib.MTGRCPGame;
import javax.swing.JLabel;
import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class MTGCardFormatterTest {

    public MTGCardFormatterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
     * Test of getGame method, of class MTGCardFormatter.
     */
    @Test
    public void testGetGame() {
        System.out.println("getGame");
        MTGCardFormatter instance = new MTGCardFormatter();
        assertTrue(instance.getGame().getName().equals(new MTGRCPGame().getName()));
    }

    /**
     * Test of format method, of class MTGCardFormatter.
     */
    @Test
    public void testFormat() {
        System.out.println("format");
        String value = "{3}{B}";
        MTGCardFormatter instance = new MTGCardFormatter();
        Object result = instance.format(value);
        assertTrue(result instanceof JLabel);
    }
}
