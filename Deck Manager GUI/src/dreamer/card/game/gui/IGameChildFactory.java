package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
import java.beans.IntrospectionException;
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
public class IGameChildFactory extends ChildFactory<ICardGame> {

    private static final Logger LOG = Logger.getLogger(IGameChildFactory.class.getName());

    public void refresh() {
        refresh(true);
    }

    @Override
    protected boolean createKeys(List<ICardGame> toPopulate) {
        toPopulate.addAll(Lookup.getDefault().lookupAll(ICardGame.class));
        return true;
    }

    @Override
    protected Node createNodeForKey(ICardGame game) {
        try {
            return new IGameNode(game, new ICardSetChildFactory(game));
        } catch (IntrospectionException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
