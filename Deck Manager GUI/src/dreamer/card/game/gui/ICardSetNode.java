package dreamer.card.game.gui;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardSet;
import dreamer.card.game.core.Tool;
import java.awt.Image;
import java.beans.IntrospectionException;
import java.io.IOException;
import javax.swing.JFrame;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
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

    @Override
    public Image getIcon(int type) {
        try {
            return Tool.loadImage(new JFrame(), Lookup.getDefault().lookup(ICardCache.class).getSetIcon(set)).getImage();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    @Override
    public boolean canDestroy() {
        return false;
    }
}
