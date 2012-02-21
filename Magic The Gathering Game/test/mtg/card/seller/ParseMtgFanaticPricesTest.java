/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mtg.card.seller;

import dreamer.card.game.storage.ICardStore;
import dreamer.card.game.storage.MemoryCardStorage;
import dreamer.card.game.storage.MemoryCardStore;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtg.card.IMagicCard;
import mtg.card.MagicCard;
import mtg.card.MagicCardFilter;
import mtg.card.MagicFilteredCardStore;
import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ParseMtgFanaticPricesTest {
    
    public ParseMtgFanaticPricesTest() {
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
     * Test of updateStore method, of class ParseMtgFanaticPrices.
     */
    @Test
    public void testUpdateStore() throws Exception {
        System.out.println("updateStore");
         ParseMtgFanaticPrices item = new ParseMtgFanaticPrices();
        item.parseSets();
        HashMap<String, Float> res = item.parse("383");
        try {
            ParseMtgFanaticPrices prices = new ParseMtgFanaticPrices();
            MagicFilteredCardStore<IMagicCard> fstore = new MagicFilteredCardStore<IMagicCard>() {

                MemoryCardStore<MemoryCardStorage<IMagicCard>> store = new MemoryCardStore<MemoryCardStorage<IMagicCard>>();

                @Override
                public ICardStore getCardStore() {
                    return store;
                }
            };
            MagicCard card = new MagicCard();
            card.setSet("Time Spiral");
            card.setName("Amrou Scout");
            fstore.getCardStore().add(card);
            fstore.update(new MagicCardFilter());
            prices.updateStore(fstore);
            Iterator iterator = fstore.iterator();
            while (iterator.hasNext()) {
                MagicCard temp = (MagicCard) iterator.next();
                System.out.println(temp.toString());
                System.out.println("Prize: $" + temp.getDbPrice());
            }
        } catch (Exception ex) {
            Logger.getLogger(FindMagicCardsPrices.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getName method, of class ParseMtgFanaticPrices.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        ParseMtgFanaticPrices instance = new ParseMtgFanaticPrices();
        String result = instance.getName();
        assertTrue(result != null);
    }

    /**
     * Test of getURL method, of class ParseMtgFanaticPrices.
     */
    @Test
    public void testGetURL() {
        System.out.println("getURL");
        ParseMtgFanaticPrices instance = new ParseMtgFanaticPrices();
        URL result = instance.getURL();
        assertTrue(result != null);
    }
}
