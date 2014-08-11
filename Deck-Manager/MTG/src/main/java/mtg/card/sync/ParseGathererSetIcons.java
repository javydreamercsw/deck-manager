package mtg.card.sync;

import com.reflexit.magiccards.core.model.ICardSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ParseGathererSetIcons extends ParseGathererPage {

    private final ICardSet set;
    private String iconURL = null;
    private static final Logger LOG = 
            Logger.getLogger(ParseGathererSetIcons.class.getName());

    public ParseGathererSetIcons(ICardSet set) {
        this.set = set;
    }

    @Override
    protected void loadHtml(String html) {
        Pattern iconPattern = Pattern.compile("<\\s*img\\s*title\\s*=\\\""
                + set.getName().replaceAll("\"", "&quot;")
                + "\\s*\\(\\s*[^\\>]*src\\s*=\\s*([\"\\'])(.*?)\\1");
        html = html.replaceAll("\r?\n", " ");
        Matcher matcher = iconPattern.matcher(html);
        if (matcher.find()) {
            String match = matcher.group();
            /**
             * Should have something like: <img title="Portal (Rare)"
             * src="../../Handlers/Image.ashx?type=symbol&amp;set=PO&amp;size=small&amp;rarity=R"
             */
            Pattern urlPattern = Pattern.compile("src\\s*=\\s*([\"\\'])(.*?)\\1");
            Matcher urlmatcher = urlPattern.matcher(match);
            if (urlmatcher.find()) {
                match = urlmatcher.group();
                iconURL = GATHERER_URL_BASE + match.subSequence(11,
                        match.length() - 2).toString().replaceAll("&amp;", "&") + "C";
            } else {
                LOG.log(Level.SEVERE, "Unable to match pattern to: {0}", match);
            }
        } else {
            LOG.log(Level.SEVERE, "Unable to match pattern {1} to: {0}", new Object[]{html, iconPattern});
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
}
