package dreamer.card.game.core;

import com.dreamer.outputhandler.OutputHandler;
import dreamer.card.game.ICardGame;
import dreamer.card.game.storage.IDataBaseManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.netbeans.api.db.explorer.*;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    private static final Logger LOG = Logger.getLogger(Installer.class.getName());

    @Override
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> dbProperties = new HashMap<String, String>();
                dbProperties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:file:~/.Deck Manager/data/card_manager;AUTO_SERVER=TRUE");
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
                }
                long start = System.currentTimeMillis();
                OutputHandler.output("Output", "Initializing database...");
                Lookup.getDefault().lookup(IDataBaseManager.class).setDataBaseProperties(dbProperties);
                //Start the database activities
                LOG.log(Level.FINE, "Initializing database...");
                Lookup.getDefault().lookup(IDataBaseManager.class).getEntityManagerFactory();
                LOG.log(Level.FINE, "Database initialized");
                OutputHandler.output("Output", "Database initialized");
                LOG.log(Level.INFO, "Initializing games took: {0}", Tool.elapsedTime(start));
                LOG.log(Level.FINE, "Initializing games...");
                for (Iterator<? extends ICardGame> it = Lookup.getDefault().lookupAll(ICardGame.class).iterator(); it.hasNext();) {
                    ICardGame game = it.next();
                    new GameInitializationAction(game).actionPerformed(null);
                    if (game.getUpdateRunnable() != null) {
                        OutputHandler.output("Output", "Updating: " + game.getName());
                        if (game.getUpdateRunnable() instanceof IProgressAction) {
                            //Properly created to display progress in the IDE
                            new GameUpdateAction((IProgressAction) game.getUpdateRunnable()).actionPerformed(null);
                        } else {
                            //No progress information available
                            new Thread(game.getUpdateRunnable()).start();
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean closing() {
        super.closing();
        try {
            Lookup.getDefault().lookup(IDataBaseManager.class).close();
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error colosing database!", e);
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
