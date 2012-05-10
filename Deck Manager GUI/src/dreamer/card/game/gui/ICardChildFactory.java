package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardChildFactory extends ChildFactory<ICard> {

    private final ICardSet set;
    private static final Logger LOG = Logger.getLogger(ICardChildFactory.class.getName());
    private ArrayList<ICard> cards = new ArrayList<ICard>();

    ICardChildFactory(ICardSet set) {
        this.set = set;
    }

    @Override
    protected boolean createKeys(List<ICard> toPopulate) {
        if (cards.isEmpty()) {
            refresh();
        }
        toPopulate.addAll(cards);
        return true;
    }

    @Override
    protected Node createNodeForKey(ICard card) {
        try {
            return new ICardNode(card, set.getGameName());
        } catch (IntrospectionException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    protected Node[] createNodesForKey(ICard key) {
        return new Node[]{createNodeForKey(key)};
    }

    public void refresh() {
        for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForSet(set).iterator(); it.hasNext();) {
            ICard card = (ICard) it.next();
            if(!cards.contains(card)){
                cards.add(card);
            }
        }
        refresh(true);
    }
}
