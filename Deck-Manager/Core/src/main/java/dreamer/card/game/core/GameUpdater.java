package dreamer.card.game.core;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public abstract class GameUpdater extends UpdateRunnable
{

  private static final Logger LOG
          = Logger.getLogger(GameUpdater.class.getName());
  protected AtomicBoolean dbError = new AtomicBoolean(false);
  private String codeNameBase, dbFileName = "card_manager.mv.db";
  protected final ExecutorService executor = Executors.newFixedThreadPool(50);

  public GameUpdater(ICardGame game)
  {
    super(game);
  }

  @Override
  public void defaultUpdateLocal()
  {
    synchronized (this)
    {
      if (!localUpdated.get())
      {
        long start = System.currentTimeMillis();
        RequestProcessor RP = new RequestProcessor("Updating local", 1,
                false);
        ProgressHandle ph = ProgressHandleFactory.createHandle(
                "Updating Local Database.");
        RequestProcessor.Task theTask = RP.create(() ->
        {
          updating.set(true);
          localUpdating.set(true);
          ph.start();
          try
          {
            List temp = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                    .namedQuery("CardSet.findAll");
            LOG.log(Level.FINE, "{0} sets found in database.",
                    temp.size());
            if (temp.isEmpty())
            {
              //Load from the default database
              updateProgressMessage(NbBundle.getMessage(GameUpdater.class,
                      "initial.load").replaceAll("[x]",
                              getGame().getName()));
              loadDefaultCards(ph);
            }
            updateLocal();
          }
          catch (DBException ex)
          {
            Exceptions.printStackTrace(ex);
          }
          localUpdated.set(true);
          localUpdating.set(false);
        });
        theTask.addTaskListener((org.openide.util.Task task) ->
        {
          //Make sure that we get rid of the ProgressHandle
          //when the task is finished
          ph.finish();
          LOG.log(Level.FINE, "Updating local took: {0}",
                  Tool.elapsedTime(start));
          reportDone();
        });
        theTask.schedule(0);
      }
      else
      {
        localUpdating.set(false);
      }
    }
  }

  @Override
  public void defaultUpdateRemote()
  {
    synchronized (this)
    {
      long start = System.currentTimeMillis();
      RequestProcessor RP = new RequestProcessor("Updating remote", 1, false);
      ProgressHandle ph = ProgressHandleFactory.createHandle(
              "Updating from remote data. Please wait. This can take a long time.");
      RequestProcessor.Task theTask = RP.create(() ->
      {
        ph.start();
        remoteUpdating.set(true);
        updateRemote();
      });
      theTask.addTaskListener((org.openide.util.Task task) ->
      {
        //Make sure that we get rid of the ProgressHandle
        //when the task is finished
        ph.finish();
        LOG.log(Level.INFO, "Updating remote took: {0}",
                Tool.elapsedTime(start));
        remoteUpdating.set(false);
      });
      theTask.schedule(0);
    }
  }

  /**
   * @return the codeNameBase
   */
  public String getCodeNameBase()
  {
    return codeNameBase;
  }

  /**
   * @param codeNameBase the codeNameBase to set
   */
  public void setCodeNameBase(String codeNameBase)
  {
    this.codeNameBase = codeNameBase;
  }

  /**
   * @return the dbFileName
   */
  public String getDBFileName()
  {
    return dbFileName;
  }

  /**
   * @param dbFileName the dbFileName to set
   */
  public void setDBFileName(String dbFileName)
  {
    this.dbFileName = dbFileName;
  }

  /**
   * Load the default cards
   *
   * @param ph Progress Handle to update status.
   * @throws com.reflexit.magiccards.core.model.storage.db.DBException
   */
  public abstract void loadDefaultCards(ProgressHandle ph) throws DBException;

  /**
   * Update a card set.
   *
   * @param set Set to update
   */
  public abstract void updateSet(ICardSet set);

  /**
   * Update card.
   *
   * @param card Card to update.
   */
  public abstract void updateCard(ICard card);
}
