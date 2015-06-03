package mtg.card;

import com.reflexit.magiccards.core.model.ICardField;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public enum MagicCardField implements ICardField {

    ID,
    NAME,
    COST,
    TYPE,
    POWER,
    TOUGHNESS,
    ORACLE("oracleText"),
    SET("edition"),
    RARITY,
    CTYPE("colorType"),
    CMC,
    DBPRICE,
    LANG,
    EDITION_ABBR(null),
    RATING,
    ARTIST,
    COLLNUM("num"), // collector number i.e. 5/234
    RULINGS,
    TEXT,
    ENID("enId"),
    PROPERTIES,
    FLIPID(null),
    PART(null),
    OTHER_PART(null),
    DUAL_ID(null);
    private final Field field;

    MagicCardField(String javaField) {
        if (javaField != null) {
            try {
                field = MagicCard.class.getDeclaredField(javaField);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(e);
            } catch (SecurityException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            field = null;
        }
    }

    MagicCardField() {
        String javaField = name().toLowerCase();
        try {
            field = MagicCard.class.getDeclaredField(javaField);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Class getType() {
        return field == null ? String.class : field.getClass();
    }

    @Override
    public boolean isTransient() {
        return field == null ? true : Modifier.isTransient(field.getModifiers());
    }

    public static ICardField[] allFields() {
        MagicCardField[] values = MagicCardField.values();
        return values;
    }

    public static ICardField[] allNonTransientFields() {
        MagicCardField[] values = MagicCardField.values();
        ArrayList<ICardField> res = new ArrayList<ICardField>();
        for (MagicCardField f : values) {
            if (!f.isTransient()) {
                res.add(f);
            }
        }
        return res.toArray(new ICardField[res.size()]);
    }

    @Override
    public Field getJavaField() {
        return field;
    }
}
