package mtg.card.sync.scryfall;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICardSet;

import forohfor.scryfall.api.MTGCardQuery;

/**
 * This will get a list of sets form Scryfall.
 *
 * @author Javier Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ParseScryfallSets extends AbstractScryfallAPI
{
  private final Collection<Edition> newSets = new ArrayList<>();
  private final Collection<String> allParsed = new ArrayList<>();
  private static final Logger LOG
          = Logger.getLogger(ParseScryfallSets.class.getSimpleName());
  private final static Map<String, URI> SET_ICONS = new HashMap<>();

  /**
   * Parse sets from Scryfall.
   */
  public void getSets()
  {
    MTGCardQuery.getSets().forEach(set ->
    {
      String name = set.getName();
      LOG.log(Level.FINE, "Parsed set: {0}", name);
      allParsed.add(name);
      if (!Editions.getInstance().containsName(name))
      {
        Edition ed = Editions.getInstance().addEdition(name, set.getCode());
        newSets.add(ed);
      }
      else
      {
        Editions.getInstance().addEdition(name, set.getCode());
      }

      try
      {
        //Update icon URL
        SET_ICONS.put(name, new URI(set.getSetIconURI()));
      }
      catch (URISyntaxException ex)
      {
        LOG.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
    });
  }

  /**
   * Get the set icon URI.
   *
   * @param set Set to search for.
   * @return Icon URI or null if not found.
   */
  public URI getSetIconURI(ICardSet set)
  {
    return SET_ICONS.get(set.getName());
  }

  /**
   * New sets parsed compared to existing ones locally.
   *
   * @return New sets parsed compared to existing ones locally.
   */
  public Collection<Edition> getNewSets()
  {
    return newSets;
  }

  /**
   * All parsed sets.
   *
   * @return All parsed sets.
   */
  public Collection<String> getAllSets()
  {
    return allParsed;
  }
}
