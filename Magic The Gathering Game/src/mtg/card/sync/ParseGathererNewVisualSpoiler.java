package mtg.card.sync;

import com.sun.istack.NotNull;
import dreamer.card.game.Editions;
import dreamer.card.game.Editions.Edition;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mtg.card.IMagicCard;
import mtg.card.MagicCard;

public class ParseGathererNewVisualSpoiler {

    public static final String UPDATE_BASIC_LAND_PRINTINGS = "land";
    public static final String UPDATE_OTHER_PRINTINGS = "other.printings";
    public static final String UPDATE_LANGUAGE = "lang";
    private final static Charset UTF_8 = Charset.forName("utf-8");

    /*-
     <tr class="cardItem evenItem">
     <td class="leftCol">
     <div class="clear"></div>
     <a href="../Card/Details.aspx?multiverseid=154408" id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl00_listRepeater_ctl00_cardImageLink" onclick="return CardLinkAction(event, this, 'SameWindow');">
     <img src="../../Handlers/Image.ashx?multiverseid=154408&amp;type=card" id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl00_listRepeater_ctl00_cardImage" width="95" height="132" alt="Advice from the Fae" border="0" />
     </a>
     <div class="clear"></div>
     </td>
     <td class="middleCol">
     <div class="clear"></div>
     <div class="cardInfo">
     <span class="cardTitle">
     <a id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl00_listRepeater_ctl00_cardTitle" onclick="return CardLinkAction(event, this, 'SameWindow');" href="../Card/Details.aspx?multiverseid=154408">Advice from the Fae</a></span> <span class="manaCost">
     <img src="/Handlers/Image.ashx?size=small&amp;name=2U&amp;type=symbol" alt="Two or Blue" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=2U&amp;type=symbol" alt="Two or Blue" align="absbottom" /><img src="/Handlers/Image.ashx?size=small&amp;name=2U&amp;type=symbol" alt="Two or Blue" align="absbottom" /></span> (<span class="convertedManaCost">6</span>)
     <br />
     <span class="typeLine">
     Sorcery
     </span>
     <br />
     <div class="rulesText">
     <p><i>(<img src="/Handlers/Image.ashx?size=small&amp;name=2U&amp;type=symbol" alt="Two or Blue" align="absbottom" /> can be paid with any two mana or with <img src="/Handlers/Image.ashx?size=small&amp;name=U&amp;type=symbol" alt="Blue" align="absbottom" />. This card's converted mana cost is 6.)</i></p><p>Look at the top five cards of your library. If you control more creatures than any other player, put two of those cards into your hand. Otherwise, put one of them into your hand. Then put the rest on the bottom of your library in any order.</p></div>
     </div>
     </td>
     <td class="rightCol setVersions">
     <div class="clear"></div>
     <div>
     <div id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_ctl00_listRepeater_ctl00_cardSetCurrent" class="rightCol">
     <a onclick="return CardLinkAction(event, this, 'SameWindow');" href="../Card/Details.aspx?multiverseid=154408"><img title="Shadowmoor (Uncommon)" src="../../Handlers/Image.ashx?type=symbol&amp;set=SHM&amp;size=small&amp;rarity=U" alt="Shadowmoor (Uncommon)" style="border-width:0px;" /></a>
     </div>
     </div>

     </td>
     </tr>

     *
     * */
    public static interface ILoadCardHander {

        void handle(MagicCard card);

        void handleSecondary(MagicCard primary, MagicCard secondary);

        void edition(Edition ed);
    }

    public static class OutputHandler implements ILoadCardHander {

        private PrintStream out;
        private boolean loadLandPrintings;
        private boolean loadOtherPrintings;

        public OutputHandler(PrintStream st, boolean loadLandPrintings, boolean loadOtherPrintings) {
            this.out = st;
            this.loadLandPrintings = loadLandPrintings;
            this.loadOtherPrintings = loadOtherPrintings;
        }

        @Override
        public void handle(MagicCard card) {
            TextPrinter.print(card, this.out);
        }

        @Override
        public void edition(Edition ed) {
            Edition res = Editions.getInstance().addEdition(ed.getName(), ed.getMainAbbreviation());
            if (res.getReleaseDate() == null) {
                res.setReleaseDate(Calendar.getInstance().getTime());
            }
        }

        @Override
        public void handleSecondary(MagicCard primary, MagicCard secondary) {
            if (loadLandPrintings && primary.getSet() != null && primary.getSet().equals(secondary.getSet())) {
                handle(secondary);
            } else if (loadOtherPrintings) {
                handle(secondary);
            }
        }
    }
    private final static String base = "http://gatherer.wizards.com/Pages/Search/Default.aspx?output=standard&special=true";
    private static String[] updateAll = { //
        base + "&format=[%22Legacy%22]", //
        base + "&set=[%22Unhinged%22]", //
        base + "&set=[%22Unglued%22]", //
    };
    private static String[] updateLatest = {base + "&format=[%22Standard%22]"};

