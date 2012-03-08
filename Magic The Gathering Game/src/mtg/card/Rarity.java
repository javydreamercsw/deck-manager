package mtg.card;

import com.reflexit.magiccards.core.model.ISearchableProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

public class Rarity implements ISearchableProperty {

    public static final String COMMON = "Common";
    public static final String UNCOMMON = "Uncommon";
    public static final String RARE = "Rare";
    public static final String MYTHIC_RARE = "Mythic Rare";

    private Rarity() {
        this.names = new LinkedHashMap();
        add(MYTHIC_RARE);
        add(RARE);
        add(UNCOMMON);
        add(COMMON);
        add("Land");
        add("Other");
    }
    static Rarity instance = new Rarity();
    private LinkedHashMap names;

    private void add(String string) {
        String id = getPrefConstant(string);
        this.names.put(id, string);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.reflexit.magiccards.core.model.ISearchableProperty#getIdPrefix()
     */
    @Override
    public String getIdPrefix() {
        return FilterHelper.RARITY;
    }

    public static Rarity getInstance() {
        return instance;
    }

    @Override
    public Collection getNames() {
        return new ArrayList(this.names.values());
    }

    @Override
    public Collection getIds() {
        return new ArrayList(this.names.keySet());
    }

    public String getPrefConstant(String name) {
        return FilterHelper.getPrefConstant(getIdPrefix(), name);
    }

    @Override
    public String getNameById(String id) {
        return (String) this.names.get(id);
    }

    /**
     * @param a1
     * @param a2
     * @return
     */
    public static int compare(String r1, String r2) {
        Collection values = getInstance().names.values();
        int i1 = values.size() - 1, i2 = i1, i = 0;
        for (Iterator iterator = values.iterator(); iterator.hasNext(); i++) {
            String v = (String) iterator.next();
            if (r1.equals(v)) {
                i1 = i;
            }
            if (r2.equals(v)) {
                i2 = i;
            }
        }
        return i2 - i1;
    }
    private static final Logger LOG = Logger.getLogger(Rarity.class.getName());
}
