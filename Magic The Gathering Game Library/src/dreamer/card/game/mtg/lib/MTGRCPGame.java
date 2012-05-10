package dreamer.card.game.mtg.lib;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.IGameCellRendererImageFactory;
import dreamer.card.game.MTGGame;
import dreamer.card.game.core.Tool;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = ICardGame.class)
public class MTGRCPGame extends MTGGame implements ICardGame {

    private static final Logger LOG = Logger.getLogger(MTGRCPGame.class.getName());

    public MTGRCPGame() {
        synchronized (collectionTypes) {
            collectionTypes.add("Deck");
            collectionTypes.add("Collection");
        }
        synchronized (collections) {
            collections.put("Collection", "My Collection");
        }
    }

    @Override
    public Runnable getUpdateRunnable() {
        return new MTGUpdater();
    }

    @Override
    public Image getBackCardIcon() {
        try {
            return Tool.createImage("dreamer.card.game.mtg.lib", "images/back.jpg", "Card icon");
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Image getGameIcon() {
        try {
            return Lookup.getDefault().lookup(ICardCache.class).getGameIcon((ICardGame) this);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public IGameCellRendererImageFactory getCellRendererImageFactory() {
        return new IGameCellRendererImageFactory() {
            @Override
            public JLabel getRendering(String string, Object value) {
                if (value != null) {
                    List<ICardCache> impls = getCardCacheImplementations();
                    if (impls.size() > 0 && ((String) value).contains("{") && ((String) value).contains("}")) {
                        JLabel container = new JLabel();
                        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
                        ArrayList<String> values = new ArrayList<String>();
                        StringTokenizer st = new StringTokenizer((String) value, "}");
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken();
                            values.add(token.substring(1));
                        }
                        for (Iterator<String> it = values.iterator(); it.hasNext();) {
                            try {
                                String v = it.next();
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
                    }
                    return new JLabel(((String) value));
                } else {
                    return new JLabel();
                }
            }
        };
    }
}
