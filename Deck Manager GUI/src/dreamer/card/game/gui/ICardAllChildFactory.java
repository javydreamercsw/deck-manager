package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import dreamer.card.game.core.ICardDataManager;
import dreamer.card.game.core.ui.ICardUI;
import java.beans.IntrospectionException;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ICardAllChildFactory extends ChildFactory<ICardUI> {

    private final ICardGame game;

    public ICardAllChildFactory(ICardGame game) {
        this.game = game;
    }

    @Override
    protected boolean createKeys(List<ICardUI> toPopulate) {
        for(ICard card:Lookup.getDefault().lookup(ICardDataManager.class).getCardsForGame(game)){
            toPopulate.add((ICardUI)card);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(ICardUI card) {
        try {
            return new ICardNode(card, game.getName());
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    protected Node[] createNodesForKey(ICardUI key) {
        return new Node[]{createNodeForKey(key)};
    }

    public void refresh() {
        refresh(true);
    }
}
