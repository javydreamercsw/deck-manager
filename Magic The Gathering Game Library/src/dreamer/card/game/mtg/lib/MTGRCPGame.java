package dreamer.card.game.mtg.lib;

import com.reflexit.magiccards.core.model.ICardGame;
import dreamer.card.game.MTGGame;
import java.util.logging.Logger;
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
}
