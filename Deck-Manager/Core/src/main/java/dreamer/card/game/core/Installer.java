package dreamer.card.game.core;

import java.awt.event.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

import com.reflexit.magiccards.core.cache.AbstractCardCache;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;

@ServiceProvider(service = DataBaseStateListener.class)
public class Installer extends ModuleInstall implements ActionListener,
        DataBaseStateListener, WindowSystemListener
{

  private static final Logger LOG
          = Logger.getLogger(Installer.class.getName());
  private final ArrayList<GameUpdateAction> updateActions
          = new ArrayList<>();
  final ArrayList<Thread> runnables = new ArrayList<>();
  private Timer timer;
  private final int period = 30000, pause = 10000;
  private long start;
  private boolean waitDBInit = true;
  private final ExecutorService es = Executors.newCachedThreadPool();

  @Override
  public void restored()
  {
    //Create game cache dir
    File cardCacheDir = new File(MessageFormat.format("{0}{1}cache",
            Places.getCacheSubdirectory("Deck Manager").getAbsolutePath(),
            System.getProperty("file.separator")));
    //Create game cache dir
    if (!cardCacheDir.exists())
    {
      cardCacheDir.mkdirs();
    }
    AbstractCardCache.setCachingEnabled(true);
    AbstractCardCache.setLoadingEnabled(true);
    AbstractCardCache.setCacheDir(cardCacheDir);
    WindowManager.getDefault().invokeWhenUIReady(() ->
    {
      start = System.currentTimeMillis();
      RequestProcessor RP = new RequestProcessor("Updating", 1, false);
      ProgressHandle ph = ProgressHandleFactory.createHandle(
              "Updating Database. Please wait. This can take a long time.");
      ph.start();
      RequestProcessor.Task theTask = RP.create(() ->
      {
        try
        {
          //Timer for inactivity background work
          timer = new Timer(period, Installer.this);
          timer.setInitialDelay(pause);
          timer.start();
          //Start the database activities
          LOG.log(Level.FINE, "Initializing database...");
          try
          {
            //Make sure to load the driver
            start = System.currentTimeMillis();
            Lookup.getDefault().lookup(IDataBaseCardStorage.class).initialize();
            while (waitDBInit)
            {
              Thread.sleep(100);
            }
          }
          catch (DBException | InterruptedException ex)
          {
            LOG.log(Level.SEVERE, null, ex);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    "Unable to connect to database. Please restart application",
                    NotifyDescriptor.ERROR_MESSAGE));
          }
        }
        catch (IllegalArgumentException | SecurityException ex)
        {
          LOG.log(Level.SEVERE, null, ex);
        }
      });
      theTask.addTaskListener((org.openide.util.Task task) ->
      {
        //Make sure that we get rid of the ProgressHandle
        //when the task is finished
        ph.finish();
        LOG.log(Level.INFO, "Updating remote took: {0}",
                Tool.elapsedTime(start));
      });
      theTask.schedule(0);
    });
  }

  @Override
  public boolean closing()
  {
    super.closing();
    try
    {
      updateActions.forEach((updater) ->
      {
        updater.shutdown();
      });
      es.shutdownNow();
      Lookup.getDefault().lookup(IDataBaseCardStorage.class).close();
      return true;
    }
    catch (Exception e)
    {
      LOG.log(Level.SEVERE, "Error shuting down!", e);
      return false;
    }
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    if (afterUpdates())
    {
      //We are done
      timer.stop();
    }
    else
    {
      //Make sure to retry in case we got an update.
      timer.restart();
    }
  }

  private boolean afterUpdates()
  {
    boolean ready = true;
    for (GameUpdateAction gua : updateActions)
    {
      if (!gua.finished)
      {
        ready = false;
        break;
      }
    }
    if (ready)
    {
      for (Thread t : runnables)
      {
        if (!t.isAlive())
        {
          ready = false;
          break;
        }
      }
      //TODO: Code to be executed after all updates are complete
    }
    return ready;
  }

  @Override
  public void initialized()
  {
    LOG.fine("DB ready!");
    waitDBInit = false;
    //Code to be done after the db is ready
    LOG.log(Level.FINE, "Database initialized");
    LOG.log(Level.FINE, "Initializing database took: {0}",
            Tool.elapsedTime(start));
    LOG.log(Level.FINE, "Initializing games...");
    Runnable task;
    for (ICardGame game : Lookup.getDefault().lookupAll(ICardGame.class))
    {
      new GameInitializationAction(game).actionPerformed(null);
      task = game.getUpdateRunnable();
      if (task != null)
      {
        if (task instanceof IProgressAction)
        {
          //Properly created to display progress in the IDE
          updateActions.add(new GameUpdateAction((IProgressAction) task));
        }
        else
        {
          //No progress information available
          runnables.add(new Thread(task,
                  MessageFormat.format("{0} game updater",
                          game.getName())));
        }
      }
    }
    for (ICardCache cache : Lookup.getDefault().lookupAll(ICardCache.class))
    {
      task = cache.getCacheTask();
      if (task != null)
      {
        if (task instanceof IProgressAction)
        {
          //Properly created to display progress in the IDE
          updateActions.add(new CacheUpdateAction((IProgressAction) task));
        }
        else
        {
          //No progress information available
          runnables.add(new Thread(task,
                  MessageFormat.format("{0} cache updater",
                          cache.getGameName())));
        }
      }
    }
    updateActions.forEach((updater) ->
    {
      updater.actionPerformed(null);
    });
    runnables.forEach((runnable) ->
    {
      es.execute(runnable);
    });
  }

  @Override
  public void beforeLoad(WindowSystemEvent event)
  {
    String role = NbPreferences.forModule(TopComponent.class)
            .get("currentScreen", "game_view");
    WindowManager.getDefault().setRole(role);
    WindowManager.getDefault().removeWindowSystemListener(this);
  }

  @Override
  public void afterLoad(WindowSystemEvent wse)
  {
    //Do nothing
  }

  @Override
  public void beforeSave(WindowSystemEvent wse)
  {
    //Do nothing
  }

  @Override
  public void afterSave(WindowSystemEvent wse)
  {
    //Do nothing
  }
}
