package dreamer.card.game.gui.node;

import com.reflexit.magiccards.core.model.ICardGame;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class RootNode extends AbstractNode {

    private final ChildFactory<ICardGame> childFactory;

    /**
     * Creates a new instance of RootNode
     *
     * @param childFactory
     */
    public RootNode(ChildFactory<ICardGame> childFactory) {
        super(Children.create(childFactory, true));
        this.childFactory = childFactory;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    /**
     * @return the childFactory
     */
    public ChildFactory<ICardGame> getChildFactory() {
        return childFactory;
    }
}