    public static void main(String[] args) throws IOException {
        String from = args[0];
        String to = null;
        if (args.length > 1) {
            to = args[1];
        }
        Properties options = new Properties();
        if (from.equals("updateAll")) {
            updateAll(to, updateAll, options);
        } else {
            parseFileOrUrl(from, to, options);
        }
        Editions.getInstance().save();
    }

    private static void updateAll(String to, String[] urls, Properties options) throws MalformedURLException, IOException {
        PrintStream out = System.out;
        if (to != null) {
            out = new PrintStream(new File(to));
        }
        TextPrinter.printHeader(IMagicCard.DEFAULT, out);
        for (String string : urls) {
            System.err.println("Loading " + string);
            loadUrl(new URL(string), createOutputHandler(out, options));
        }
        out.close();
    }

    private static OutputHandler createOutputHandler(PrintStream out, Properties options) {
        String land = (String) options.get(UPDATE_BASIC_LAND_PRINTINGS);
        boolean bland = "true".equals(land);
        String other = (String) options.get(UPDATE_OTHER_PRINTINGS);
        boolean bother = "true".equals(other);
        return new OutputHandler(out, bland, bother);
    }

    public static boolean loadUrl(URL url, ILoadCardHander handler) throws IOException {
        InputStream openStream = url.openStream();
        BufferedReader st = new BufferedReader(new InputStreamReader(openStream, UTF_8));
        boolean res = processFile(st, handler);
        st.close();
        return res;
    }

