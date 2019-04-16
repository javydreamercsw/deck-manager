package mtg.card.sync;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.testng.annotations.Test;

import com.reflexit.magiccards.core.model.Editions.Edition;

import mtg.card.MagicCard;

public class ParseScryfallNGTest
{

  /**
   * Test of getSets method, of class ParseScryfall.
   */
  @Test
  public void testGetSets()
  {
    System.out.println("getSets");
    ParseScryfall parser = new ParseScryfall();
    parser.getSets();

    Collection<Edition> newSets = parser.getNewSets();
    Collection<Edition> allSets = parser.getAllSets();

    assertFalse(allSets.isEmpty());
    assertTrue(allSets.size() >= newSets.size());
  }

  @Test
  public void testGetCardsForSet()
  {
    System.out.println("getSetCards");

    ParseScryfall parser = new ParseScryfall();
    
    List<MagicCard> cards = parser.parseCards(parser.getAllSets());
    assertFalse(cards.isEmpty());
            
    cards.forEach(card ->
    {
      System.out.println(card);
    });
  }
}
