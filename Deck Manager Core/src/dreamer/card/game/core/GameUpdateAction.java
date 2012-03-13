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
public final class GameUpdateAction implements UpdateProgressListener, ActionListener {

    private final RequestProcessor RP;
    private RequestProcessor.Task theTask = null;
    private IProgressAction runnable;
    private ProgressHandle ph;
    private static final Logger LOG = Logger.getLogger(GameUpdateAction.class.getName());
    private int currentProgress = 0;
    private boolean finished = false;

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
        ph.progress(currentProgress);
    }

    @Override
    public void reportDone() {
        ph.finish();
        finished = true;
    }

    @Override
    public void reportSize(int size) {
        ph.switchToDeterminate(size);
    }

    @Override
    public void changeMessage(String message) {
        ph.progress(message);
    }

    @Override
    public void resume() {
        ph.progress(currentProgress);
    }

    @Override
    public void suspend() {
        ph.suspend(null);
    }
}
