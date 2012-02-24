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

import com.reflexit.magiccards.core.CachedImageNotFoundException;
import dreamer.card.game.CardFileUtils;
import dreamer.card.game.Editions;
import dreamer.card.game.Editions.Edition;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mtg.card.CannotDetermineSetAbbriviation;
import mtg.card.IMagicCard;

/**
 * @author Alena
 *
 */
public class CardCache {

    private static boolean caching;
    private static boolean loading;
    private static CardImageLoader loader = null;

    public static void setCachingEnabled(boolean enabled) {
        caching = enabled;
    }

    public static void setLoadingEnabled(boolean enabled) {
        loading = enabled;
    }

    public static URL createSetImageURL(IMagicCard card, boolean upload) throws IOException {
        String edition = card.getSet();
        String editionAbbr = Editions.getInstance().getAbbrByName(edition);
        String rarity = card.getRarity();
        if (editionAbbr == null) {
            return null;
        }
        String path = createLocalSetImageFilePath(editionAbbr, rarity);
        File file = new File(path);
        URL localUrl = file.toURI().toURL();
        if (upload == false) {
            return localUrl;
        }
        if (file.exists()) {
            return localUrl;
        }
        try {
            URL url = createSetImageRemoteURL(editionAbbr, rarity);
            if (url == null) {
                return null;
            }
            InputStream st = url.openStream();
            CardFileUtils.saveStream(st, file);
            st.close();
        } catch (IOException e1) {
            throw e1;
        }
        return localUrl;
    }

    public static URL createRemoteImageURL(IMagicCard card) throws MalformedURLException {
        String edition = card.getSet();
        String editionAbbr = Editions.getInstance().getAbbrByName(edition);
        if (editionAbbr == null) {
            throw new CannotDetermineSetAbbriviation(card);
        }
        return ParseGathererNewVisualSpoiler.createImageURL(card.getCardId(), editionAbbr);
    }

    public static URL createSetImageRemoteURL(String editionAbbr, String rarity) throws MalformedURLException {
        if (!CardCache.isLoadingEnabled()) {
            return null;
        }
        return ParseGathererNewVisualSpoiler.createSetImageURL(editionAbbr, rarity);
    }

    public static String createLocalImageFilePath(IMagicCard card) {
        String editionName = card.getSet();
        Editions editions = Editions.getInstance();
        Edition set = editions.getEditionByName(editionName);
        if (set == null) {
            throw new CannotDetermineSetAbbriviation(card);
        }
        String editionAbbr = set.getBaseFileName();
        int cardId = card.getCardId();
        File loc = new File("./");//TODO: Use the cache dir see http://bits.netbeans.org/dev/javadoc/org-openide-modules/org/openide/modules/Places.html getCacheDirectory()
        String locale = "EN";
        String part = "Cards/" + editionAbbr + "/" + locale + "/Card" + cardId + ".jpg";
        String file = new File(loc, part).getPath();
        return file;
    }

    public static String createLocalSetImageFilePath(String editionAbbr, String rarity) throws MalformedURLException {
        File loc = new File("./");//TODO: Use the cache dir see http://bits.netbeans.org/dev/javadoc/org-openide-modules/org/openide/modules/Places.html getCacheDirectory()
        String part = "Sets/" + editionAbbr + "-" + rarity + ".jpg";
        String file = new File(loc, part).getPath();
        return file;
    }

    public static boolean isLoadingEnabled() {
        return loading;
    }
    private final static ArrayList<IMagicCard> cardImageQueue = new ArrayList<IMagicCard>();

    private static class CardImageLoader extends Thread {

        public CardImageLoader() {
            super("Loading card images");
        }

        @Override
        public void run() {
             while (true) {
                IMagicCard card = null;
                synchronized (cardImageQueue) {
                    if (cardImageQueue.size() > 0) {
                        card = cardImageQueue.iterator().next();
                        cardImageQueue.remove(card);
                    } else {
                        try {
                            sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(CardCache.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                synchronized (card) {
                    try {
                        downloadAndSaveImage(card, isLoadingEnabled(), true);
                    } catch (Exception e) {
                        continue;
                    } finally {
                        card.notifyAll();
                    }
                }
            }
        }
    }

    private static void queueImageLoading(IMagicCard card) {
        synchronized (cardImageQueue) {
            if (!cardImageQueue.contains(card)) {
                cardImageQueue.add(card);
            }
            if(loader == null){
                loader = new CardImageLoader();
                loader.start();
            }
        }
    }

    /**
     * Download and save card image, if not already saved
     *
     * @param card
     * @param caching2
     * @return
     * @throws IOException
     */
    public static File downloadAndSaveImage(IMagicCard card, boolean remote, boolean forceRemote) throws IOException, Exception {
        synchronized (card) {
            String path = CardCache.createLocalImageFilePath(card);
            File file = new File(path);
            if (forceRemote == false && file.exists()) {
                return file;
            }
            if (!remote) {
                throw new Exception("Cannot find cached image for " + card.getName());
            }
            URL url = createRemoteImageURL(card);
            InputStream st = null;
            try {
                st = url.openStream();
            } catch (IOException e) {
                throw new IOException("Cannot connect: " + e.getMessage());
            }
            File file2 = new File(path + ".part");
            CardFileUtils.saveStream(st, file2);
            st.close();
            if (file2.exists()) {
                file2.renameTo(file);
                if (!file.exists()) {
                    throw new IOException("failed to rename into " + file.toString());
                }
                return file;
            }
            throw new FileNotFoundException(file.toString());
        }
    }

    /**
     * Get card image or schedule a loading job if image not found. This image
     * is not managed - to be disposed by called. To get notified when job is
     * done loading, can wait on card object
     *
     * @param card
     * @return true if card image exists, schedule update otherwise. If loading
     * is disabled and there is no cached image through an exception
     * @throws IOException
     */
    public static boolean loadCardImageOffline(IMagicCard card, boolean forceUpdate) throws IOException, CannotDetermineSetAbbriviation {
        String path = createLocalImageFilePath(card);
        File file = new File(path);
        if (file.exists() && forceUpdate == false) {
            return true;
        }
        if (!isLoadingEnabled()) {
            throw new CachedImageNotFoundException("Cannot find cached image for " + card.getName());
        }
        CardCache.queueImageLoading(card);
        return false;
    }

    public static boolean isImageCached(IMagicCard card) {
        String path = createLocalImageFilePath(card);
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    private CardCache() {
    }
}
