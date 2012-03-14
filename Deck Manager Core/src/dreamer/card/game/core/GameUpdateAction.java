package dreamer.card.game.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class GameUpdateAction implements UpdateProgressListener, ActionListener {

    protected final RequestProcessor RP;
    protected RequestProcessor.Task theTask = null;
    protected IProgressAction runnable;
    protected ProgressHandle ph;
    private static final Logger LOG = Logger.getLogger(GameUpdateAction.class.getName());
    protected int currentProgress = 0;
    protected boolean finished = false;

    public GameUpdateAction(IProgressAction runnable) {
        RP = new RequestProcessor("Updater", 1, false);
        this.runnable = runnable;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (runnable != null) {
            final long start = System.currentTimeMillis();
            runnable.addListener(this);
            ph = ProgressHandleFactory.createHandle(runnable.getActionName());
            theTask = RP.create(runnable);
            theTask.addTaskListener(new TaskListener() {

                @Override
                public void taskFinished(org.openide.util.Task task) {
                    //Make sure that we get rid of the ProgressHandle
                    //when the task is finished
                    if (!finished) {
                        ph.finish();
                    }
                    LOG.log(Level.INFO, "Updating database took: {0}", Tool.elapsedTime(start));
                }
            });
            //start the progresshandle the progress UI will show 500s after
            ph.start();
            //this actually start the task
            theTask.schedule(0);
        }
    }

    @Override
    public void reportProgress(int amount) {
        currentProgress = amount;
        if (ph != null) {
            ph.progress(currentProgress);
        }
    }

    @Override
    public void reportDone() {
        if (ph != null) {
            ph.finish();
        }
        finished = true;
    }

    @Override
    public void reportSize(int size) {
        if (ph != null) {
            ph.switchToDeterminate(size);
        }
    }

    @Override
    public void changeMessage(String message) {
        if (ph != null) {
            ph.progress(message);
        }
    }

    @Override
    public void resume() {
        if (ph != null) {
            ph.progress(currentProgress);
        }
    }

    @Override
    public void suspend() {
        if (ph != null) {
            ph.suspend(null);
        }
    }
}
