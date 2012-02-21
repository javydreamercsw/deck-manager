package mtg.card.sync;

import dreamer.card.game.ICardField;
import dreamer.card.game.storage.ICardStore;
import dreamer.card.game.storage.IStorage;
import dreamer.card.game.storage.IStorageContainer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import mtg.card.IMagicCard;
import mtg.card.MagicCard;
import mtg.card.MagicCardField;

public class UpdateCardsFromWeb {

    public void updateStore(IMagicCard card, Set<ICardField> fieldMaps, String lang, ICardStore magicDb)
            throws IOException {
        ArrayList<IMagicCard> list = new ArrayList<IMagicCard>(1);
        list.add(card);
        if (lang == null) {
            lang = card.getLanguage();
        }
        updateStore(list.iterator(), 1, fieldMaps, lang, magicDb);
    }

    public void updateStore(Iterator<IMagicCard> iter, int size, Set<ICardField> fieldMaps, String lang, ICardStore magicDb) throws IOException {
        IStorage storage = ((IStorageContainer) magicDb).getStorage();
        ParseGathererDetails rulParser = new ParseGathererDetails();
        rulParser.setMagicDb(magicDb);
        ParseGathererBasicInfo linfoParser = new ParseGathererBasicInfo();
        ParseGathererBasicInfo textParser = new ParseGathererBasicInfo();
        ParseGathererCardLanguages langParser = new ParseGathererCardLanguages();
        langParser.setLanguage(lang);
        boolean loadText = fieldMaps.contains(MagicCardField.TEXT);
        boolean loadLang = fieldMaps.contains(MagicCardField.LANG);
        boolean loadImage = fieldMaps.contains(MagicCardField.ID);
        // load
        storage.setAutoCommit(false);
        try {
            for (int i = 0; iter.hasNext(); i++) {
                IMagicCard card = iter.next();
                MagicCard magicCard = card.getBase();
                // load individual card
                System.out.println("Updating card " + (i+1) + " of " + size);
                try {
                    rulParser.parseSingleCard(card, fieldMaps);
                    if (loadText) {
                        textParser.setCard(card);
                        textParser.setFilter(fieldMaps);
                        textParser.load();
                    }
                    if (loadLang) {
                        langParser.setCardId(card.getCardId());
                        langParser.load();
                        int langId = langParser.getLangCardId();
                        if (langId != 0) {
                            MagicCard newMagicCard = magicCard.cloneCard();
                            newMagicCard.setCardId(langId);
                            newMagicCard.setEnglishCardId(card.getCardId());
                            newMagicCard.setLanguage(lang);
                            linfoParser.setCard(newMagicCard);
                            linfoParser.load();
                            if (magicDb.getCard(newMagicCard.getCardId()) == null) {
                                magicDb.add(newMagicCard);
                                 System.err.println("Added " + newMagicCard.getName());
                            }
                        }
                    }
                } catch (IOException e) {
//					Activator.log("Cannot load card " + e.getMessage() + " " + card.getCardId());
                }
                magicDb.update(magicCard);
                if (loadImage) {
                    // load and cache image offline
                    CardCache.loadCardImageOffline(card, false);
                }
            }
        } finally {
            storage.setAutoCommit(true);
            storage.save();
        }
    }
}
