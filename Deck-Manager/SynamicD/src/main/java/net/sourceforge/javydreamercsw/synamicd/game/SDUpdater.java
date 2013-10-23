package net.sourceforge.javydreamercsw.synamicd.game;

import com.reflexit.magiccards.core.model.CardFileUtils;
import dreamer.card.game.core.GameUpdater;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = DataBaseStateListener.class)
public class SDUpdater extends GameUpdater implements DataBaseStateListener {

    private static final Logger LOG
            = Logger.getLogger(SDUpdater.class.getName());
    public static final String SD_URL_BASE = "http://sdrelaunched.com/pages.php";
    public static Charset UTF_8 = Charset.forName("utf-8");
    private static final Pattern editionStartPattern
            = Pattern.compile("<a class=\"slinks\" href=\".*?\">(.*?)</a>");
    private static final Pattern pageStartPattern
            = Pattern.compile("<div rarity=\"(.*?)\" class=\"card_block\">");
    private static final Pattern pageNamePattern
            = Pattern.compile("<h2 style=\"margin: 0; text-transform: none;\">(.*?)</h2>");
    private static final Pattern keyWordPatter
            = Pattern.compile("<label style=\"font-size:12px; font-style:oblique\">(.*?)</label><br>");
    private static final Pattern costPattern
            = Pattern.compile("<b>COST:</b>(.*?)\"><br>");
    private static final Pattern damagePattern
            = Pattern.compile("<b>DMG:</b>(.*?)<br>");
    private static final Pattern moveTypePattern
            = Pattern.compile("<span style=\".*?\">(.*?)</span>");
    private static final String editionURL = "http://sdrelaunched.com/pages.php?mset=%n&ptype=0&rtype=0";

    public SDUpdater() {
        super(new SDGame());
    }

    @Override
    public void updateLocal() {
        try {
            super.updateLocal();
            URL url = new URL(SD_URL_BASE);
            InputStream openStream = url.openStream();
            BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
            String tempHtml = CardFileUtils.readFileAsString(st);
            parse(tempHtml);
            st.close();
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void updateRemote() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getActionName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void main(String[] args) {
        SDUpdater updater = new SDUpdater();
        updater.updateLocal();
    }

    private void parseEdition(String link) throws MalformedURLException, IOException {
        URL url = new URL(link);
        InputStream openStream = url.openStream();
        BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
        String html = CardFileUtils.readFileAsString(st);
        String rarity;
        String name;
        String keywords;
        String cost;
        String damage;
        String moveType;
        Matcher matcher;
        Matcher mainMatcher = pageStartPattern.matcher(html);
        boolean found = true;
        while (found) {
            found = mainMatcher.find();
            if (found) {
                rarity = mainMatcher.group(1);
                LOG.info(MessageFormat.format("Rarity: {0}", rarity));
            } else {
                break;
            }
            matcher = pageNamePattern.matcher(html);
            if (matcher.find(mainMatcher.end())) {
                name = matcher.group(1).trim();
                LOG.info(MessageFormat.format("Name: {0}", name));
            }
            matcher = keyWordPatter.matcher(html);
            if (matcher.find(mainMatcher.end())) {
                keywords = matcher.group(1).trim();
                LOG.info(MessageFormat.format("Keywords: {0}", keywords));
            }
            matcher = costPattern.matcher(html);
            if (matcher.find(mainMatcher.end())) {
                cost = matcher.group(1).trim();
                LOG.info(MessageFormat.format("Cost: {0}", cost));
            }
            matcher = damagePattern.matcher(html);
            if (matcher.find(mainMatcher.end())) {
                damage = matcher.group(1).trim();
                LOG.info(MessageFormat.format("Damage: {0}", damage));
            }
            matcher = moveTypePattern.matcher(html);
            if (matcher.find(mainMatcher.end())) {
                moveType = matcher.group(1).trim();
                LOG.info(MessageFormat.format("Move Type: {0}", moveType));
            }
        }
    }

    private void parse(String html) throws IOException {
        String edition;
        Matcher matcher;
        boolean found = true;
        int start = html.indexOf("Editions:");
        String temp = html.substring(start, html.indexOf("</div>", start));
        matcher = editionStartPattern.matcher(temp);
        while (found) {
            found = matcher.find();
            if (found) {
                edition = matcher.group(1);
                LOG.info(MessageFormat.format("Edition: {0}", edition));
                parseEdition(editionURL.replaceAll("%n", edition.toLowerCase().replaceAll(" ", "%20")));
            }
        }
    }
}
