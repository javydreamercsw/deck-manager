package dreamer.card.game;

import mtg.card.sync.MTGCardCache;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class MTGGame extends DefaultCardGame {

    private MTGCardCache cache;

    public MTGGame() {
        cache = new MTGCardCache();
    }

    @Override
    public String getName() {
        return "Magic the Gathering";
    }

    @Override
    public Runnable getUpdateRunnable() {
        return getCache().getCacheTask();
    }

    /**
     * @return the cache
     */
    public MTGCardCache getCache() {
        return cache;
    }
}
