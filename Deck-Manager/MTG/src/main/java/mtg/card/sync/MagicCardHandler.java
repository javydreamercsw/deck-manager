package mtg.card.sync;

import java.util.Properties;

import com.reflexit.magiccards.core.model.ICardHandler;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

import mtg.card.MagicFilteredCardStore;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class MagicCardHandler implements ICardHandler
{

  private final MagicFilteredCardStore dbStore = new MagicFilteredCardStore();
  private final MagicFilteredCardStore libStore = new MagicFilteredCardStore();

  @Override
  public IFilteredCardStore getDBFilteredStore()
  {
    return dbStore;
  }

  @Override
  public IFilteredCardStore getDBFilteredStoreWorkingCopy()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public IFilteredCardStore getLibraryFilteredStore()
  {
    return libStore;
  }

  @Override
  public ICardStore getLibraryCardStore()
  {
    return libStore.getCardStore();
  }

  @Override
  public IFilteredCardStore getLibraryFilteredStoreWorkingCopy()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public IFilteredCardStore getCardCollectionFilteredStore(String id)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public IFilteredCardStore getActiveDeckHandler()
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public ICardStore loadFromXml(String filename)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setActiveDeckHandler(IFilteredCardStore store)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int downloadUpdates(String set, Properties options)
          throws Exception, InterruptedException
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void loadInitialIfNot() throws Exception
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public ICardStore getDBStore()
  {
    return dbStore.getCardStore();
  }
}
