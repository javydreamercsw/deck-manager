package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardSet;
import java.beans.IntrospectionException;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardSetNode extends BeanNode {

    private final ICardSet set;

    public ICardSetNode(ICardSet set, ICardChildFactory childFactory) throws IntrospectionException {
        super(set, Children.create(childFactory, true), Lookups.singleton(set));
        setDisplayName(set.getName());
        this.set = set;
    }

    /**
     * @return the set
     */
    public ICardSet getSet() {
        return set;
    }
}
