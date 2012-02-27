package dreamer.card.game.core;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public interface UpdateProgressListener {
    /**
     * Progress amount (in units of work)
     * @param amount amount to report
     */
    public void reportProgress(int amount);
    
    
    /**
     * Report that the task is done
     */
    public void reportDone();
    
    /**
     * Report the task size
     * @param size task size
     */
    public void reportSize(int size);
    
    /**
     * Change the progress message
     * @param message
     */
    public void changeMessage(String message);
}
