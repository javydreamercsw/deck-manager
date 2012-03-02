/**
 * *****************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 * *****************************************************************************
 */
package mtg.card.sync;

import dreamer.card.game.Editions;
import dreamer.card.game.storage.IDataBaseManager;
import dreamer.card.game.storage.cache.AbstractCardCache;
import dreamer.card.game.storage.cache.CannotDetermineSetAbbriviation;
import dreamer.card.game.storage.cache.ICardCache;
import dreamer.card.game.storage.database.persistence.Card;
import dreamer.card.game.storage.database.persistence.CardSet;
import dreamer.card.game.storage.database.persistence.controller.CardJpaController;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public URL createRemoteImageURL(Card card, CardSet cs) throws MalformedURLException, CannotDetermineSetAbbriviation {
        String edition = cs.getName();
        String editionAbbr = Editions.getInstance().getAbbrByName(edition);
        if (editionAbbr == null) {
            throw new CannotDetermineSetAbbriviation(cs);
        }
        return ParseGathererNewVisualSpoiler.createImageURL(
                Integer.valueOf(Lookup.getDefault().lookup(IDataBaseManager.class).getCardAttribute(card, "CardId")), editionAbbr);
    }

    @Override
    public Runnable getCacheTask() {
        return new CardImageLoader();
    }

    private class CardImageLoader extends Thread {

        public CardImageLoader() {
            super("Loading card images");
        }

        @Override
        public void run() {
            while (true) {
                Card card;
                synchronized (getCardImageQueue()) {
                    if (getCardImageQueue().size() > 0) {
                        card = getCardImageQueue().iterator().next();
                        getCardImageQueue().remove(card);
                        //Update with database
                        card = new CardJpaController(Lookup.getDefault().lookup(IDataBaseManager.class).getEntityManagerFactory()).findCard(card.getCardPK());
                        LOG.log(Level.INFO, "Processing card: {0}", card.getName());
                        if (card.getCardSetList().isEmpty()) {
                            LOG.log(Level.SEVERE, "No card sets defined for card: {0}", card.getName());
                        } else {
                            for (CardSet cs : card.getCardSetList()) {
                                LOG.log(Level.INFO, "Processing set: {0} for card: {1}",
                                        new Object[]{cs.getName(), card.getName()});
                                try {
                                    URL url = createRemoteImageURL(card, cs);
                                    downloadAndSaveImage(card, cs, url, isLoadingEnabled(), true);
                                } catch (CannotDetermineSetAbbriviation e) {
                                    LOG.log(Level.SEVERE, "Looks like the set: " + cs.getName() + " is not properly created. It is missing the abbreviation", e);
                                    return;
                                } catch (Exception e) {
                                    LOG.log(Level.SEVERE, null, e);
                                    return;
                                } finally {
                                    getCardImageQueue().notifyAll();
                                }
                            }
                        }
                    } else {
                        try {
                            Thread.currentThread().sleep(100);
                        } catch (InterruptedException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }
}
