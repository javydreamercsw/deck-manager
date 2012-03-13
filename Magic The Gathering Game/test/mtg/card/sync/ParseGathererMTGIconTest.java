package mtg.card.sync;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ParseGathererMTGIconTest {

    public ParseGathererMTGIconTest() {
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
     * Test of getIconURL method, of class ParseGathererMTGIcon.
     */
    @Test
    public void testGetIconURL() {
        System.out.println("getIconURL");
        try {
            ParseGathererMTGIcon parser = new ParseGathererMTGIcon();
            parser.load();
            assertTrue(parser.getIconURL() != null);
        } catch (IOException ex) {
            Logger.getLogger(ParseGathererMTGIcon.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
