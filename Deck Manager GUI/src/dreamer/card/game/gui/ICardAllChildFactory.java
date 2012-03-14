package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.beans.IntrospectionException;
import java.util.Iterator;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ICardAllChildFactory extends ChildFactory<ICard> {

    private final ICardGame game;

    public ICardAllChildFactory(ICardGame game) {
        this.game = game;
    }

    @Override
    protected boolean createKeys(List<ICard> toPopulate) {
        for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForGame(game).iterator(); it.hasNext();) {
            ICard card = (ICard) it.next();
            toPopulate.add((ICard) card);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(ICard card) {
        try {
            return new ICardNode(card, game.getName());
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
