package dreamer.card.game.core;

import com.dreamer.outputhandler.OutputHandler;
import dreamer.card.game.ICardGame;
import dreamer.card.game.storage.IDataBaseManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

public class Installer extends ModuleInstall {

    private static final Logger LOG = Logger.getLogger(Installer.class.getName());

    @Override
    public void restored() {
        HashMap<String, String> dbProperties = new HashMap<String, String>();
        dbProperties.put(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:file:~/.Deck Manager/data/card_manager");
        dbProperties.put(PersistenceUnitProperties.TARGET_DATABASE, "org.eclipse.persistence.platform.database.H2Platform");
        dbProperties.put(PersistenceUnitProperties.JDBC_PASSWORD, "test");
        dbProperties.put(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
        dbProperties.put(PersistenceUnitProperties.JDBC_USER, "card_manager");
        OutputHandler.output("Output", "Initializing database...");
        Lookup.getDefault().lookup(IDataBaseManager.class).setDataBaseProperties(dbProperties);
        //Start the database activities
        LOG.log(Level.FINE, "Initializing database...");
        Lookup.getDefault().lookup(IDataBaseManager.class).getEntityManagerFactory();
        LOG.log(Level.FINE, "Database initialized");
        OutputHandler.output("Output", "Database initialized");
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
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
}
