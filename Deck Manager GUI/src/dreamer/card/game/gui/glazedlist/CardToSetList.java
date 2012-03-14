package dreamer.card.game.gui.glazedlist;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import com.reflexit.magiccards.core.model.ICard;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class CardToSetList extends TransformedList<ICard, String> {

    public CardToSetList(EventList<ICard> source) {
        super(source);
        source.addListEventListener(CardToSetList.this);
    }
    
    /**
     * Gets the user at the specified index.
     */
    @Override
    public String get(int index) {
        ICard card = source.get(index);
        return card.getSetName();
    }

    @Override
    /**
     * {@inheritDoc}
     */
    protected boolean isWritable() {
        return false;
    }

    @Override
    public void listChanged(ListEvent<ICard> le) {
        updates.forwardEvent(le);
    }
}
