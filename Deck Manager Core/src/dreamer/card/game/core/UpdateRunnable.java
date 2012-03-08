package dreamer.card.game.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public abstract class UpdateRunnable implements IProgressAction {

    private int size, progress = 0;
    private ArrayList<UpdateProgressListener> listeners = new ArrayList<UpdateProgressListener>();
    private static final Logger LOG = Logger.getLogger(UpdateRunnable.class.getName());

    public UpdateRunnable() {
    }

    @Override
    public void addListener(UpdateProgressListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void removeListener(UpdateProgressListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    @Override
    public void setSize(int size) {
        LOG.log(Level.FINE, "Setting size to: {0}", size);
        this.size = size;
        reportSize(getSize());
    }

    @Override
    public void reportSize(int size) {
        for (Iterator<UpdateProgressListener> it = listeners.iterator(); it.hasNext();) {
            UpdateProgressListener listener = it.next();
            listener.reportSize(size);
        }
    }

    @Override
    public void reportProgress(int amount) {
        for (Iterator<UpdateProgressListener> it = listeners.iterator(); it.hasNext();) {
            UpdateProgressListener listener = it.next();
            listener.reportProgress(amount);
        }
    }
    
    @Override
    public void reportDone() {
        for (Iterator<UpdateProgressListener> it = listeners.iterator(); it.hasNext();) {
            UpdateProgressListener listener = it.next();
            listener.reportDone();
        }
    }

    /**
     * @return the progress
     */
    public int getProgress() {
        return progress;
    }

    public void increaseProgress(int amount) {
        progress += amount;
        reportProgress(progress);
    }

    public void increaseProgress() {
        increaseProgress(1);
    }

    @Override
    public void updateProgressMessage(String message) {
        for (Iterator<UpdateProgressListener> it = listeners.iterator(); it.hasNext();) {
            UpdateProgressListener listener = it.next();
            listener.changeMessage(message);
        }
    }
    
    @Override
    public void reportSuspendProgress() {
        for (Iterator<UpdateProgressListener> it = listeners.iterator(); it.hasNext();) {
            UpdateProgressListener listener = it.next();
            listener.suspend();
        }
    }
    
    @Override
    public void reportResumeProgress(){
        for (Iterator<UpdateProgressListener> it = listeners.iterator(); it.hasNext();) {
            UpdateProgressListener listener = it.next();
            listener.resume();
        }
    }
}
