package dreamer.card.game.core;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.TaskListener;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class CacheUpdateAction extends GameUpdateAction {

    private static final Logger LOG = Logger.getLogger(CacheUpdateAction.class.getName());

    public CacheUpdateAction(IProgressAction runnable) {
        super(runnable);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (runnable != null) {
            final long start = System.currentTimeMillis();
            runnable.addListener(this);
            theTask = RP.create(runnable);
            theTask.addTaskListener(new TaskListener() {

                @Override
                public void taskFinished(org.openide.util.Task task) {
                    //Make sure that we get rid of the ProgressHandle
                    //when the task is finished
                    if (!finished && ph != null) {
                        ph.finish();
                    }
                    LOG.log(Level.INFO, "Updating cache took: {0}", Tool.elapsedTime(start));
                }
            });
            //this actually start the task
            theTask.schedule(0);
        }
    }

    @Override
    public void reportSize(int size) {
        if (ph == null) {
            ph = ProgressHandleFactory.createHandle(runnable.getActionName());
            //start the progresshandle the progress UI will show 500s after
            ph.start();
        }
        super.reportSize(size);
    }

    @Override
    public void reportDone() {
        super.reportDone();
        ph = null;
    }
}
