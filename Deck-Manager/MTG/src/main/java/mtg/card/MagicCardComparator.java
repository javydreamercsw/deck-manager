package mtg.card;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardComparator;
import com.reflexit.magiccards.core.model.ICardField;
import java.util.logging.Logger;

class MagicCardComparator implements ICardComparator {

    private final ICardField field;
    private final boolean accending;

    public MagicCardComparator(ICardField sortField, boolean accending) {
        this.field = sortField;
        this.accending = accending;
    }

    public MagicCardComparator() {
        this.field = MagicCardField.NAME;
        this.accending = true;
    }

    @Override
    public ICardField getField() {
        return field;
    }

    @Override
    public boolean isAccending() {
        return accending;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MagicCardComparator other = (MagicCardComparator) obj;
        if (field == null) {
            if (other.field != null) {
                return false;
            }
        } else if (!field.equals(other.field)) {
            return false;
        }
        return true;
    }

    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == o2) {
            return 0;
        }
        if (o1 instanceof IMagicCard && o2 instanceof IMagicCard) {
            IMagicCard c1 = (IMagicCard) o1;
            IMagicCard c2 = (IMagicCard) o2;
            return compare(c1, c2);
        }
        return 0;
    }

    @Override
    public int compare(ICard c1, ICard c2) {
        if (c1 == c2) {
            return 0;
        }
        int dir = accending ? 1 : -1;
        ICardField sort = field;
        ICardField sby = sort;
        Object a1 = c1.getObjectByField(sby);
        Object a2 = c2.getObjectByField(sby);
        int d = 0;
        if (sort == MagicCardField.COST) {
            a1 = Colors.getColorSort((String) a1);
            a2 = Colors.getColorSort((String) a2);
        }
        if (a1 == null && a2 != null) {
            d = 1;
        } else if (a1 != null && a2 == null) {
            d = -1;
        } else if (sort == MagicCardField.POWER || sort == MagicCardField.TOUGHNESS) {
            float f1 = MagicCard.convertFloat((String) a1);
            float f2 = MagicCard.convertFloat((String) a2);
            d = Float.compare(f1, f2);
        } else if (sort == MagicCardField.RARITY) {
            d = Rarity.compare((String) a1, (String) a2);
        } else if (sort == MagicCardField.COLLNUM && a1 instanceof String) {
            String s1 = (String) a1;
            String s2 = (String) a2;
            try {
                d = Integer.valueOf(s1) - Integer.valueOf(s2);
            } catch (NumberFormatException e) {
                d = s1.compareTo(s2);
            }
        } else if (a1 instanceof Comparable) {
            if (a2 == null) {
                d = 1;
            } else {
                d = ((Comparable) a1).compareTo(a2);
            }
        }
        if (d == 0 && sort == MagicCardField.CMC) {
            int d1 = Colors.getColorSort((String) c1.getObjectByField(MagicCardField.COST));
            int d2 = Colors.getColorSort((String) c2.getObjectByField(MagicCardField.COST));
            d = d1 - d2;
        }
        // if (d == 0 && c1.getCardId() != 0) {
        // d = c1.getCardId() - c2.getCardId();
        // }
        if (d != 0) {
            return dir * d;
        }
        // return this.dir * (System.identityHashCode(o1) -
        // System.identityHashCode(o2));
        return 0;
    }
    private static final Logger LOG = Logger.getLogger(MagicCardComparator.class.getName());
}
