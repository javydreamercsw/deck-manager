package mtg.card;

import com.reflexit.magiccards.core.model.ICardField;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Fields for actual player card
 */
public enum MagicCardFieldPhysical implements ICardField {

    COUNT,
    PRICE,
    COMMENT,
    CUSTOM,
    OWNERSHIP,
    FORTRADECOUNT("forTrade"),
    SPECIAL, // like foil, premium, mint, played, online etc
    ;
    // fields
    private final Field field;

    MagicCardFieldPhysical(String javaField) {
        if (javaField != null) {
            try {
                field = MagicCardPhisical.class.getDeclaredField(javaField);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(e);
            } catch (SecurityException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            field = null;
        }
    }

    MagicCardFieldPhysical() {
        String javaField = name().toLowerCase(Locale.ENGLISH);
        try {
            field = MagicCardPhisical.class.getDeclaredField(javaField);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Class getType() {
        if (field == null) {
            return null;
        }
        return field.getClass();
    }

    @Override
    public boolean isTransient() {
        if (field == null) {
            return true;
        }
        return Modifier.isTransient(field.getModifiers());
    }

    public static ICardField[] allFields() {
        MagicCardField[] values = MagicCardField.values();
        MagicCardFieldPhysical[] values2 = values();
        ICardField[] res = new ICardField[values.length + values2.length];
        System.arraycopy(values, 0, res, 0, values.length);
        System.arraycopy(values2, 0, res, values.length, values2.length);
        return res;
    }

    public static ICardField[] allNonTransientFields() {
        MagicCardField[] values = MagicCardField.values();
        MagicCardFieldPhysical[] values2 = values();
        ArrayList<ICardField> res = new ArrayList<ICardField>();
        for (MagicCardField f : values) {
            if (!f.isTransient()) {
                res.add(f);
            }
        }
        for (MagicCardFieldPhysical f : values2) {
            if (!f.isTransient()) {
                res.add(f);
            }
        }
        return res.toArray(new ICardField[res.size()]);
    }

    public static ICardField fieldByName(String field) {
        try {
            MagicCardFieldPhysical p = valueOf(field);
            if (p != null) {
                return p;
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            MagicCardField f = MagicCardField.valueOf(field);
            if (f != null) {
                return f;
            }
        } catch (Exception e) {
            // ignore
        }
        // aliases
        if (field.equals("EDITION")) {
            return MagicCardField.SET;
        }
        if (field.equals("QTY")) {
            return MagicCardFieldPhysical.COUNT;
        }
        return null;
    }

    @Override
    public Field getJavaField() {
        return field;
    }

    public static ICardField[] toFields(String line, String sep) {
        String split[] = line.split(sep);
        ICardField res[] = new ICardField[split.length];
        for (int i = 0; i < split.length; i++) {
            String string = split[i];
            ICardField field = MagicCardFieldPhysical.fieldByName(string);
            res[i] = field;
        }
        return res;
    }
}
