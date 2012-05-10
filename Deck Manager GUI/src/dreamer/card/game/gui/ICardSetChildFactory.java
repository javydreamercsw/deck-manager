package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
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
public class ICardSetChildFactory extends ChildFactory<ICardSet> {

    private final ICardGame game;
    private static final Logger LOG = Logger.getLogger(ICardSetChildFactory.class.getName());
    private ArrayList<ICardSet> sets = new ArrayList<ICardSet>();

    public ICardSetChildFactory(ICardGame game) {
        this.game = game;
    }

    @Override
    protected boolean createKeys(List<ICardSet> list) {
        if (sets.isEmpty()) {
            refresh();
        }
        list.addAll(sets);
        return true;
    }

    @Override
    protected Node createNodeForKey(ICardSet set) {
        try {
            return new ICardSetNode(set, new ICardChildFactory(set));
        } catch (IntrospectionException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    protected Node[] createNodesForKey(ICardSet key) {
        return new Node[]{createNodeForKey(key)};
    }

    public void refresh() {
        for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getSetsForGame(getGame()).iterator(); it.hasNext();) {
            ICardSet set = (ICardSet) it.next();
            if (!sets.contains(set)) {
                sets.add(set);
            }
        }
        refresh(true);
    }

    /**
     * @return the game
     */
    public ICardGame getGame() {
        return game;
    }
}
