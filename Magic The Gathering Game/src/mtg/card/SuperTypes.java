package mtg.card;

import dreamer.card.game.ISearchableProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

public class SuperTypes implements ISearchableProperty {

    private SuperTypes() {
        this.names = new LinkedHashMap();
        add("Artifact");
        add("Basic");
        add("Tribal");
        add("Legendary");
        add("Snow");
        add("Land");
    }
    static SuperTypes instance = new SuperTypes();
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
        return "supertypes";
    }

    public static SuperTypes getInstance() {
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

    private String getPrefConstant(String name) {
        return FilterHelper.getPrefConstant(getIdPrefix(), name);
    }

    @Override
    public String getNameById(String id) {
        return (String) this.names.get(id);
    }
}
