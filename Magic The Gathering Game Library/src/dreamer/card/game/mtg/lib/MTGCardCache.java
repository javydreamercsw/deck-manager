package dreamer.card.game.mtg.lib;

import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.cache.AbstractCardCache;
import com.reflexit.magiccards.core.cache.ICacheData;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.Card;
import com.reflexit.magiccards.core.storage.database.CardSet;
import com.reflexit.magiccards.core.storage.database.DataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.controller.CardJpaController;
import dreamer.card.game.core.UpdateRunnable;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import mtg.card.sync.ParseGathererMTGIcon;
import mtg.card.sync.ParseGathererNewVisualSpoiler;
import mtg.card.sync.ParseGathererSetIcons;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 *
 */
@ServiceProvider(service = ICardCache.class)
public class MTGCardCache extends AbstractCardCache {

    private static final Logger LOG = Logger.getLogger(MTGCardCache.class.getName());
    private CardImageLoader loader = null;

    public MTGCardCache() {
        super(new MTGRCPGame().getName());
    }

    @Override
    public URL createSetImageRemoteURL(String editionAbbr, String rarity) throws MalformedURLException {
        if (!MTGCardCache.isLoadingEnabled()) {
            return null;
        }
        return ParseGathererNewVisualSpoiler.createSetImageURL(editionAbbr, rarity);
    }

    @Override
    public Runnable getCacheTask() {
        if (loader == null) {
            loader = new CardImageLoader();
        }
        return loader;
    }

    @Override
    public URL createRemoteImageURL(ICard icard, Edition edtn) throws MalformedURLException, CannotDetermineSetAbbriviation {
        String edition = edtn.getName();
        String editionAbbr = Editions.getInstance().getAbbrByName(edition);
        if (editionAbbr == null) {
            throw new CannotDetermineSetAbbriviation(edtn);
        }
        Integer cardId = Integer.valueOf(Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardAttribute(icard, "CardId"));
        LOG.log(Level.FINE, "Retireving Card id: {0}", cardId);
        return ParseGathererNewVisualSpoiler.createImageURL(
                cardId, editionAbbr);
    }

