package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
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
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardSetChildFactory extends ChildFactory<ICardSet> {

    private final ICardGame game;
    private static final Logger LOG = Logger.getLogger(ICardSetChildFactory.class.getName());
    private HashMap<ICardSet, ICardSetNode> sets = new HashMap<ICardSet, ICardSetNode>();

    public ICardSetChildFactory(ICardGame game) {
        this.game = game;
    }

    @Override
    protected boolean createKeys(List<ICardSet> list) {
        if (sets.isEmpty()) {
            long start = System.currentTimeMillis();
            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getSetsForGame(getGame()).iterator(); it.hasNext();) {
                ICardSet set = (ICardSet) it.next();
                if (!sets.containsKey(set)) {
                    sets.put(set, null);
                }
            }
            LOG.log(Level.INFO, "DB query for Game: {1} took: {0} hits: {2}",
                    new Object[]{Tool.elapsedTime(start), game.getName(), sets.size()});
        }
        list.addAll(sets.keySet());
        return true;
    }

    @Override
    protected Node createNodeForKey(ICardSet set) {
        try {
            if (sets.containsKey(set) && sets.get(set) == null) {
                long start = System.currentTimeMillis();
                ICardSetNode node = new ICardSetNode(set, new ICardChildFactory(set));
                LOG.log(Level.INFO, "Creating node for set: {1} took {0}",
                        new Object[]{Tool.elapsedTime(start), set.getName()});
                sets.put(set, node);
            }
            return sets.get(set);
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
        refresh(true);
    }

    /**
     * @return the game
     */
    public ICardGame getGame() {
        return game;
    }
}
