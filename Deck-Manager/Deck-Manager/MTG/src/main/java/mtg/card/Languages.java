package mtg.card;

import com.reflexit.magiccards.core.model.ISearchableProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.logging.Logger;

public class Languages implements ISearchableProperty {

    public static enum Language {

        ENGLISH(Locale.ENGLISH),
        RUSSIAN(new Locale("ru")),
        FRENCH(Locale.FRENCH),
        SPANISH(new Locale("es")),
        GERMAN(new Locale("de")),
        ITALIAN(new Locale("it")),
        PORTUGESE(new Locale("pt")),
        JAPANESE(Locale.JAPANESE),
        CHINESE("Chinese Standard", Locale.CHINESE);
        private String lang;
        private Locale locale;

        Language(Locale locale) {
            this.lang = name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
            this.locale = locale;
        }

        Language(String name, Locale locale) {
            this.lang = name;
            this.locale = locale;
        }

        public String getLang() {
            return lang;
        }
    }
    private LinkedHashMap names;

    public static String[] getLangValues() {
        Language[] values = Language.values();
        String[] res = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            Language language = values[i];
            res[i] = language.getLang();
        }
        return res;
    }
    private static Languages instance = new Languages();

    private Languages() {
        this.names = new LinkedHashMap();
        Language[] langs = Language.values();
        for (int i = 0; i < langs.length; i++) {
            Language lang = langs[i];
            add(lang.getLang());
        }
    }

    private void add(String string) {
        String id = getPrefConstant(string);
        this.names.put(id, string);
    }

    public static Languages getInstance() {
        return instance;
    }

    @Override
    public String getIdPrefix() {
        return FilterHelper.LANG;
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

    public static Locale getLocale(String language) {
        Language[] array = Language.values();
        for (int i = 0; i < array.length; i++) {
            Language l = array[i];
            if (l.getLang().equals(language)) {
                return l.locale;
            }
        }
        throw new IllegalArgumentException("Language Not Found");
    }
    private static final Logger LOG = Logger.getLogger(Languages.class.getName());
}
