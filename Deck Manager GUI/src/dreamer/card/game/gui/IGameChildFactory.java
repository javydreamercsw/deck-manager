package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
import dreamer.card.game.core.Tool;
import java.beans.IntrospectionException;
import java.util.ArrayList;
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
    private ArrayList<ICardGame> games = new ArrayList<ICardGame>();

    public void refresh() {
        refresh(true);
    }

    @Override
    protected boolean createKeys(List<ICardGame> toPopulate) {
        if (games.isEmpty()) {
            long start = System.currentTimeMillis();
            for (ICardGame game : Lookup.getDefault().lookupAll(ICardGame.class)) {
                if (!games.contains(game)) {
                    games.add(game);
                }
            }
            LOG.info(Tool.elapsedTime(start));
        }
        toPopulate.addAll(games);
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
