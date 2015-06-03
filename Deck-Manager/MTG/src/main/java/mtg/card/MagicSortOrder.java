package mtg.card;

import com.reflexit.magiccards.core.model.AbstractSortOrder;
import com.reflexit.magiccards.core.model.ICardField;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class MagicSortOrder extends AbstractSortOrder {

    @Override
    public void setSortField(ICardField sortField, boolean accending) {
        MagicSortOrder sortOrder = this;
        MagicCardComparator elem = new MagicCardComparator(sortField, accending);
        if (sortOrder.contains(elem)) {
            sortOrder.remove(elem);
        }
        while (sortOrder.size() > 5) {
            sortOrder.remove(5);
        }
        sortOrder.push(elem);
    }
    private static final Logger LOG = Logger.getLogger(MagicSortOrder.class.getName());
}
