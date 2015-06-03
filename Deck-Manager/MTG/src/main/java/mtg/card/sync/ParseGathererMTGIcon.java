package mtg.card.sync;

import com.reflexit.magiccards.core.cache.ICardCache;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mtg.card.game.MTGGame;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ParseGathererMTGIcon extends AbstractParseGathererPage {

    private String iconURL = null;
    private static final Logger LOG
            = Logger.getLogger(ParseGathererMTGIcon.class.getName());

    @Override
    protected void loadHtml(String html) {
        Pattern iconPattern = Pattern.compile("<link .*? href=\"(.*?.ico)\"");
        Matcher matcher = iconPattern.matcher(html);
        if (matcher.find()) {
            String match = matcher.group(1);
            LOG.fine(match);
            iconURL = GATHERER_URL_BASE + match;
        }
    }

    @Override
    protected String getUrl() {
        return GATHERER_URL_BASE;
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
            try {
                Lookup.getDefault().lookup(ICardCache.class)
                        .getGameIcon(new MTGGame());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
