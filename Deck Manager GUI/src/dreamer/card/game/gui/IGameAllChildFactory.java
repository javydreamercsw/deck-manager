package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
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
public class IGameAllChildFactory extends ChildFactory<ICardGame>{

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
            return new IGameAllNode(game, new ICardAllChildFactory(game));
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
        return null;
        }
    }
    
}
