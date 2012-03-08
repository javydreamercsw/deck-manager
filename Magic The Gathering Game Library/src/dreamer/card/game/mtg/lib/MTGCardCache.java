package dreamer.card.game.mtg.lib;

import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.cache.AbstractCardCache;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.Card;
import com.reflexit.magiccards.core.storage.database.CardSet;
import com.reflexit.magiccards.core.storage.database.DataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.controller.CardJpaController;
import dreamer.card.game.core.UpdateRunnable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtg.card.sync.ParseGathererNewVisualSpoiler;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 *
 */
@ServiceProvider(service = ICardCache.class)
public class MTGCardCache extends AbstractCardCache {

    private static final Logger LOG = Logger.getLogger(MTGCardCache.class.getName());

    @Override
    public URL createSetImageRemoteURL(String editionAbbr, String rarity) throws MalformedURLException {
        if (!MTGCardCache.isLoadingEnabled()) {
            return null;
        }
        return ParseGathererNewVisualSpoiler.createSetImageURL(editionAbbr, rarity);
    }

    @Override
    public Runnable getCacheTask() {
        return new CardImageLoader();
    }

    @Override
    public URL createRemoteImageURL(ICard icard, Edition edtn) throws MalformedURLException, CannotDetermineSetAbbriviation {
        String edition = edtn.getName();
        String editionAbbr = Editions.getInstance().getAbbrByName(edition);
        if (editionAbbr == null) {
            throw new CannotDetermineSetAbbriviation(edtn);
        }
        return ParseGathererNewVisualSpoiler.createImageURL(
                Integer.valueOf(Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardAttribute(icard, "CardId")), editionAbbr);
    }

    @Override
    public boolean loadCardImageOffline(ICard icard, Edition edtn, boolean bln) throws IOException, CannotDetermineSetAbbriviation {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class CardImageLoader extends UpdateRunnable {

        @Override
        public void run() {
            while (true) {
                Card card;
                synchronized (getCardImageQueue()) {
                    if (getCardImageQueue().size() > 0) {
                        reportResumeProgress();
                        int progress=0;
                        for (Iterator<ICard> it = getCardImageQueue().iterator(); it.hasNext();) {
                            setSize(getCardImageQueue().size());
                            card = (Card) it.next();
                            updateProgressMessage("Downloading card images...");
                            getCardImageQueue().remove((ICard) card);
                            card = new CardJpaController(((DataBaseCardStorage) Lookup.getDefault().lookup(IDataBaseCardStorage.class)).getEntityManagerFactory()).findCard(card.getCardPK());
                            LOG.log(Level.INFO, "Processing card: {0}", card.getName());
                            if (card.getCardSetList().isEmpty()) {
                                LOG.log(Level.SEVERE, "No card sets defined for card: {0}", card.getName());
                            } else {
                                for (CardSet cs : card.getCardSetList()) {
                                    String message = "Processing set: " + cs.getName() + " for card: " + card.getName() + "";
                                    LOG.log(Level.INFO, message);
                                    updateProgressMessage(message);
                                    try {
                                        URL url = createRemoteImageURL((ICard) card, Editions.getInstance().getEditionByName(cs.getName()));
                                        downloadAndSaveImage(card, cs, url, isLoadingEnabled(), true);
                                    } catch (CannotDetermineSetAbbriviation e) {
                                        LOG.log(Level.SEVERE, "Looks like the set: "
                                                + cs.getName() + " is not properly created. "
                                                + "It is missing the abbreviation", e);
                                        return;
                                    } catch (Exception e) {
                                        LOG.log(Level.SEVERE, null, e);
                                        return;
                                    } finally {
                                        getCardImageQueue().notifyAll();
                                    }
                                }
                                progress++;
                                reportProgress(progress);
                            }
                        }
                    } else {
                        try {
                            reportSuspendProgress();
                            Thread.currentThread().sleep(100);
                        } catch (InterruptedException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }

        @Override
        public String getActionName() {
            return "Loading card images";
        }
    }
}
