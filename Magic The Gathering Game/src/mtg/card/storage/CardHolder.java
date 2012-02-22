package mtg.card.storage;

import dreamer.card.game.ICardHandler;
import dreamer.card.game.storage.ICardStore;
import dreamer.card.game.storage.IFilteredCardStore;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import mtg.card.MagicException;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class CardHolder implements ICardHandler {

    @Override
    public IFilteredCardStore getDBFilteredStore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IFilteredCardStore getDBFilteredStoreWorkingCopy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IFilteredCardStore getLibraryFilteredStore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ICardStore getLibraryCardStore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IFilteredCardStore getLibraryFilteredStoreWorkingCopy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IFilteredCardStore getCardCollectionFilteredStore(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IFilteredCardStore getActiveDeckHandler() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ICardStore loadFromXml(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setActiveDeckHandler(IFilteredCardStore ifcs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int downloadUpdates(String string, Properties prprts) throws Exception, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void loadInitialIfNot() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ICardStore getDBStore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void loadInitial() throws MagicException {
//        try {
//            LoadMagicDb.loadInitial();
//        } catch (IOException e) {
//            throw new MagicException(e);
//        } catch (SQLException e) {
//            throw new MagicException(e);
//        }
    }
}
