package mtg.card.sync;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICardSet;

import forohfor.scryfall.api.Card;
import forohfor.scryfall.api.MTGCardQuery;
import forohfor.scryfall.api.Set;
import mtg.card.MagicCard;

/**
 * This will get a list of sets form Scryfall.
 *
 * @author Javier Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ParseScryfall extends AbstractScryfallAPI
{
  private final Collection<Edition> newSets = new ArrayList<>();
  private final Collection<Edition> allParsed = new ArrayList<>();
  private static final Logger LOG
          = Logger.getLogger(ParseScryfall.class.getSimpleName());
  private final static Map<String, Set> SETS = new HashMap<>();

  /**
   * Parse sets from Scryfall.
   */
  public void getSets()
  {
    MTGCardQuery.getSets().forEach(set ->
    {
      String name = set.getName();
      LOG.log(Level.FINE, "Parsed set: {0}", name);
      Edition ed = Editions.getInstance().getEditionByName(name);
      if (ed == null)
      {
        //Add new set
        ed = Editions.getInstance().addEdition(name, set.getCode());
        newSets.add(ed);
      }
      allParsed.add(ed);
      //Update set map
      SETS.put(name, set);
    });
  }

  /**
   * Get the set icon URI.
   *
   * @param set Set to search for.
   * @return Icon URI or null if not found.
   */
  public URI getSetIconURI(ICardSet set) throws URISyntaxException
  {
    return new URI(SETS.get(set.getName()).getSetIconURI());
  }

  /**
   * New sets parsed compared to existing ones locally.
   *
   * @return New sets parsed compared to existing ones locally.
   */
  public Collection<Edition> getNewSets()
  {
    if (allParsed.isEmpty())
    {
      getSets();
    }
    return newSets;
  }

  /**
   * All parsed sets.
   *
   * @return All parsed sets.
   */
  public Collection<Edition> getAllSets()
  {
    if (allParsed.isEmpty())
    {
      getSets();
    }
    return allParsed;
  }

  /**
   * Get cards from set.
   *
   * @param set Set to look cards for.
   * @return list of cards for set.
   */
  public List<Card> getSetCards(Edition set)
  {
    return getSetCards(set.getName());
  }

  /**
   * Get cards from set.
   *
   * @param set Set to look cards for.
   * @return list of cards for set.
   */
  public List<Card> getSetCards(String set)
  {
    return MTGCardQuery.getCardsFromURI(SETS.get(set).getSearchUri());
  }

  public List<MagicCard> parseCards(Collection<Edition> sets)
  {
    List<MagicCard> cards = new ArrayList<>();
    sets.forEach(set ->
    {
      getSetCards(set).forEach(c ->
      {
        MagicCard card = new MagicCard();
        card.setId(c.getScryfallUUID() );
        card.setName(c.getName());
        card.setCost(c.getManaCost());
        card.setType(c.getTypeLine());
        card.setOracleText(c.getOracleText());
        card.setPower(c.getPower());
        card.setToughness(c.getToughness());
        card.setSetName(set.getName());
        card.setRarity(c.getRarity());
        cards.add(card);
      });
    });
    return cards;
  }
}
