package dreamer.card.game.core;

import com.dreamer.outputhandler.OutputHandler;
import com.reflexit.magiccards.core.cache.AbstractCardCache;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.IGameDataManager;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.netbeans.api.db.explorer.*;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

@ServiceProvider(service = DataBaseStateListener.class)
public class Installer extends ModuleInstall implements ActionListener, DataBaseStateListener {

    private static final Logger LOG = Logger.getLogger(Installer.class.getName());
    private final ArrayList<GameUpdateAction> updaters = new ArrayList<GameUpdateAction>();
    final ArrayList<Thread> runnables = new ArrayList<Thread>();
    private Timer timer;
    private final int period = 30000, pause = 10000;
    private final HashMap<String, String> dbProperties = new HashMap<String, String>();
    private long start;

    @Override
    public void restored() {
        //Create game cache dir
        File cacheDir = Places.getCacheSubdirectory(".Deck Manager");
        dbProperties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:file:"
                + cacheDir.getAbsolutePath()
                + "/data/card_manager");
        dbProperties.put(PersistenceUnitProperties.TARGET_DATABASE, "org.eclipse.persistence.platform.database.H2Platform");
        dbProperties.put(PersistenceUnitProperties.JDBC_PASSWORD, "test");
        dbProperties.put(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
        dbProperties.put(PersistenceUnitProperties.JDBC_USER, "deck_manager");
        OutputHandler.select("Output");
        DatabaseConnection conn;
        if (ConnectionManager.getDefault().getConnections().length == 0) {
            try {
                conn = DatabaseConnection.create(findSqlServerDriver("org.h2.Driver", "H2"),
                        dbProperties.get(PersistenceUnitProperties.JDBC_URL),
                        dbProperties.get(PersistenceUnitProperties.JDBC_USER),
                        "card_manager",
                        dbProperties.get(PersistenceUnitProperties.JDBC_PASSWORD),
                        true,
                        "Deck Manager Database");
                //Create the default connection to embedded database
                ConnectionManager.getDefault().addConnection(conn);
                LOG.log(Level.FINE, "Created default connection!");
            } catch (DatabaseException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } else {
            //Copy the settings from the defined database
            conn = ConnectionManager.getDefault().getConnections()[0];
            dbProperties.put(PersistenceUnitProperties.JDBC_URL, conn.getDatabaseURL());
            dbProperties.put(PersistenceUnitProperties.TARGET_DATABASE, conn.getDatabaseURL().startsWith("jdbc:h2")
                    ? "org.eclipse.persistence.platform.database.H2Platform" : "Auto");
            dbProperties.put(PersistenceUnitProperties.JDBC_PASSWORD, conn.getPassword());
            dbProperties.put(PersistenceUnitProperties.JDBC_DRIVER, conn.getDriverClass());
            dbProperties.put(PersistenceUnitProperties.JDBC_USER, conn.getUser());
            LOG.log(Level.FINE, "Updating from found connection!");
        }
        File cardCacheDir = new File(Places.getCacheSubdirectory(".Deck Manager").getAbsolutePath()
                + System.getProperty("file.separator") + "cache");
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
                    Lookup.getDefault().lookup(IDataBaseCardStorage.class).setDataBaseProperties(dbProperties);
                    //Start the database activities
                    LOG.log(Level.FINE, "Initializing database...");
                    try {
                        //Make sure to load the driver
                        start = System.currentTimeMillis();
                        Lookup.getDefault().lookup(ClassLoader.class).loadClass(dbProperties.get(PersistenceUnitProperties.JDBC_DRIVER));
                        Lookup.getDefault().lookup(IDataBaseCardStorage.class).initialize();
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    @Override
    public boolean closing() {
        super.closing();
        try {
            OutputHandler.output("Output", "Shutting down updaters...");
            for (Iterator<GameUpdateAction> it = updaters.iterator(); it.hasNext();) {
                GameUpdateAction updater = it.next();
                updater.shutdown();
            }

            for (Iterator<Thread> it = runnables.iterator(); it.hasNext();) {
                Thread runnable = it.next();
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

    private JDBCDriver findSqlServerDriver(String driverClass, String driverName) {
        JDBCDriver sqlSrvDrv = null;
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
        // We know that there should be at least one as this module registers it  
        for (JDBCDriver drv : drivers) {
            if (driverName == null || (driverName != null && driverName.equals(drv.getName()))) {
                sqlSrvDrv = drv;
                break;
            }
        }
        return sqlSrvDrv;
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
        for (Iterator<GameUpdateAction> it = updaters.iterator(); it.hasNext();) {
            GameUpdateAction gua = it.next();
            if (!gua.finished) {
                ready = false;
                break;
            }
        }
        if (ready) {
            for (Iterator<Thread> it = runnables.iterator(); it.hasNext();) {
                Thread t = it.next();
                if (!t.isAlive()) {
                    ready = false;
                    break;
                }
            }
        }
        if (ready) {
            //Code to be executed after all updates are complete
            for (Iterator<? extends IGameDataManager> it = Lookup.getDefault().lookupAll(IGameDataManager.class).iterator(); it.hasNext();) {
                IGameDataManager gdm = it.next();
                gdm.load();
            }
            OutputHandler.output("Output", "Done!");
        }
        return ready;
    }

    @Override
    public void initialized() {
        try {
            Lookup.getDefault().lookup(ClassLoader.class).loadClass(dbProperties.get(PersistenceUnitProperties.JDBC_DRIVER));
            LOG.info("DB ready!");
            //Code to be done after the db is ready
            LOG.log(Level.FINE, "Database initialized");
            OutputHandler.output("Output", "Database initialized");
            LOG.log(Level.FINE, "Initializing database took: {0}", Tool.elapsedTime(start));
            LOG.log(Level.FINE, "Initializing games...");
            Runnable task;
            OutputHandler.output("Output", "Starting game updaters...");
            for (Iterator<? extends ICardGame> it =
                    Lookup.getDefault().lookupAll(ICardGame.class).iterator(); it.hasNext();) {
                ICardGame game = it.next();
                new GameInitializationAction(game).actionPerformed(null);
                task = game.getUpdateRunnable();
                if (task != null) {
                    if (task instanceof IProgressAction) {
                        //Properly created to display progress in the IDE
                        updaters.add(new GameUpdateAction((IProgressAction) task));
                    } else {
                        //No progress information available
                        runnables.add(new Thread(task));
                    }
                }
            }
            OutputHandler.output("Output", "Done!");
            OutputHandler.output("Output", "Starting cache updaters...");
            for (Iterator<? extends ICardCache> it =
                    Lookup.getDefault().lookupAll(ICardCache.class).iterator(); it.hasNext();) {
                ICardCache cache = it.next();
                task = cache.getCacheTask();
                if (task != null) {
                    if (task instanceof IProgressAction) {
                        //Properly created to display progress in the IDE
                        updaters.add(new CacheUpdateAction((IProgressAction) task));
                    } else {
                        //No progress information available
                        runnables.add(new Thread(task));
                    }
                }
            }
            for (Iterator<GameUpdateAction> it = updaters.iterator(); it.hasNext();) {
                GameUpdateAction updater = it.next();
                updater.actionPerformed(null);
            }
            for (Iterator<Thread> it = runnables.iterator(); it.hasNext();) {
                Thread runnable = it.next();
                runnable.start();
            }
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
