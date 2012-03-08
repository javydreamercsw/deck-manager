package dreamer.card.game.core;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public interface IProgressAction extends Runnable {

    /**
     * @return the size
     */
    int getSize();

    /**
     * Add a listener
     * @param listener to add
     */
    void addListener(UpdateProgressListener listener);

    /**
     * Remove a listener
     * @param listener to remove
     */
    void removeListener(UpdateProgressListener listener);
    
    /**
     * Name to display in progress handle
     * @return 
     */
    String getActionName();
    
    /**
     * Set the task size
     * @param size to set to
     */
    void setSize(int size);
    
    /**
     * Report task size to listeners
     * @param size size to report
     */
    void reportSize(int size);
    
    /**
     * Report progress to listeners
     * @param amount
     */
    void reportProgress(int amount);
    
    /**
     * Report you are done
     */
    void reportDone();
    
    /**
     * Update the progress message
     * @param message progress message
     */
    void updateProgressMessage(String message);
    
    /**
     * Report that the task is suspended
     */
    void reportSuspendProgress();
    
    /**
     * Report that the task is resumed
     */
    void reportResumeProgress();
}
