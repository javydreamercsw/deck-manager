/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mtg.card.sync;

import dreamer.card.game.ICardField;
import dreamer.card.game.storage.MemoryCardStore;
import java.util.HashSet;
import java.util.Set;
import mtg.card.IMagicCard;
import mtg.card.MagicCard;
import mtg.card.MagicCardField;
import org.junit.*;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class UpdateCardsFromWebTest {

    public UpdateCardsFromWebTest() {
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
     * Test of updateStore method, of class UpdateCardsFromWeb.
     */
    @Test
    public void testUpdateStore_4args() throws Exception {
        System.out.println("updateStore");
        MagicCard card = new MagicCard();
        card.setSet("Time Spiral");
        card.setName("Amrou Scout");
        ParseGathererSets parser = new ParseGathererSets();
        parser.load();
        CardCache.setLoadingEnabled(true);
        CardCache.setCachingEnabled(true);
        Set<ICardField> fieldMaps = new HashSet<ICardField>();
        fieldMaps.add(MagicCardField.TEXT);
        fieldMaps.add(MagicCardField.LANG);
        fieldMaps.add(MagicCardField.ID);
        String lang = "";
        MemoryCardStore<IMagicCard> fstore = new MemoryCardStore<IMagicCard>();
        fstore.add(card);
        UpdateCardsFromWeb instance = new UpdateCardsFromWeb();
        instance.updateStore(card, fieldMaps, lang, fstore);
    }
}
