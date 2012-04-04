package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.beans.IntrospectionException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ICardAllChildFactory extends ChildFactory<ICard> {

    private final ICardGame game;
    private static final Logger LOG = Logger.getLogger(ICardAllChildFactory.class.getName());

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
