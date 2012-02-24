/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mtg.card.sync;

import java.util.logging.Logger;
import org.junit.*;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class CardCacheTest {
    
    public CardCacheTest() {
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
     * Test of setCahchingEnabled method, of class CardCache.
     */
    @Test
    public void testSetCachhingEnabled() {
        CardCache.setCachingEnabled(true);
        CardCache.setLoadingEnabled(true);
        //TODO
    }
    private static final Logger LOG = Logger.getLogger(CardCacheTest.class.getName());
}
