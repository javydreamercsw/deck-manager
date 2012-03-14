package dreamer.card.game.core;

import com.dreamer.outputhandler.OutputHandler;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.CardFileUtils;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.netbeans.api.db.explorer.*;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    private static final Logger LOG = Logger.getLogger(Installer.class.getName());

    @Override
    public void restored() {
        //Create game cache dir
        File cacheDir = Places.getCacheSubdirectory(".Deck Manager");
        //Check if database is present, if not copy the default database (to avoid long initial update)
        File dbDir = new File(cacheDir.getAbsolutePath()
                + System.getProperty("file.separator") + "data");
        dbDir.mkdirs();
        if (dbDir.isDirectory() && dbDir.listFiles().length == 0) {
            LOG.log(Level.INFO, "Copying default database...");
            File db = InstalledFileLocator.getDefault().locate("deck_manager.h2.db",
                    "dreamer.card.game.core", false);
            if (db != null) {
                try {
                    CardFileUtils.copyFile(db, new File(dbDir.getAbsolutePath()
                            + System.getProperty("file.separator")
                            + "deck_manager.h2.db"));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            LOG.log(Level.INFO, "Done!");
        }
        HashMap<String, String> dbProperties = new HashMap<String, String>();
        dbProperties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:file:"
                + cacheDir.getAbsolutePath()
                + "/data/deck_manager;AUTO_SERVER=TRUE");
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
                        "Deck manager Database");
                //Create the default connection to embedded database
                ConnectionManager.getDefault().addConnection(conn);
                LOG.log(Level.INFO, "Created default connection!");
            } catch (DatabaseException ex) {
                Exceptions.printStackTrace(ex);
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
            LOG.log(Level.INFO, "Updating from found connection!");
        }
        long start = System.currentTimeMillis();
        OutputHandler.output("Output", "Initializing database...");
        Lookup.getDefault().lookup(IDataBaseCardStorage.class).setDataBaseProperties(dbProperties);
        //Start the database activities
        LOG.log(Level.FINE, "Initializing database...");
        try {
            Lookup.getDefault().lookup(IDataBaseCardStorage.class).initialize();
        } catch (DBException ex) {
            Exceptions.printStackTrace(ex);
        }
        LOG.log(Level.FINE, "Database initialized");
        OutputHandler.output("Output", "Database initialized");
        LOG.log(Level.INFO, "Initializing database took: {0}", Tool.elapsedTime(start));
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                LOG.log(Level.FINE, "Initializing games...");
                Runnable task;
                OutputHandler.output("Output", "Starting game updaters...");
                for (Iterator<? extends ICardGame> it =
                        Lookup.getDefault().lookupAll(ICardGame.class).iterator(); it.hasNext();) {
                    ICardGame game = it.next();
                    new GameInitializationAction(game).actionPerformed(null);
                    task = game.getUpdateRunnable();
                    if (task != null) {
                        OutputHandler.output("Output", "Updating: " + game.getName());
                        if (task instanceof IProgressAction) {
                            //Properly created to display progress in the IDE
                            new GameUpdateAction((IProgressAction) task).actionPerformed(null);
                        } else {
                            //No progress information available
                            new Thread(task).start();
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
                            new CacheUpdateAction((IProgressAction) task).actionPerformed(null);
                        } else {
                            //No progress information available
                            new Thread(task).start();
                        }
                    }
                }
                OutputHandler.output("Output", "Done!");
            }
        });
    }

    @Override
    public boolean closing() {
        super.closing();
        try {
            Lookup.getDefault().lookup(IDataBaseCardStorage.class).close();
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error closing database!", e);
            return false;
        }
    }

    private JDBCDriver findSqlServerDriver(String driverClass, String driverName) {
        JDBCDriver sqlSrvDrv = null;
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
        // we know that there should be at least one as this module registers it  
        for (JDBCDriver drv : drivers) {
            if ((driverName != null && driverName.equals(drv.getName())) || driverName == null) {
                sqlSrvDrv = drv;
                break;
            }
        }
        return sqlSrvDrv;
    }
}
