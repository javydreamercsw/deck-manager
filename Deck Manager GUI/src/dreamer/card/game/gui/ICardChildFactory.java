package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.beans.IntrospectionException;
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

    ICardChildFactory(ICardSet set) {
        this.set = set;
    }

    @Override
    protected boolean createKeys(List<ICard> toPopulate) {
        toPopulate.addAll(Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForSet(set));
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
        refresh(true);
    }
}
