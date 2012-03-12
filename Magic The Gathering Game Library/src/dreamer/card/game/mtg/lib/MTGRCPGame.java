package dreamer.card.game.mtg.lib;

import com.reflexit.magiccards.core.model.ICardGame;
import dreamer.card.game.MTGGame;
import dreamer.card.game.core.Tool;
import java.awt.Image;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = ICardGame.class)
public class MTGRCPGame extends MTGGame {

    private static final Logger LOG = Logger.getLogger(MTGRCPGame.class.getName());

    @Override
    public Runnable getUpdateRunnable() {
        return new MTGUpdater(this);
    }

    @Override
    public Image getBackCardIcon() {
        try {
            return Tool.createImage("dreamer.card.game.mtg.lib", "images/back.jpg", "Card icon");
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
}
