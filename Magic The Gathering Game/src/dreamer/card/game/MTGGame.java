package dreamer.card.game;

import java.util.logging.Logger;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class MTGGame extends DefaultCardGame {

    private static final Logger LOG = Logger.getLogger(MTGGame.class.getName());

    @Override
    public String getName() {
        return "Magic the Gathering";
    }

    @Override
    public Runnable getUpdateRunnable() {
        return null;
    }
}
