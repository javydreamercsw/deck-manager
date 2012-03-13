package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
import dreamer.card.game.core.Tool;
import java.awt.Image;
import java.beans.IntrospectionException;
import javax.swing.JFrame;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class IGameAllNode extends BeanNode {

    private final ICardGame game;

    public IGameAllNode(ICardGame game, ICardAllChildFactory childFactory) throws IntrospectionException {
        super(game, Children.create(childFactory, true), Lookups.singleton(game));
        setDisplayName(game.getName());
        this.game = game;
    }

    @Override
    public Image getIcon(int type) {
        return Tool.loadImage(new JFrame(), game.getGameIcon()).getImage();
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
