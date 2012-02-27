package dreamer.card.game.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private final ProgressHandle ph;

    public GameUpdateAction(IProgressAction runnable) {
        RP = new RequestProcessor("Updater", 1, false);
        this.runnable = runnable;
        ph = ProgressHandleFactory.createHandle(runnable.getActionName());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (runnable != null) {
            runnable.addListener(this);
            theTask = RP.create(runnable);
            theTask.addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(org.openide.util.Task task) {
                    //Make sure that we get rid of the ProgressHandle
                    //when the task is finished
                    ph.finish();
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
        ph.progress(amount);
    }

    @Override
    public void reportDone() {
        ph.finish();
    }

    @Override
    public void reportSize(int size) {
        ph.switchToDeterminate(size);
    }

    @Override
    public void changeMessage(String message) {
        ph.progress(message);
    }
}