    @Override
    public boolean loadCardImageOffline(ICard icard, Edition edtn, boolean bln) throws IOException, CannotDetermineSetAbbriviation {
        try {
            HashMap parameters = new HashMap();
            parameters.put("name", edtn.getName());
            List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", parameters);
            if (result.isEmpty()) {
                return false;
            } else {
                CardSet cs = (CardSet) result.get(0);
                return loadCardImageOffline(icard, cs, bln);
            }
        } catch (DBException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public String getGamePath() {
        return getCacheLocationFile().getAbsolutePath() + System.getProperty("file.separator") + getGameName();
    }

    private class CardImageLoader extends UpdateRunnable implements ActionListener {

        private Timer timer;
        private final int period = 10000, pause = 10000;
        private ArrayList<String> mana = new ArrayList<String>();

        public CardImageLoader() {
            mana.add("{B}");
            mana.add("{U}");
            mana.add("{W}");
            mana.add("{R}");
            mana.add("{G}");
            mana.add("{P}");
            mana.add("{X}");
            for (int i = 1; i <= 12; i++) {
                mana.add("{" + i + "}");
            }
        }

        @Override
        public void run() {
            //Timer for inactivity background work
            timer = new Timer(period, this);
            timer.setInitialDelay(pause);
            timer.start();
            while (true) {
                try {
                    Card card = (Card) Lookup.getDefault().lookup(ICacheData.class).next();
                    if (card != null) {
                        timer.restart();
                        synchronized (this) {
                            reportResumeProgress();
                            int size = Lookup.getDefault().lookup(ICacheData.class).toCacheAmount();
                            setSize(size);
                            int progress = 0;
                            updateProgressMessage("Downloading card images...");
                            card = new CardJpaController(((DataBaseCardStorage) Lookup.getDefault().lookup(IDataBaseCardStorage.class)).getEntityManagerFactory()).findCard(card.getCardPK());
                            LOG.log(Level.FINE, "Processing card: {0}", card.getName());
                            if (card.getCardSetList().isEmpty()) {
                                LOG.log(Level.SEVERE, "No card sets defined for card: {0}", card.getName());
                            } else {
                                for (Iterator<CardSet> it2 = card.getCardSetList().iterator(); it2.hasNext();) {
                                    CardSet cs = it2.next();
                                    String message = "Processing set: " + cs.getName() + " for card: " + card.getName() + "";
                                    LOG.log(Level.FINE, message);
                                    updateProgressMessage(message);
                                    try {
                                        URL url = createRemoteImageURL((ICard) card, Editions.getInstance().getEditionByName(cs.getName()));
                                        getCardImage(card, cs, url, isLoadingEnabled(), true);
                                    } catch (CannotDetermineSetAbbriviation e) {
                                        LOG.log(Level.SEVERE, "Looks like the set: "
                                                + cs.getName() + " is not properly created. "
                                                + "It is missing the abbreviation", e);
                                        return;
                                    } catch (Exception e) {
                                        LOG.log(Level.SEVERE, null, e);
                                        return;
                                    } finally {
                                        notifyAll();
                                    }
                                }
                                progress++;
                                reportProgress(progress);
                            }
                        }
                    } else {
                        try {
                            reportDone();
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (Exception ex) {
                    //THis is expected if the task is interrupted
                    LOG.log(Level.FINEST, null, ex);
                    return;
                }
            }
        }

        @Override
        public String getActionName() {
            return "Loading card images";
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Get mana icons
                        for (String m : mana) {
                            LOG.log(Level.FINE, "Getting mana icon for: {0}", m);
                            try {
                                getManaIcon(m);
                            } catch (IOException ex) {
                                LOG.log(Level.SEVERE, null, ex);
                            }
                        }
                        //Get all cards
                        LOG.log(Level.FINE, "Adding cards to the download queue");
                        for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Card.findAll").iterator(); it.hasNext();) {
                            Card card = (Card) it.next();
                            //Check if card's image has been downloaded or not
                            for (CardSet set : card.getCardSetList()) {
                                if (!cardImageExists(card, set)) {
                                    //Add it to the queue
                                    LOG.log(Level.FINER, "Added card: {0} to the image queue.", card.getName());
                                    Lookup.getDefault().lookup(ICacheData.class).add(card);
                                    break;
                                }
                            }
                        }
                        LOG.log(Level.FINE, "Done adding cards to the download queue");
                        timer.restart();
                    } catch (DBException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
            timer.restart();
        }
    }

    public static URL createManaImageURL(String symbol) {
        String manaName = symbol.replaceAll("[{}/]", "");
        try {
            return new URL("http://gatherer.wizards.com/Handlers/Image.ashx?size=small&name=" + manaName + "&type=symbol");
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public String getManaIconPath(String mana) {
        return getGamePath() + System.getProperty("file.separator")
                + "Mana" + System.getProperty("file.separator") + mana.replaceAll("[{}/]", "") + ".jpg";
    }

    @Override
    public Image getGameIcon(ICardGame game) throws IOException {
        ParseGathererMTGIcon parser = new ParseGathererMTGIcon();
        parser.load();
        try {
            String path = getGameIconPath();
            File dest = new File(path);
            String url = parser.getIconURL();
            URL setIconURL = new URL(url);
            return downloadImageFromURL(setIconURL, dest, !dest.exists());
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, "Errors with the icon URL at URL: "
                    + parser.getIconURL(), ex);
            return null;
        }
    }

    @Override
    public Image getSetIcon(ICardSet set) throws IOException {
        ParseGathererSetIcons parser = new ParseGathererSetIcons(set);
        parser.load();
        try {
            String path = getSetIconPath(set);
            File dest = new File(path);
            String url = parser.getIconURL();
            URL setIconURL = new URL(url);
            return downloadImageFromURL(setIconURL, dest, !dest.exists());
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, "Errors with the icon URL for set: "
                    + set.getName() + " at URL: " + parser.getIconURL(), ex);
            return null;
        }
    }

    public Image getManaIcon(String mana) throws IOException {
        URL manaIconURL = null;
        try {
            String path = getManaIconPath(mana);
            File dest = new File(path);
            manaIconURL = createManaImageURL(mana);
            return downloadImageFromURL(manaIconURL, dest, !dest.exists());
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, "Errors with the icon URL for mana: "
                    + mana + " at URL: " + manaIconURL, ex);
            return null;
        }
    }
}
