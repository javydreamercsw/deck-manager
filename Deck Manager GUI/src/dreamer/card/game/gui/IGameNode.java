package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.IGame;
import java.beans.IntrospectionException;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class IGameNode extends BeanNode {
    private final IGame game;

    public IGameNode(IGame game, ICardSetChildFactory childFactory) throws IntrospectionException {
        super(game, Children.create(childFactory, true), Lookups.singleton(game));
        setDisplayName(game.getName());
        this.game = game;
    }

    /**
     * @return the game
     */
    public IGame getGame() {
        return game;
    }
}
