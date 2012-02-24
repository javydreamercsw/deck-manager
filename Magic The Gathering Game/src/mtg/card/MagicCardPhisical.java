package mtg.card;

import dreamer.card.game.ICard;
import dreamer.card.game.ICardCountable;
import dreamer.card.game.ICardField;
import dreamer.card.game.ICardModifiable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import mtg.card.MagicCardFilter.TextValue;

public class MagicCardPhisical implements IMagicCard, ICardCountable, ICardModifiable {

    private MagicCard card;
    private int count;
    private float price;
    private String comment;
    private String custom;
    private boolean ownership;
    private int forTrade;
    private String special;

    public MagicCardPhisical(ICard card) {
        if (card instanceof MagicCard) {
            this.card = (MagicCard) card;
            this.count = 1;
            this.ownership = false;
            this.forTrade = 0;
            this.special = null;
        } else if (card instanceof MagicCardPhisical) {
            MagicCardPhisical phi = (MagicCardPhisical) card;
            this.card = phi.getCard();
            this.count = phi.getCount();
            this.comment = phi.getComment();
            this.custom = phi.getCustom();
            this.price = phi.getPrice();
            this.ownership = phi.ownership;
            this.forTrade = phi.forTrade;
            this.special = phi.special;
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public ICard cloneCard() {
        return (IMagicCard) clone();
    }

    @Override
    public float getDbPrice() {
        return card.getDbPrice();
    }

    @Override
    public float getCommunityRating() {
        return card.getCommunityRating();
    }

    @Override
    public String getArtist() {
        return card.getArtist();
    }

    @Override
    public String getRulings() {
        return card.getRulings();
    }

    public Collection getHeaderNames() {
        ArrayList list = new ArrayList();
        list.addAll(this.card.getHeaderNames());
        MagicCardFieldPhysical[] values = MagicCardFieldPhysical.values();
        for (MagicCardFieldPhysical magicCardField : values) {
            list.add(magicCardField.toString());
        }
        return list;
    }

    public MagicCard getCard() {
        return this.card;
    }

    public void setMagicCard(MagicCard card) {
        this.card = card;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getPrice() {
        return this.price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getComment() {
        if (this.comment == null) {
            return "";
        }
        return this.comment;
    }

    public void setComment(String comment) {
        if (comment == null || comment.trim().length() == 0) {
            this.comment = null;
        } else {
            this.comment = comment.trim();
        }
    }

    public String getCustom() {
        return this.custom;
    }

    public void setCustom(String cutom) {
        this.custom = cutom;
    }

    @Override
    public int getCardId() {
        return this.card.getCardId();
    }

    @Override
    public int getCmc() {
        return this.card.getCmc();
    }

    @Override
    public String getColorType() {
        return this.card.getColorType();
    }

    @Override
    public String getCost() {
        return this.card.getCost();
    }

    @Override
    public String getSet() {
        return this.card.getSet();
    }

    @Override
    public String getName() {
        return this.card.getName();
    }

    @Override
    public String getOracleText() {
        return this.card.getOracleText();
    }

    @Override
    public String getPower() {
        return this.card.getPower();
    }

    @Override
    public String getRarity() {
        return this.card.getRarity();
    }

    @Override
    public String getToughness() {
        return this.card.getToughness();
    }

    @Override
    public String getType() {
        return this.card.getType();
    }

    public boolean isOwn() {
        return this.ownership;
    }

    public void setOwn(boolean ownership) {
        this.ownership = ownership;
    }

    @Override
    public int hashCode() {
        return this.card.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MagicCardPhisical) {
            MagicCardPhisical phi = (MagicCardPhisical) obj;
            if (this.getCount() != phi.getCount()) {
                return false;
            }
            if (!this.matching(phi)) {
                return false;
            }
            return true;
        }
        if (obj instanceof ICard) {
            return this.card.equals(obj);
        }
        return false;
    }

    /**
     * Kind of equals by ignores count and location
     *
     * @param phi2
     * @return
     */
    public boolean matching(MagicCardPhisical phi2) {
        MagicCardPhisical phi1 = this;
        if (!phi1.card.equals(phi2.card)) {
            return false;
        }
        if (phi1.isOwn() != phi2.isOwn()) {
            return false;
        }
        if (Math.abs(phi1.price - phi2.price) >= 0.01) {
            return false;
        }
        if (!eqNull(phi1.getCustom(), phi2.getCustom())) {
            return false;
        }
        if (!eqNull(phi1.getComment(), phi2.getComment())) {
            return false;
        }
        return true;
    }

    public static boolean eqNull(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    @Override
    public String toString() {
        return this.card.toString() + " x " + this.count;
    }

    @Override
    public boolean setObjectByField(ICardField field, String value) {
        boolean is = card.setObjectByField(field, value);
        if (is == true) {
            return true;
        }
        if (!(field instanceof MagicCardFieldPhysical)) {
            return false;
        }
        MagicCardFieldPhysical pfield = (MagicCardFieldPhysical) field;
        switch (pfield) {
            case COUNT:
                setCount(Integer.parseInt(value));
                break;
            case PRICE:
                setPrice(Float.parseFloat(value));
                break;
            case COMMENT:
                setComment(value);
                break;
            case CUSTOM:
                setCustom(value);
                break;
            case OWNERSHIP:
                setOwn(Boolean.parseBoolean(value));
                break;
            case SPECIAL:
                setSpecial(value);
                break;
            case FORTRADECOUNT:
                setForTrade(Integer.parseInt(value));
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public Object getObjectByField(ICardField field) {
        Object x = card.getObjectByField(field);
        if (x != null) {
            return x;
        }
        if (!(field instanceof MagicCardFieldPhysical)) {
            return null;
        }
        MagicCardFieldPhysical pfield = (MagicCardFieldPhysical) field;
        switch (pfield) {
            case COUNT:
                return getCount();
            case PRICE:
                return getPrice();
            case COMMENT:
                return getComment();
            case CUSTOM:
                return getCustom();
            case OWNERSHIP:
                return isOwn();
            case FORTRADECOUNT:
                return getForTrade();
            case SPECIAL:
                return getSpecial();
        }
        return null;
    }

    public void setCommunityRating(float parseFloat) {
        card.setCommunityRating(parseFloat);
    }

    public void setArtist(String artist) {
        card.setArtist(artist);
    }

    public void setRulings(String rulings) {
        card.setRulings(rulings);
    }

    public int getForTrade() {
        return forTrade;
    }

    public void setForTrade(int forSale) {
        this.forTrade = forSale;
    }

    public String getSpecial() {
        if (this.special == null) {
            return "";
        }
        return special;
    }

    public void setSpecial(String special) {
        if (special == null || special.trim().length() == 0) {
            this.special = null;
        } else {
            String value = getSpecial();
            String tags[] = special.trim().split(",");
            boolean add = false;
            for (String tag : tags) {
                tag = tag.trim();
                if (tag.length() == 0) {
                    continue;
                }
                if (tag.startsWith("+")) {
                    tag = tag.substring(1);
                    value = addTag(value, tag);
                    add = true;
                } else if (tag.startsWith("-")) {
                    tag = tag.substring(1);
                    value = removeTag(value, tag);
                    add = true;
                } else {
                    if (add) {
                        addTag(value, tag);
                    } else {
                        value = tag + ",";
                    }
                }
            }
            this.special = value;
        }
    }

    protected String addTag(String value, String tag) {
        if (!containsTag(value, tag)) {
            value += tag + ",";
        }
        return value;
    }

    protected String removeTag(String value, String a) {
        String res = "";
        String tags[] = value.split(",");
        for (String tag : tags) {
            if (!tag.equals(a)) {
                res = res + tag + ",";
            }
        }
        return res;
    }

    private boolean containsTag(String value, String a) {
        String tags[] = value.split(",");
        for (String tag : tags) {
            if (tag.equals(a)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setDbPrice(float dbprice) {
        getCard().setDbPrice(dbprice);
    }

    @Override
    public MagicCard getBase() {
        return card;
    }

    @Override
    public String getText() {
        return card.getText();
    }

    @Override
    public String getLanguage() {
        return card.getLanguage();
    }

    @Override
    public boolean matches(ICardField left, TextValue right) {
        return card.matches(left, right);
    }

    @Override
    public int getEnglishCardId() {
        return card.getEnglishCardId();
    }

    @Override
    public int getFlipId() {
        return card.getFlipId();
    }

    @Override
    public List<ImageIcon> getImages() {
        return new ArrayList<ImageIcon>();
    }

    @Override
    public int compareTo(Object o) {
        return new MagicCardComparator().compare(this, o);
    }
    private static final Logger LOG = Logger.getLogger(MagicCardPhisical.class.getName());
}