    public static void loadFile(File file, ILoadCardHander handler) throws IOException {
        BufferedReader st = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF_8));
        processFile(st, handler);
        st.close();
    }

    public static void parseFileOrUrl(String from, String to, Properties options) throws FileNotFoundException,
            MalformedURLException, IOException {
        PrintStream out = System.out;
        if (to != null) {
            out = new PrintStream(new FileOutputStream(new File(to)), true, UTF_8.toString());
        }
        TextPrinter.printHeader(IMagicCard.DEFAULT, out);
        OutputHandler handler = createOutputHandler(out, options);
        try {
            countCards = 0;
            if (from.startsWith("http:")) {
                // http://ww2.wizards.com/gatherer/index.aspx?setfilter=All%20sets&colorfilter=White&output=Spoiler
                int i = 0;
                boolean lastPage = false;
                while (lastPage == false && i < 2000 ) {
                    URL url = new URL(from + "&page=" + i);
                    lastPage = loadUrl(url, handler);
                    i++;
                    int pages = countCards / 25 + 1;
                }
            } else {
                File input = new File(from);
                loadFile(input, handler);
            }
        } finally {
            out.close();
        }
    }
    private static Pattern countPattern = Pattern.compile("Search:<span id=\"ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay\"><i>.*</i>  \\((\\d+)\\)</span>");
    private static Pattern lastPagePattern = Pattern.compile("\\Q<span style=\"visibility:hidden;\">&nbsp;&gt;</span></div>");
    private static int countCards;

    private static boolean processFile(BufferedReader st, ILoadCardHander handler) throws IOException {
        String line = "";
        int state = 0;
        boolean lastPage = false;
        boolean cards = false;
        while ((state == 0 && (line = st.readLine()) != null) || (state == 1)) {
            Matcher cm = countPattern.matcher(line);
            if (cm.find()) {
                countCards = Integer.parseInt(cm.group(1));
            }
            if (lastPagePattern.matcher(line).find()) {
                lastPage = true;
            }
            if (line.matches(".*class=\"cardItem .*")) {
                String tr = "";
                do {
                    if (line.matches(".*</tr>.*")) {
                        state = 1;
                        break;
                    }
                    tr += line + " ";
                } while ((line = st.readLine()) != null);
                parseRecord(tr, handler);
                cards = true;
                continue;
            }
            state = 0;
        }
        if (cards == false) {
            throw new IOException("No results");
        }
        return lastPage;
    }
    static Pattern spanPattern = Pattern.compile("class=[^>]*>(.*)</span>");
    static Pattern divPattern = Pattern.compile("class=[^>]*>(.*?)</div>");
    static Pattern idPattern = Pattern.compile("href=.*/Card/Details.aspx\\?multiverseid=(\\d+)");
    static Pattern setPattern = Pattern.compile("title=\"(.*) \\((.*)\\)\" src=.*set=(\\w+)");
    static Pattern namePattern = Pattern.compile(".*>(.*)</a></span>");
    static Pattern powPattern = Pattern.compile("\\((\\d+/)?(\\d+)\\)");

    private static void parseRecord(String line, ILoadCardHander handler) {
        MagicCard card = new MagicCard();
        // split by td
        String[] rows = line.split("<td");
        String[] fields = rows[2].split("<span|<div");
        String id = getMatch(idPattern, fields[3]);
        card.setId(id);
        card.setName(getMatch(namePattern, fields[3]));
        String cost = getMatch(spanPattern, fields[4]);
        card.setCost(cost);
        String type = getMatch(spanPattern, fields[6]);
        String powerCombo = type;
        String pow = getMatch(powPattern, powerCombo, 1).replaceFirst("/", "");
        String tou = getMatch(powPattern, powerCombo, 2);
        type = type.replaceAll("\\(.*", "").trim();
        card.setType(type);
        String text = fixText(getMatch(divPattern, fields[7]));
        card.setOracleText(text);
        card.setPower(pow);
        card.setToughness(tou);
        String[] sets = rows[3].split("<a onclick");
        for (String set : sets) {
            String edition = getMatch(setPattern, set, 1);
            String rarity = getMatch(setPattern, set, 2);
            String abbr = getMatch(setPattern, set, 3);
            String setId = getMatch(idPattern, set, 1);
            if (edition.length() <= 1) {
                continue;
            }
            edition = edition.trim();
            Edition ed = new Editions.Edition(edition, abbr);
            if (id.equals(setId)) {
                card.setSet(edition);
                card.setRarity(rarity.trim());
                handler.edition(ed);
            } else {
                // other printings
                MagicCard card2 = (MagicCard) card.clone();
                card2.setId(setId);
                card2.setSet(edition);
                card2.setRarity(rarity.trim());
                handler.edition(ed);
                handler.handleSecondary(card, card2);
            }
        }
        // print
        handler.handle(card);
    }

    private static String getMatch(Pattern textPattern, String typeF) {
        return getMatch(textPattern, typeF, 1);
    }

    private static String getMatch(Pattern textPattern, String line, int g) {
        Matcher matcher;
        matcher = textPattern.matcher(line);
        String text = "";
        if (matcher.find()) {
            text = matcher.group(g);
            if (text == null) {
                text = "";
            }
        }
        String res = htmlToString(text).trim();
        if (res.length() == 0) {
            res = " ";
        }
        return res;
    }
    final static Map manaMap = new LinkedHashMap();

    static {
        manaMap.put("\\Q{500}", "{0.5}");
        manaMap.put("\\{(\\d)([BUGRW])\\}", "{$1/$2}");
        manaMap.put("\\{([BUGRW])([BUGRW])\\}", "{$1/$2}");
        manaMap.put("\\Q{tap}", "{T}");
        manaMap.put("\\Q{untap}", "{Q}");
    }

    private static String fixText(String str1) {
        String str = str1;
        str = str.replaceAll("</p><p>", "<br>");
        str = str.replaceFirst("<p>", "");
        str = str.replaceFirst("</p>", "");
        return str;
    }

    public static void printBytes(byte[] array, String name) {
        for (int k = 0; k < array.length; k++) {
            System.out.println(name + "[" + k + "] = " + "0x" + UnicodeFormatter.byteToHex(array[k]));
        }
    }

    static class UnicodeFormatter {

        static public String byteToHex(byte b) {
            // Returns hex String representation of byte b
            char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            char[] array = {hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f]};
            return new String(array);
        }

        static public String charToHex(char c) {
            // Returns hex String representation of char c
            byte hi = (byte) (c >>> 8);
            byte lo = (byte) (c & 0xff);
            return byteToHex(hi) + byteToHex(lo);
        }
    } // class
    private static String LONG_MINUS;

    static {
        try {
            LONG_MINUS = new String(new byte[]{(byte) 0xe2, (byte) 0x80, (byte) 0x94}, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // hmm
        }
    }

    public static String htmlToString(String str) {
        str = str.replaceAll("\\Q " + LONG_MINUS, "-");
        str = str.replaceAll("&nbsp;", " ");
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&apos;", "'");
        str = str.replaceAll("&quot;", "\"");
        if (str.contains("img")) {
            str = str.replaceAll("<img [^<]*name=([^&]*)&[^>]*/>", "{$1}");
            for (Iterator iterator = manaMap.keySet().iterator(); iterator.hasNext();) {
                String alt = (String) iterator.next();
                String to = (String) manaMap.get(alt);
                str = str.replaceAll(alt, to);
            }
        }
        return str;
    }

    @NotNull
    public static URL createImageURL(int cardId, String editionAbbr) throws MalformedURLException {
        return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + cardId + "&type=card");
    }

    public static URL createSetImageURL(String editionAbbr, String rarity) {
        try {
            String rarLetter = rarity == null ? "C" : rarity.substring(0, 1).toUpperCase();
            return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?type=symbol&set=" + editionAbbr + "&size=small&rarity="
                    + rarLetter);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static URL createManaImageURL(String symbol) {
        String manaName = symbol.replaceAll("[{}/]", "");
        try {
            return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=" + manaName + "&type=symbol");
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static void downloadUpdates(String set, String file, Properties options) throws FileNotFoundException,
            MalformedURLException, IOException {
        String url;
        if (set == null || set.equals("Standard")) {
            url = updateLatest[0];
        } else {
            if (set.equalsIgnoreCase("All")) {
                url = base + "&set=[%22%22]";
            } else {
                url = base + "&set=[%22" + set.replaceAll(" ", "%20") + "%22]&sort=cn+";
            }
        }
        parseFileOrUrl(url, file, options);
    }
}
