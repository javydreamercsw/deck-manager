package mtg.card.seller;

import dreamer.card.game.ISellableCard;
import dreamer.card.game.price.IPriceProvider;
import dreamer.card.game.price.IStoreUpdator;
import dreamer.card.game.storage.ICardStore;
import dreamer.card.game.storage.IFilteredCardStore;
import dreamer.card.game.storage.IStorage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseMtgFanaticPrices implements IStoreUpdator, IPriceProvider {

    String baseURL;
    String setURL;

    public ParseMtgFanaticPrices() {
        // http://www.mtgfanatic.com/store/viewproducts.aspx?CatID=217&AffiliateID=44349&PageSize=500
        baseURL = "http://www.mtgfanatic.com/store/magic/viewcards.aspx?CatID=SET&ForumReferrerID=44349&PageSize=500";
        setURL = "http://www.mtgfanatic.com/Store/Magic/BasicSearch.aspx?CatID=3";
    }
    
    @Override
    public void updateStore(IFilteredCardStore<ISellableCard> fstore) throws IOException {
        updateStore(fstore.getCardStore(), fstore, fstore.getSize());
    }

    @Override
    public void updateStore(ICardStore<ISellableCard> store, Iterable<ISellableCard> iterable, int size)
            throws IOException {
        if (iterable == null) {
            iterable = store;
            size = store.size();
        }
        HashSet<String> sets = new HashSet();
        for (Iterator<ISellableCard> it = iterable.iterator(); it.hasNext();) {
            ISellableCard magicCard = it.next();
            String set = magicCard.getSet();
            sets.add(set);
        }
        HashMap<String, String> parseSets = parseSets();
        for (String set : sets) {
            String id = findSetId(set, parseSets);
            if (id != null) {
                HashMap<String, Float> prices = parse(id);
                if (prices.size() > 0) {
                    IStorage storage = store.getStorage();
                    storage.setAutoCommit(false);
                    try {
                        for (Iterator<ISellableCard> it = iterable.iterator(); it.hasNext();) {
                            ISellableCard magicCard = it.next();
                            String set2 = magicCard.getSet();
                            if (set2.equals(set)) {
                                if (prices.containsKey(magicCard.getName())) {
                                    Float price = prices.get(magicCard.getName());
                                    magicCard.setDbPrice(price);
                                    store.update(magicCard);
                                }
                            }
                        }
                    } finally {
                        storage.setAutoCommit(true);
                        storage.save();
                    }
                }
            }
        }
    }

    private String findSetId(String set, HashMap<String, String> parseSets) {
        if (set.contains("vs.")) {
            set = set.replaceAll("vs\\.", "vs");
        }
        String id = parseSets.get(set);
        if (id != null) {
            return id;
        }
        if (set.equals("Ravnica: City of Guilds")) {
            return parseSets.get("Ravnica");
        }
        if (set.equals("Judgment")) {
            return parseSets.get("Judgement");
        }
        if (set.equals("Tenth Edition")) {
            return parseSets.get("10th Edition");
        }
        if (set.equals("Ninth Edition")) {
            return parseSets.get("9th Edition");
        }
        if (set.equals("Eighth Edition")) {
            return parseSets.get("8th Edition");
        }
        if (set.equals("Seventh Edition")) {
            return parseSets.get("7th Edition");
        }
        if (set.equals("Classic Sixth Edition")) {
            return parseSets.get("6th Edition");
        }
        if (set.equals("Fifth Edition")) {
            return parseSets.get("5th Edition");
        }
        if (set.equals("Fourth Edition")) {
            return parseSets.get("4th Edition");
        }
        if (set.equals("Portal Second Age")) {
            return parseSets.get("Portal: Second Age");
        }
        if (set.equals("Portal Three Kingdoms")) {
            return parseSets.get("Portal: Three Kingdoms");
        }
        if (set.equals("Limited Edition Alpha")) {
            return parseSets.get("Alpha");
        }
        if (set.equals("Limited Edition Beta")) {
            return parseSets.get("Beta");
        }
        if (set.equals("Time Spiral \"Timeshifted\"")) {
            return parseSets.get("Time Spiral (Timeshifted)");
        }
        if (set.endsWith(" Box Set")) {
            return parseSets.get(set.replaceAll(" Box Set", ""));
        }
        if (set.endsWith(" Edition")) {
            return parseSets.get(set.replaceAll(" Edition", ""));
        }
        System.err.println("Cannot find prices for " + set);
        return null;
    }

    public HashMap<String, Float> parse(String setId) throws IOException {
        HashMap<String, Float> res = new HashMap<String, Float>();
        URL url = new URL(baseURL.toString().replace("SET", setId));
        InputStream openStream = url.openStream();
        BufferedReader st = new BufferedReader(new InputStreamReader(openStream));
        processFile(st, res);
        st.close();
        return res;
    }

    /*-
     <tr class="altDataRow" id="r93">
     <td rowspan="1"><a href="javascript:AddToCart('MTG-PS-049062', 1, 93, 0, 0);">Buy</a></td><td rowspan="1"><input name="q93" type="text" id="q93" style="width:50px;" /></td><td rowspan="1">$.74</td><td rowspan="1">4</td><td rowspan="1">Played</td>
     </tr><tr class="dataRow" id="r94">
     <td rowspan="1"><a href="javascript:AddToCart('MTG-MS-049063', 1, 94, 0, 0);">Buy</a></td><td rowspan="1"><input name="q94" type="text" id="q94" style="width:50px;" /></td>
     <td rowspan="1">$1.99</td><td rowspan="1">14</td>
     <td rowspan="1">NM+/M</td>
     <td rowspan="2"><a href="/store/magic/viewcard.aspx?I=MTG-MS-049063">Sengir Vampire</a></td>
     <td rowspan="2"><a href="/store/magic/viewcard.aspx?I=MTG-MS-049063" alt="Sengir Vampire"><img src="http://www.mtgfanatic.com/images/CreateProductThumbnail.ashx?ID=50361&S=75" /></a></td>
     </tr>
     */
    private static final Pattern rowStart = Pattern.compile("<tr class=\"(alt)?[Dd]ataRow\" id=\"r\\d+\">");
    private static final Pattern rowEnd = Pattern.compile("</tr>");
    private static final Pattern pricePattern = Pattern.compile("<td rowspan=\"\\d+\">\\$(.*?)</td>");
    private static final Pattern namePattern = Pattern.compile("<a href=\"/store/magic/viewcard.aspx.I=MTG-MS-\\d+\">(.*?)</a>");

    private void processFile(BufferedReader st, HashMap<String, Float> res) throws IOException {
        String line;
        while ((line = st.readLine()) != null) {
            if (rowStart.matcher(line).find()) {
                String row = "";
                while ((line = st.readLine()) != null) {
                    if (rowEnd.matcher(line).find()) {
                        break;
                    }
                    row = row + line;
                }
                processRow(row, res);
            }
        }
    }

    private void processRow(String row, HashMap<String, Float> res) {
        Matcher matcher = namePattern.matcher(row);
        if (matcher.find()) {
            String name = matcher.group(1);
            Matcher pmatcher = pricePattern.matcher(row);
            if (pmatcher.find()) {
                String price = pmatcher.group(1);
                try {
                    float f = Float.parseFloat(price);
                    res.put(name, f);
                } catch (NumberFormatException e) {
                    return;
                }
            }
        }
    }

    /*
     * <li> <a href="/store/magic/viewcards.aspx?CatID=390"><div class="setIcon"><img
     * src="/images/magic/symbols/Magic2010_Common.gif" alt="Magic 2010 icon"
     * title="Magic 2010 icon" /></div>Magic 2010</a> </li>
     */
    private static final Pattern setLinePattern = Pattern.compile("class=\"setIcon\"");
    private static Pattern setItemPattern = Pattern.compile("<a href=\"/store/magic/viewcards.aspx\\?CatID=(\\d+)\">.*?</div>(.*?)</a>");

    public HashMap<String, String> parseSets() throws IOException {
        HashMap<String, String> res = new HashMap<String, String>();
        URL url = new URL(setURL);
        InputStream openStream = url.openStream();
        BufferedReader st = new BufferedReader(new InputStreamReader(openStream));
        processSetFile(st, res);
        st.close();
        return res;
    }

    private void processSetFile(BufferedReader st, HashMap<String, String> res) throws IOException {
        String line;
        while ((line = st.readLine()) != null) {
            if (setLinePattern.matcher(line).find()) {
                Matcher m = setItemPattern.matcher(line);
                while (m.find()) {
                    String id = m.group(1);
                    String name = m.group(2);
                    res.put(name, id);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "MTG Fanatic";
    }

    @Override
    public URL getURL() {
        try {
            return new URL("http://www.mtgfanatic.com");
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
