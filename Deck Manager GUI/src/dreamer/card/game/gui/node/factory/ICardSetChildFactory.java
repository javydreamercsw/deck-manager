package dreamer.card.game.gui.node.factory;

import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import dreamer.card.game.core.Tool;
import dreamer.card.game.gui.node.ICardSetNode;
import dreamer.card.game.gui.node.actions.Reloadable;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardSetChildFactory extends ChildFactory<ICardSet> implements Lookup.Provider {

    private final ICardGame game;
    private static final Logger LOG = Logger.getLogger(ICardSetChildFactory.class.getName());
    private final List<ICardSet> sets = new ArrayList<ICardSet>();
    /**
     * The lookup for Lookup.Provider
     */
    private Lookup lookup;
    /**
     * The InstanceContent that keeps this entity's abilities
     */
    private InstanceContent instanceContent;

    public ICardSetChildFactory(final ICardGame game) {
        this.game = game;
        // Create an InstanceContent to hold abilities...
        instanceContent = new InstanceContent();
        // Create an AbstractLookup to expose InstanceContent contents...
        lookup = new AbstractLookup(instanceContent);
        // Add a "Reloadable" ability to this entity
        instanceContent.add(new Reloadable() {
            @Override
            public void reload() throws Exception {
                synchronized (sets) {
                    long start = System.currentTimeMillis();
                    for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getSetsForGame(getGame()).iterator(); it.hasNext();) {
                        ICardSet set = (ICardSet) it.next();
                        if (!sets.contains(set)) {
                            sets.add(set);
                        }
                    }
                    LOG.log(Level.FINE, "DB query for Game: {1} took: {0} hits: {2}",
                            new Object[]{Tool.elapsedTime(start), game.getName(), sets.size()});
                }
            }
        });
    }

    @Override
    protected boolean createKeys(final List<ICardSet> list) {
        long start = System.currentTimeMillis();
        // The node is reloadable, isn't it? Then just
        // get this ability from the lookup ...
        Reloadable r = getLookup().lookup(Reloadable.class);
        // ... and  use the ability
        int size = sets.size();
        if (r != null) {
            try {
                r.reload();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        list.addAll(sets);
        LOG.log(Level.FINE, "Creating keys for Game: {1} took: {0}",
                new Object[]{Tool.elapsedTime(start), game.getName()});
        return size == sets.size();
    }

    @Override
    protected Node createNodeForKey(ICardSet set) {
        try {
            long start = System.currentTimeMillis();
            ICardSetNode node = new ICardSetNode(set, new ICardChildFactory(set));
            LOG.log(Level.FINE, "Creating node for set: {1} took {0}",
                    new Object[]{Tool.elapsedTime(start), set.getName()});
            return node;
        } catch (IntrospectionException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return new AbstractNode(Children.LEAF);
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

    @Override
    public Lookup getLookup() {
        return lookup;
    }
}
