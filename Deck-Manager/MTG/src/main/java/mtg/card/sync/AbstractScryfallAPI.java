package mtg.card.sync;

/**
 * See: https://scryfall.com/docs/api for more details.
 *
 * @author Javier Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public abstract class AbstractScryfallAPI
{
  protected final String API = "https://api.scryfall.com";
  //Milliseconds as requested in the API page.
  protected final int DEFAULT_DELAY = 100;
}
