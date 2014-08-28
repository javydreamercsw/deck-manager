package dreamer.card.game.core;

import com.dreamer.outputhandler.OutputHandler;
import com.reflexit.magiccards.core.cache.AbstractCardCache;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

@ServiceProvider(service = DataBaseStateListener.class)
public class Installer extends ModuleInstall implements ActionListener,
        DataBaseStateListener, WindowSystemListener {

    private static final Logger LOG
            = Logger.getLogger(Installer.class.getName());
    private final ArrayList<GameUpdateAction> updaters
            = new ArrayList<>();
    final ArrayList<Thread> runnables = new ArrayList<>();
    private Timer timer;
    private final int period = 30000, pause = 10000;
    private final HashMap<String, String> dbProperties
            = new HashMap<>();
    private long start;
    private boolean waitDBInit = true;

    @Override
    public void restored() {
        //Create game cache dir
        dbProperties.put(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
        OutputHandler.select("Output");
        File cardCacheDir = new File(MessageFormat.format("{0}{1}cache",
                Places.getCacheSubdirectory("Deck Manager").getAbsolutePath(),
                System.getProperty("file.separator")));
        //Create game cache dir
        if (!cardCacheDir.exists()) {
            cardCacheDir.mkdirs();
        }
        AbstractCardCache.setCachingEnabled(true);
        AbstractCardCache.setLoadingEnabled(true);
        AbstractCardCache.setCacheDir(cardCacheDir);
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                try {
                    //Timer for inactivity background work
                    timer = new Timer(period, Installer.this);
                    timer.setInitialDelay(pause);
                    timer.start();
                    OutputHandler.output("Output", "Initializing database...");
                    Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                            .setDataBaseProperties(dbProperties);
                    //Start the database activities
                    LOG.log(Level.FINE, "Initializing database...");
                    try {
                        //Make sure to load the driver
                        start = System.currentTimeMillis();
                        Lookup.getDefault().lookup(ClassLoader.class)
                                .loadClass(dbProperties.get(PersistenceUnitProperties.JDBC_DRIVER));
                        LOG.log(Level.FINE,
                                "Succesfully loaded driver: {0}",
                                dbProperties.get(PersistenceUnitProperties.JDBC_DRIVER));
                        Lookup.getDefault().lookup(IDataBaseCardStorage.class).initialize();
                        while (waitDBInit) {
                            Thread.sleep(100);
                        }
                    } catch (ClassNotFoundException | DBException | InterruptedException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                "Unable to connect to database. Please restart application", NotifyDescriptor.ERROR_MESSAGE));
                    }
                } catch (IllegalArgumentException | SecurityException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public boolean closing() {
        super.closing();
        try {
            OutputHandler.output("Output", "Shutting background tasks...");
            for (GameUpdateAction updater : updaters) {
                updater.shutdown();
            }
            for (Thread runnable : runnables) {
                runnable.interrupt();
            }
            OutputHandler.output("Output", "Done!");
            Lookup.getDefault().lookup(IDataBaseCardStorage.class).close();
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error shuting down!", e);
            return false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (afterUpdates()) {
            //We are done
            timer.stop();
        } else {
            //Make sure to retry in case we got an update.
            timer.restart();
        }
    }

    private boolean afterUpdates() {
        boolean ready = true;
        for (GameUpdateAction gua : updaters) {
            if (!gua.finished) {
                ready = false;
                break;
            }
        }
        if (ready) {
            for (Thread t : runnables) {
                if (!t.isAlive()) {
                    ready = false;
                    break;
                }
            }
            OutputHandler.output("Output", "Executing after update code...");
            //TODO: Code to be executed after all updates are complete
            OutputHandler.output("Output", "Done!");
        }
        return ready;
    }

    @Override
    public void initialized() {
        LOG.fine("DB ready!");
        waitDBInit = false;
        //Code to be done after the db is ready
        LOG.log(Level.FINE, "Database initialized");
        OutputHandler.output("Output", "Database initialized");
        LOG.log(Level.FINE, "Initializing database took: {0}",
                Tool.elapsedTime(start));
        LOG.log(Level.FINE, "Initializing games...");
        Runnable task;
        OutputHandler.output("Output", "Starting game updaters...");
        for (ICardGame game : Lookup.getDefault().lookupAll(ICardGame.class)) {
            new GameInitializationAction(game).actionPerformed(null);
            task = game.getUpdateRunnable();
            if (task != null) {
                if (task instanceof IProgressAction) {
                    //Properly created to display progress in the IDE
                    updaters.add(new GameUpdateAction((IProgressAction) task));
                } else {
                    //No progress information available
                    runnables.add(new Thread(task,
                            MessageFormat.format("{0} game updater",
                                    game.getName())));
                }
            }
        }
        OutputHandler.output("Output", "Done!");
        OutputHandler.output("Output", "Starting cache updaters...");
        for (ICardCache cache : Lookup.getDefault().lookupAll(ICardCache.class)) {
            task = cache.getCacheTask();
            if (task != null) {
                if (task instanceof IProgressAction) {
                    //Properly created to display progress in the IDE
                    updaters.add(new CacheUpdateAction((IProgressAction) task));
                } else {
                    //No progress information available
                    runnables.add(new Thread(task,
                            MessageFormat.format("{0} cache updater",
                                    cache.getGameName())));
                }
            }
        }
        for (GameUpdateAction updater : updaters) {
            updater.actionPerformed(null);
        }
        for (Thread runnable : runnables) {
            runnable.start();
        }
        OutputHandler.output("Output", "Done!");
    }

    @Override
    public void beforeLoad(WindowSystemEvent event) {
        String role = NbPreferences.forModule(TopComponent.class).get("currentScreen", "game_view");
        WindowManager.getDefault().setRole(role);
        WindowManager.getDefault().removeWindowSystemListener(this);
    }

    @Override
    public void afterLoad(WindowSystemEvent wse) {
        //Do nothing
    }

    @Override
    public void beforeSave(WindowSystemEvent wse) {
        //Do nothing
    }

    @Override
    public void afterSave(WindowSystemEvent wse) {
        //Do nothing
    }
}
