/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mtg.card.sync;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardSet;
import dreamer.card.game.MTGGame;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.*;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ParseGathererSetIconsTest {

    public ParseGathererSetIconsTest() {
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
     * Test of getIconURL method, of class ParseGathererSetIcons.
     */
    @Test
    public void testGetIconURL() {
        System.out.println("getSetIconURL");
        try {
            ICardSet set = new ICardSet() {

                @Override
                public String getName() {
                    return "Portal";
                }

                @Override
                public String getGameName() {
                    return "Magic the gathering";
                }

                @Override
                public Iterator iterator() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Collection getCards() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
            ParseGathererSetIcons parser = new ParseGathererSetIcons(set);
            parser.load();
            assertTrue(parser.getIconURL() != null);
            for (Iterator<ICardCache> it = new MTGGame().getCardCacheImplementations().iterator(); it.hasNext();) {
                ICardCache cache = it.next();
                cache.getSetIcon(set);
            }
        } catch (IOException ex) {
            Logger.getLogger(ParseGathererSetIcons.class.getName()).log(Level.SEVERE, null, ex);
            fail();
        }
    }
}
