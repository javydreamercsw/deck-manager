package dreamer.card.game.core;

import com.dreamer.outputhandler.OutputHandler;
import com.reflexit.magiccards.core.model.ICardGame;
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
final class GameInitializationAction implements ActionListener {

    private final RequestProcessor RP;
    private RequestProcessor.Task theTask = null;
    private final ICardGame game;
    private static final Logger LOG = Logger.getLogger(GameInitializationAction.class.getName());

    public GameInitializationAction(ICardGame game) {
        this.game = game;
        RP = new RequestProcessor("Game Initialization for: " + game.getName(), 1, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Initializing game: " + game.getName());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LOG.log(Level.FINE, "Initializing game: {0}", game.getName());
                OutputHandler.output("Output", "Initializing game: " + game.getName());
                game.init();
                LOG.log(Level.FINE, "Done!");
            }
        };
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
