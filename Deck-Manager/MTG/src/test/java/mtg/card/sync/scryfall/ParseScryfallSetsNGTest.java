package mtg.card.sync.scryfall;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import com.reflexit.magiccards.core.model.Editions.Edition;

public class ParseScryfallSetsNGTest
{

  /**
   * Test of getSets method, of class ParseScryfallSets.
   */
  @Test
  public void testGetSets()
  {
    System.out.println("getSets");
    ParseScryfallSets parser = new ParseScryfallSets();
    parser.getSets();

    Collection<Edition> newSets = parser.getNew();
    Collection<String> allSets = parser.getAll();
    
    assertFalse(allSets.isEmpty());
    assertTrue(allSets.size() >= newSets.size());
  }
}
