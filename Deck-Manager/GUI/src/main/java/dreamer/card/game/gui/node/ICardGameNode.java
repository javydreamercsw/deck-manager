package dreamer.card.game.gui.node;

import com.reflexit.magiccards.core.model.ICardGame;
import dreamer.card.game.core.Tool;
import dreamer.card.game.gui.node.factory.ICardSetChildFactory;
import java.awt.Image;
import java.beans.IntrospectionException;
import javax.swing.JFrame;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * Represents a ICardGame element within the system
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardGameNode extends BeanNode {

    private final ICardGame game;
    private Image image = null;

    public ICardGameNode(ICardGame game, ICardSetChildFactory childFactory)
            throws IntrospectionException {
        super(game, Children.create(childFactory, false),
                Lookups.singleton(game));
        setDisplayName(game.getName());
        this.game = game;
        //Retrieve icon in advance
        getIcon(0);
    }

    /**
     * @return the game
     */
    public ICardGame getGame() {
        return game;
    }

    @Override
    public Image getIcon(int type) {
        if (image == null) {
            Image gameIcon = getGame().getGameIcon();
            image = gameIcon == null ? null : Tool.loadImage(new JFrame(),
                    gameIcon).getImage();
        }
        return image;
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
