package mtg.card;

import com.reflexit.magiccards.core.model.Editions;
import java.util.ArrayList;
import java.util.Collection;

public class FilterHelper {

    private static final String PREFIX = "mtg.card";
    public static final String TEXT_POSTFIX = "text";
    public static final String NUMERIC_POSTFIX = "numeric";
    public static final String GROUP_FIELD = "group_field";
    public static final String TYPE_LINE = MagicCardField.TYPE.name();
    public static final String TEXT_LINE = MagicCardField.TEXT.name();
    public static final String NAME_LINE = MagicCardField.NAME.name();
    public static final String POWER = MagicCardField.POWER.name();
    public static final String TOUGHNESS = MagicCardField.TOUGHNESS.name();
    public static final String CCC = MagicCardField.CMC.name();
    public static final String EDITION = MagicCardField.SET.name();
    public static final String RARITY = MagicCardField.RARITY.name();
    public static final String PRICE = MagicCardFieldPhysical.PRICE.name();
    public static final String DBPRICE = MagicCardField.DBPRICE.name();
    public static final String COMMUNITYRATING = MagicCardField.RATING.name();
    public static final String ARTIST = MagicCardField.ARTIST.name();
    public static final String COUNT = MagicCardFieldPhysical.COUNT.name();
    public static final String COMMENT = MagicCardFieldPhysical.COMMENT.name();
    public static final String OWNERSHIP = MagicCardFieldPhysical.OWNERSHIP.name();
    public static final String LANG = MagicCardField.LANG.name();
    public static final String TEXT_LINE_2 = TEXT_LINE + "_2";
    public static final String TEXT_LINE_3 = TEXT_LINE + "_3";
    public static final String TEXT_NOT_1 = TEXT_LINE + "_exclude_1";
    public static final String TEXT_NOT_2 = TEXT_LINE + "_exclude_2";
    public static final String TEXT_NOT_3 = TEXT_LINE + "_exclude_3";
    public static final String COLLNUM = MagicCardField.COLLNUM.name();

    public static String escapeProperty(String string) {
        String res = string.toLowerCase();
        res = res.replaceAll("[^\\w-./]", "_");
        return res;
    }

    public static String getPrefConstant(String sub, String name) {
        return PREFIX + ".filter." + sub + "." + escapeProperty(name);
    }

    public static Collection getAllIds() {
        ArrayList ids = new ArrayList();
        ids.addAll(Colors.getInstance().getIds());
        ids.addAll(ColorTypes.getInstance().getIds());
        ids.addAll(CardTypes.getInstance().getIds());
        ids.addAll(SuperTypes.getInstance().getIds());
        ids.addAll(Editions.getInstance().getIds());
        ids.addAll(Rarity.getInstance().getIds());
        ids.add(FilterHelper.getPrefConstant(FilterHelper.TEXT_LINE, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.TYPE_LINE, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.NAME_LINE, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.POWER, FilterHelper.NUMERIC_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.TOUGHNESS, FilterHelper.NUMERIC_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.CCC, FilterHelper.NUMERIC_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.COUNT, FilterHelper.NUMERIC_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.PRICE, FilterHelper.NUMERIC_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.DBPRICE, FilterHelper.NUMERIC_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.COMMUNITYRATING, FilterHelper.NUMERIC_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.COLLNUM, FilterHelper.NUMERIC_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.ARTIST, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.COMMENT, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.OWNERSHIP, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.TEXT_LINE_2, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.TEXT_LINE_3, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.TEXT_NOT_1, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.TEXT_NOT_2, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.TEXT_NOT_3, FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(MagicCardFieldPhysical.FORTRADECOUNT.name(), FilterHelper.NUMERIC_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(MagicCardFieldPhysical.SPECIAL.name(), FilterHelper.TEXT_POSTFIX));
        ids.add(FilterHelper.getPrefConstant(FilterHelper.LANG, FilterHelper.TEXT_POSTFIX));
        return ids;
    }

    private FilterHelper() {
    }
}
