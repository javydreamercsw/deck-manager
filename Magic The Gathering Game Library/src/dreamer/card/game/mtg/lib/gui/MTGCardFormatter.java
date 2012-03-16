package dreamer.card.game.mtg.lib.gui;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardAttributeFormatter;
import com.reflexit.magiccards.core.model.ICardGame;
import dreamer.card.game.core.Tool;
import dreamer.card.game.mtg.lib.MTGCardCache;
import dreamer.card.game.mtg.lib.MTGRCPGame;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = ICardAttributeFormatter.class)
public class MTGCardFormatter implements ICardAttributeFormatter {

    @Override
    public ICardGame getGame() {
        return new MTGRCPGame();
    }

    @Override
    public Object format(String value) {
        List<ICardCache> impls = getGame().getCardCacheImplementations();
        if (impls.size() > 0 && value.contains("{") && value.contains("}")) {
            JLabel container = new JLabel();
            container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
            ArrayList<String> values = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(value, "}");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                values.add(token.substring(1));
            }
            for (Iterator<String> it = values.iterator(); it.hasNext();) {
                String v = it.next();
                try {
                    JLabel iconLabel = new JLabel(new ImageIcon((Tool.toBufferedImage(((MTGCardCache) impls.get(0)).getManaIcon(v)))));
                    container.add(iconLabel);
                    if (it.hasNext()) {
                        container.add(Box.createRigidArea(new Dimension(5, 0)));
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return container;
        } else {
            return value;
        }
    }
}
