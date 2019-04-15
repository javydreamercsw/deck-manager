package mtg.card.sync.scryfall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;

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
    });
  }

  public Collection<Edition> getNew()
  {
    return newSets;
  }

  public Collection<String> getAll()
  {
    return allParsed;
  }
}
