package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.IGame;
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
public class IGameChildFactory extends ChildFactory<IGame>{
    
    public void refresh() {
        refresh(true);
    }

    @Override
    protected boolean createKeys(List<IGame> toPopulate) {
        toPopulate.addAll(Lookup.getDefault().lookup(ICardDataManager.class).getGames());
        return true;
    }
    
    @Override
    protected Node createNodeForKey(IGame game) {
        try {
            return new IGameNode(game, new ICardSetChildFactory(game));
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
        return null;
        }
    }
    
}
