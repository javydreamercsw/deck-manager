package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
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
public class ICardSetChildFactory extends ChildFactory<ICardSet> {

    private final ICardGame game;

    public ICardSetChildFactory(ICardGame game) {
        this.game = game;
    }

    @Override
    protected boolean createKeys(List<ICardSet> list) {
        list.addAll(Lookup.getDefault().lookup(ICardDataManager.class).getSetsForGame(getGame()));
        return true;
    }

    @Override
    protected Node createNodeForKey(ICardSet set) {
        try {
            return new ICardSetNode(set, new ICardChildFactory(set));
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
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
