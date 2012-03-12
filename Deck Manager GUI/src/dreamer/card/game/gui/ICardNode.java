package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import java.awt.Image;
import java.beans.IntrospectionException;
import java.util.Iterator;
import javax.swing.Action;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Represents a MarauroaApplication element within the system
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardNode extends BeanNode {

    private Action[] actions;
    private final ICardSet set;

    public ICardNode(ICard card, ICardSet set) throws IntrospectionException {
        super(card, null, Lookups.singleton(card));
        setDisplayName(card.getName());
        this.set = set;
    }

    @Override
    public Image getIcon(int type) {
        String gameName = set.getGameName();
        for (Iterator<? extends ICardGame> it = Lookup.getDefault().lookupAll(ICardGame.class).iterator(); it.hasNext();) {
            ICardGame game = it.next();
            if (game.getName().equals(gameName)) {
                return game.getBackCardIcon();
            }
        }
        return null;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public Node.Cookie getCookie(Class clazz) {
        Children ch = getChildren();

        if (clazz.isInstance(ch)) {
            return (Node.Cookie) ch;
        }

        return super.getCookie(clazz);
    }

    @Override
    public Action[] getActions(boolean popup) {
        actions = new Action[]{ //            SystemAction.get(DeleteAction.class),
        //            null,
        //            new MarauroaApplicationNode.ConfigureAction(),
        //            new MarauroaApplicationNode.AddRPZoneAction(),
        //            new MarauroaApplicationNode.StartServerAction(),
        //            new MarauroaApplicationNode.StopServerAction(),
        //            new MarauroaApplicationNode.ConnectAction(),
        //            new MarauroaApplicationNode.DisconnectAction(),
        //            null,
        //            new MarauroaApplicationNode.DeleteServerAction()
        };
        return actions;
    }

    /**
     * @return the card
     */
    public ICard getCard() {
        return getLookup().lookup(ICard.class);
    }
}