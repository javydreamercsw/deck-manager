package mtg.card.sync;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.CardSet;

import mtg.card.MagicException;
import mtg.card.game.MTGGame;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ParseGathererSetIcons extends AbstractParseGathererPage {

    private final ICardSet set;
    private String iconURL = null;
    private static final Logger LOG
            = Logger.getLogger(ParseGathererSetIcons.class.getSimpleName());

    public ParseGathererSetIcons(ICardSet set) {
        this.set = set;
    }

    @Override
    protected void loadHtml(String html) {
        String pattern = "(?<=set=)([A-Z0-9]*[0-9A-Z][_]*[A-Z0-9]*[0-9A-Z]*)(?=\\&)";
        Pattern iconPattern = Pattern.compile(pattern);
        html = html.replaceAll("\r?\n", " ");
        Matcher matcher = iconPattern.matcher(html);
        if (matcher.find()) {
            String match = matcher.group();
            LOG.fine(match);
            iconURL = createSetImageURL(match, "C").toString();
        } else {
            LOG.log(Level.SEVERE, "Unable to match pattern {0}", 
                    new Object[]{iconPattern});
        }
    }

    public static URL createSetImageURL(String editionAbbr, String rarity) {
        try {
            String rarLetter = rarity == null ? "C" : rarity.substring(0, 1).toUpperCase();
            return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set="
                    + editionAbbr.replaceAll(" ", "%20") + "&size=small&rarity="
                    + rarLetter);
        } catch (MalformedURLException e) {
            throw new MagicException(e);
        }
    }

    @Override
    protected String getUrl() {
        String url = GATHERER_URL_BASE + "Pages/Search/Default.aspx?set=%5b%22"
                + set.getName().replaceAll(" ", "%20").replaceAll("\"", "%22") + "%22%5d";
        LOG.log(Level.FINE, url);
        return url;
    }

    /**
     * @return the iconURL
     */
    public String getIconURL() {
        return iconURL;
    }

    public static void main(String[] args) {
        ICardCache cache = Lookup.getDefault().lookup(ICardCache.class);
        if (cache != null) {
          Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                  .getSetsForGame(new MTGGame()).forEach((o) ->
          {
            try
            {
              CardSet cs = (CardSet) o;
              cache.getSetIcon(cs);
            }
            catch (IOException ex)
            {
              Exceptions.printStackTrace(ex);
            }
          });
        }
    }
}
