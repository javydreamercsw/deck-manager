package dreamer.card.game.gui.node.factory;

import com.reflexit.magiccards.core.model.ICardGame;
import dreamer.card.game.gui.node.ICardGameNode;
import dreamer.card.game.gui.node.actions.Reloadable;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class IGameChildFactory extends ChildFactory<ICardGame> implements Lookup.Provider {

    private static final Logger LOG = Logger.getLogger(IGameChildFactory.class.getName());
    private ArrayList<ICardGame> games = new ArrayList<ICardGame>();
    /**
     * The lookup for Lookup.Provider
     */
    private Lookup lookup;
    /**
     * The InstanceContent that keeps this entity's abilities
     */
    private InstanceContent instanceContent;

    public IGameChildFactory() {
        // Create an InstanceContent to hold abilities...
        instanceContent = new InstanceContent();
        // Create an AbstractLookup to expose InstanceContent contents...
        lookup = new AbstractLookup(instanceContent);
        // Add a "Reloadable" ability to this entity
        instanceContent.add(new Reloadable() {
            @Override
            public void reload() throws Exception {
                for (Iterator<? extends ICardGame> it = Lookup.getDefault().lookupAll(ICardGame.class).iterator(); it.hasNext();) {
                    ICardGame game = it.next();
                    if (!games.contains(game)) {
                        games.add(game);
                    }
                }
                LOG.log(Level.INFO, "Games found: {0}", games.size());
                Collections.sort(games, new Comparator<ICardGame>() {
                    @Override
                    public int compare(ICardGame o1, ICardGame o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }
        });
    }

    public void refresh() {
        refresh(false);
    }

    @Override
    protected boolean createKeys(List<ICardGame> toPopulate) {
        // The query node is reloadable, isn't it? Then just
        // get this ability from the lookup ...
        Reloadable r = getLookup().lookup(Reloadable.class);
        // ... and  use the ability
        if (r != null) {
            try {
                r.reload();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        toPopulate.addAll(games);
        return true;
    }

    @Override
    protected Node createNodeForKey(ICardGame game) {
        try {
            return new ICardGameNode(game, new ICardSetChildFactory(game));
        } catch (IntrospectionException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }
}
