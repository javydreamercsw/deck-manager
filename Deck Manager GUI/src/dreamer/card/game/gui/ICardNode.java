package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import dreamer.card.game.core.Tool;
import java.awt.Image;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.JFrame;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Represents a MarauroaApplication element within the system
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardNode extends BeanNode {

    private Action[] actions;
    private final String gameName;
    private HashMap<String, String> attributes = new HashMap<String, String>();

    public ICardNode(ICard card, String gameName) throws IntrospectionException {
        super(card, null, Lookups.singleton(card));
        setDisplayName(card.getName());
        this.gameName = gameName;
        loadAttributes(card);
    }

    @Override
    public Image getIcon(int type) {
        for (Iterator<? extends ICardGame> it = Lookup.getDefault().lookupAll(ICardGame.class).iterator(); it.hasNext();) {
            ICardGame game = it.next();
            if (game.getName().equals(gameName)) {
                return Tool.loadImage(new JFrame(), game.getBackCardIcon()).getImage();
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
        //TODO: Actions?
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

    private void loadAttributes(final ICard card) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    attributes.putAll(Lookup.getDefault().lookup(IDataBaseCardStorage.class).getAttributesForCard(card.getName()));
                } catch (DBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, card.getName() + " attribute loader").start();
    }

    public String getAttribute(String attr) {
        return attributes.get(attr);
    }
}
