package dreamer.card.game.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public abstract class UpdateRunnable implements IProgressAction,
        DataBaseStateListener
{

  private int size, progress = 0;
  private final ArrayList<UpdateProgressListener> listeners
          = new ArrayList<>();
  private final ICardGame game;
  private static final Logger LOG
          = Logger.getLogger(UpdateRunnable.class.getName());
  protected boolean localUpdated = false, localUpdating = false;
  protected boolean remoteUpdated = false, remoteUpdating = false;
  protected boolean updating = false;

  public UpdateRunnable(ICardGame game)
  {
    this.game = game;
    Lookup.getDefault().lookup(IDataBaseCardStorage.class)
            .addDataBaseStateListener(UpdateRunnable.this);
  }

  /**
   * Does a local update to initialize from the plug-in. This is to be
   * implemented on each game.
   */
  public abstract void updateLocal();

  /**
   * Does a remote update to initialize from a remote place like the Internet or
   * database. This is to be implemented on each game.
   */
  public abstract void updateRemote();

  @Override
  public void addListener(UpdateProgressListener listener)
  {
    if (!listeners.contains(listener))
    {
      listeners.add(listener);
    }
  }

  @Override
  public int getSize()
  {
    return size;
  }

  @Override
  public void removeListener(UpdateProgressListener listener)
  {
    if (listeners.contains(listener))
    {
      listeners.remove(listener);
    }
  }

  @Override
  public void setSize(int size)
  {
    LOG.log(Level.FINE, "Setting size to: {0}", size);
    this.size = size;
    reportSize(getSize());
  }

  @Override
  public void reportSize(int size)
  {
    listeners.forEach((listener) ->
    {
      listener.reportSize(size);
    });
  }

  @Override
  public void reportProgress(int amount)
  {
    listeners.forEach((listener) ->
    {
      listener.reportProgress(amount);
    });
  }

  @Override
  public void reportDone()
  {
    listeners.forEach((listener) ->
    {
      listener.reportDone();
    });
  }

  /**
   * @return the progress
   */
  public int getProgress()
  {
    return progress;
  }

  public void increaseProgress(int amount)
  {
    progress += amount;
    reportProgress(progress);
  }

  public void increaseProgress()
  {
    increaseProgress(1);
  }

  @Override
  public void updateProgressMessage(String message)
  {
    listeners.forEach((listener) ->
    {
      listener.changeMessage(message);
    });
  }

  @Override
  public void reportSuspendProgress()
  {
    listeners.forEach((listener) ->
    {
      listener.suspend();
    });
  }

  @Override
  public void reportResumeProgress()
  {
    listeners.forEach((listener) ->
    {
      listener.resume();
    });
  }

  /**
   * @return the game
   */
  public ICardGame getGame()
  {
    return game;
  }

  public List<Object> namedQuery(String query,
          HashMap<String, Object> parameters, EntityManagerFactory emf)
          throws DBException
  {
    Query q;
    EntityManager localEM = emf.createEntityManager();
    EntityTransaction transaction = localEM.getTransaction();
    transaction.begin();
    q = localEM.createNamedQuery(query);
    if (parameters != null)
    {
      Iterator<Map.Entry<String, Object>> entries = parameters.entrySet().iterator();
      while (entries.hasNext())
      {
        Map.Entry<String, Object> e = entries.next();
        q.setParameter(e.getKey(), e.getValue());
      }
    }
    List result = q.getResultList();
    transaction.commit();
    localEM.close();
    return result;
  }

  @Override
  public void initialized()
  {
    HashMap parameters = new HashMap();
    try
    {
      parameters.put("name", game.getName());
      if (Lookup.getDefault().lookup(IDataBaseCardStorage.class)
              .namedQuery("Game.findByName", parameters).isEmpty())
      {
        Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .createGame(game.getName());
      }
    }
    catch (DBException ex)
    {
      LOG.log(Level.SEVERE, null, ex);
    }
    //Update locally
    localUpdating = true;
    defaultUpdateLocal();
    while (localUpdating)
    {
      //Wait for update to end
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException ex)
      {
        Exceptions.printStackTrace(ex);
      }
    }
    //Update remotely
    remoteUpdating = true;
    defaultUpdateRemote();
    while (remoteUpdating)
    {
      //Wait for update to end
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException ex)
      {
        Exceptions.printStackTrace(ex);
      }
    }
  }

  @Override
  public void run()
  {
    reportDone();
  }

  /**
   * Default tasks to be done locally. Usually not implemented by each game. It
   * must call updateLocal.
   */
  public abstract void defaultUpdateLocal();

  /**
   * Default tasks to be done remotely. Usually not implemented by each game. It
   * must call updateRemote.
   */
  public abstract void defaultUpdateRemote();
}
