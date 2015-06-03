package mtg.card;

import com.reflexit.magiccards.core.model.ISearchableProperty;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class CardTypes implements ISearchableProperty {

    public static ResourceBundle TYPES = ResourceBundle.getBundle("mtg.card.locale.CardText.properties");

    private CardTypes() {
        this.names = new LinkedHashMap();
        add(TYPES.getString("Type_Land"));
        add(TYPES.getString("Type_Instant"));
        add(TYPES.getString("Type_Sorcery"));
        add(TYPES.getString("Type_Creature"));
        add(TYPES.getString("Type_Enchantment"));
        add(TYPES.getString("Type_Artifact"));
        add(TYPES.getString("Type_Planeswalker"));
    }
    static CardTypes instance = new CardTypes();
    private final LinkedHashMap names;

    private void add(String string) {
        String id = getPrefConstant(string);
        this.names.put(id, string);
    }

    public boolean hasType(IMagicCard card, String type) {
        String typeText = card.getType();
        if (containsType(typeText, type)) {
            return true;
        }
        String language = card.getLanguage();
        if (language != null && language.length() > 0) {
            String localizedType = TYPES.getString(type);
            if (localizedType != null && containsType(typeText, localizedType)) {
                return true;
            }
        }
        if (type == null ? TYPES.getString("Type_Creature") == null : type.equals(TYPES.getString("Type_Creature"))) {
            return hasType(card, TYPES.getString("Type_Summon"));
        }
        if (type.equals(TYPES.getString("Type_Instant"))) {
            return hasType(card, TYPES.getString("Type_Instant"));
        }
        return false;
    }

    private boolean containsType(String text, String type) {
        return Pattern.compile(MessageFormat.format("\bQ{0}E\b", type), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(text).find();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.reflexit.magiccards.core.model.ISearchableProperty#getIdPrefix()
     */
    @Override
    public String getIdPrefix() {
        return "types";
    }

    public static CardTypes getInstance() {
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

    public String getLocalizedNameById(String id) {
        String enName = getNameById(id);
        return TYPES.getString(enName);
    }
    public static String[] proposals = new String[]{//
        "Basic", "Tribal", "World", "Legendary", "Snow",
        //
        "Land", "Artifact", "Creature",
        //
        "Elf", "Goblin", "Human", "Elemental", "Kithkin"};

    /**
     * @return
     */
    public static String[] getProposals() {
        return proposals;
    }

    public Collection<String> getLocalizedNames() {
        ArrayList<String> names2 = new ArrayList<String>();
        for (Iterator iterator = names.values().iterator(); iterator.hasNext();) {
            String string = (String) iterator.next();
            names2.add(TYPES.getString(string));
        }
        return names2;
    }
    private static final Logger LOG = Logger.getLogger(CardTypes.class.getName());
}
