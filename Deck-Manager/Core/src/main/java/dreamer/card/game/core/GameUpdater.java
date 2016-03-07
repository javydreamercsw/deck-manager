package dreamer.card.game.core;

import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public abstract class GameUpdater extends UpdateRunnable {

    private static final Logger LOG
            = Logger.getLogger(GameUpdater.class.getName());
    protected boolean dbError = false;
    private String codeNameBase, dbFileName = "card_manager.mv.db";

    public GameUpdater(ICardGame game) {
        super(game);
    }

    @Override
    public void defaultUpdateLocal() {
        synchronized (this) {
            if (!localUpdated) {
                long start = System.currentTimeMillis();
                RequestProcessor RP = new RequestProcessor("Updating local", 1,
                        false);
                ProgressHandle ph = ProgressHandleFactory.createHandle(
                        "Updating Local Database.");
                RequestProcessor.Task theTask = RP.create(() -> {
                    updating = true;
                    localUpdating = true;
                    try {
                        List temp = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                                .namedQuery("CardSet.findAll");
                        LOG.log(Level.INFO, "{0} sets found in database.",
                                temp.size());
                        if (temp.isEmpty()) {
                            //Load from the default database
                            DialogDisplayer.getDefault().notifyLater(
                                    new NotifyDescriptor.Message(
                                            org.openide.util.NbBundle.getMessage(GameUpdater.class,
                                                    "initial.load").replaceAll("[x]",
                                                    getGame().getName()),
                                            NotifyDescriptor.WARNING_MESSAGE));
                            loadDefaultCards(ph);
                        }
                        updateLocal();
                    } catch (DBException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    localUpdated = true;
                    localUpdating = false; 
                });
                theTask.addTaskListener((org.openide.util.Task task) -> {
                    //Make sure that we get rid of the ProgressHandle
                    //when the task is finished
                    ph.finish();
                    LOG.log(Level.INFO, "Updating local took: {0}",
                            Tool.elapsedTime(start));
                });
                theTask.schedule(0);
            }else{
                localUpdating = false;
            }
        }
    }

    @Override
    public void defaultUpdateRemote() {
        synchronized (this) {
            long start = System.currentTimeMillis();
            RequestProcessor RP = new RequestProcessor("Updating remote", 1, false);
            ProgressHandle ph = ProgressHandleFactory.createHandle(
                    "Updating from remote data. Please wait. This can take a long time.");
            RequestProcessor.Task theTask = RP.create(() -> {
                ph.start();
                remoteUpdating = true;
                updateRemote();
            });
            theTask.addTaskListener((org.openide.util.Task task) -> {
                //Make sure that we get rid of the ProgressHandle
                //when the task is finished
                ph.finish();
                LOG.log(Level.INFO, "Updating remote took: {0}",
                        Tool.elapsedTime(start));
            });
            theTask.schedule(0);
        }
    }

    /**
     * @return the codeNameBase
     */
    public String getCodeNameBase() {
        return codeNameBase;
    }

    /**
     * @param codeNameBase the codeNameBase to set
     */
    public void setCodeNameBase(String codeNameBase) {
        this.codeNameBase = codeNameBase;
    }

    /**
     * @return the dbFileName
     */
    public String getDBFileName() {
        return dbFileName;
    }

    /**
     * @param dbFileName the dbFileName to set
     */
    public void setDBFileName(String dbFileName) {
        this.dbFileName = dbFileName;
    }

    /**
     * Load the default cards
     *
     * @param ph Progress Handle to update status.
     * @throws com.reflexit.magiccards.core.model.storage.db.DBException
     */
    public abstract void loadDefaultCards(ProgressHandle ph) throws DBException;
}
