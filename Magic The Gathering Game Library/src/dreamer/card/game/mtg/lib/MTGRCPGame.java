package dreamer.card.game.mtg.lib;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardGame;
import dreamer.card.game.MTGGame;
import dreamer.card.game.core.Tool;
import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
}
