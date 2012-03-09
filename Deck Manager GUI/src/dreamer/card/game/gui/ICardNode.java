package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeSupport;
import javax.swing.Action;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * Represents a MarauroaApplication element within the system
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardNode extends BeanNode {

    private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
    private Action[] actions;
    private ICard card;

    public ICardNode(ICard card) throws IntrospectionException {
        super(card, null, Lookups.singleton(card));
        setDisplayName(card.getName());
        this.card = card;
    }

    //TODO: Set icons
//    @Override
//    public Image getIcon(int type) {
//        Image icon = getMarauroaApplication().getIcon(type);
//        if (icon == null) {
//            try {
//                icon = Tool.createImage("simple.marauroa.application.gui",
//                        "resources/images/app.png", "App icon");
//            } catch (MalformedURLException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (Exception ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//        return icon;
//    }
//
//    @Override
//    public Image getOpenedIcon(int i) {
//        return getIcon(i);
//    }

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
        actions = new Action[]{
//            SystemAction.get(DeleteAction.class),
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
