package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardSet;
import dreamer.card.game.core.ICardDataManager;
import java.beans.IntrospectionException;
import java.util.List;
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

    ICardChildFactory(ICardSet set) {
        this.set = set;
    }

    @Override
    protected boolean createKeys(List<ICard> toPopulate) {
        toPopulate.addAll(Lookup.getDefault().lookup(ICardDataManager.class).getCardsForSet(set));
        return true;
    }

    @Override
    protected Node createNodeForKey(ICard card) {
        try {
            return new ICardNode(card, set);
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
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