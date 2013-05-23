package mtg.card.sync;

import com.reflexit.magiccards.core.model.CardFileUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ParseGathererPage {

    public static final String GATHERER_URL_BASE = "http://gatherer.wizards.com/";
    public static Charset UTF_8 = Charset.forName("utf-8");
    private String title = "Loading gatherer info...";
    private String html;

    public void load() throws IOException {
        URL url = new URL(getUrl());
        InputStream openStream = url.openStream();
        BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
        String tempHtml = CardFileUtils.readFileAsString(st);
        st.close();
        setHtml(tempHtml);
        loadHtml(tempHtml);
    }

    protected abstract void loadHtml(String html);

    public void loadHtml() {
        if (html == null) {
            throw new NullPointerException();
        }
        loadHtml(this.html);
    }

    protected String extractPatternValue(String html, Pattern pattern, boolean multiple) {
        Matcher matcher = pattern.matcher(html);
        String value = "";
        while (matcher.find()) {
            String v = matcher.group(1).trim();
            if (value.length() > 0) {
                if (multiple == false) {
                    throw new IllegalStateException("Multiple pattern found where signle expected");
                }
                value += "\n";
            }
            value += v;
        }
        return value;
    }

    protected abstract String getUrl();

    public ParseGathererPage() {
        super();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}