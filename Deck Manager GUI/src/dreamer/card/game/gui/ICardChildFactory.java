package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import dreamer.card.game.core.Tool;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardChildFactory extends ChildFactory<ICard> {

    private final ICardSet set;
    private static final Logger LOG = Logger.getLogger(ICardChildFactory.class.getName());
    private HashMap<ICard, ICardNode> cards = new HashMap<ICard, ICardNode>();

    ICardChildFactory(ICardSet set) {
        this.set = set;
    }

    @Override
    protected boolean createKeys(List<ICard> toPopulate) {
        if (cards.isEmpty()) {
            long start = System.currentTimeMillis();
            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForSet(set).iterator(); it.hasNext();) {
                ICard card = (ICard) it.next();
                if (!cards.containsKey(card)) {
                    cards.put(card, null);
                }
            }
            LOG.log(Level.INFO, "DB query for set: {1} took: {0} hits: {2}",
                    new Object[]{Tool.elapsedTime(start), set.getName(), cards.size()});
        }
        toPopulate.addAll(cards.keySet());
        return true;
    }

    @Override
    protected Node createNodeForKey(ICard card) {
        if (cards.containsKey(card) && cards.get(card) == null) {
            try {
                ICardNode node = new ICardNode(card, set.getGameName());
                cards.put(card, node);
            } catch (IntrospectionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return cards.get(card);
    }

    @Override
    protected Node[] createNodesForKey(ICard key) {
        long start = System.currentTimeMillis();
        Node[] nodes = new Node[]{createNodeForKey(key)};
        LOG.log(Level.INFO, "Creating nodes for card: {1} took {0}",
                new Object[]{Tool.elapsedTime(start), key.getName()});
        return nodes;
    }

    public void refresh() {
        refresh(true);
    }
}
