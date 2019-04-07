package mtg.card.sync.scryfall;

import java.util.ArrayList;

import forohfor.scryfall.api.MTGCardQuery;
import forohfor.scryfall.api.Set;

/**
 * This will get a list of sets form Scryfall.
 *
 * @author Javier Ortiz Bultron <javierortiz@pingidentity.com>
 */
public class ParseScryfallSets extends AbstractScryfallAPI
{
  public ArrayList<Set> getSets()
  {
    return MTGCardQuery.getSets();
  }

  public static final void main(String[] args)
  {
    new ParseScryfallSets().getSets().forEach(set ->
    {
      System.out.println(set);
    });
  }
}
