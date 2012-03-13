package dreamer.card.game.core;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.openide.modules.InstalledFileLocator;

/**
 * Various commonly used operations.
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public final class Tool {

    private Tool() {
    }

    /**
     * Calculates how many instances of the what strings exists within where.
     *
     * @param where String to search
     * @param what What to search
     * @return Amount of instances
     */
    public static int instancesOfString(String where, String what) {
        int counter = 0, lastIndex = 0;
        if (where != null && !where.isEmpty() && what != null && !what.isEmpty()) {
            while ((lastIndex = where.indexOf(what, lastIndex) + 1) != 0) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Convert an exception into a string
     *
     * @param ex
     * @return exception as a string
     */
    public static String exceptionToString(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Convert an error into a string
     *
     * @param ex
     * @return error as a string
     */
    public static String exceptionToString(Error ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Centralize component on the middle of screen
     *
     * @param component
     */
    public static void centralizeDialog(JDialog component) {
        //Centralize on screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = component.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        component.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }

    /**
     * Calculated elapsed time from provided long to current time
     *
     * @param start
     * @return elapsed time
     */
    public static String elapsedTime(long start) {
        Interval interval = new Interval(start, System.currentTimeMillis());
        Period period = interval.toPeriod();
        StringBuilder sb = new StringBuilder();
        if (period.getYears() > 0) {
            if (!sb.toString().isEmpty()) {
                sb.append(", ");
            }
            sb.append(period.getYears()).append(" years");
        }
        if (period.getMonths() > 0) {
            if (!sb.toString().isEmpty()) {
                sb.append(", ");
            }
            sb.append(period.getMonths()).append(" months");
        }
        if (period.getDays() > 0) {
            if (!sb.toString().isEmpty()) {
                sb.append(", ");
            }
            sb.append(period.getDays()).append(" days");
        }
        if (period.getHours() > 0) {
            if (!sb.toString().isEmpty()) {
                sb.append(", ");
            }
            sb.append(period.getHours()).append(" hours");
        }
        if (period.getMinutes() > 0) {
            if (!sb.toString().isEmpty()) {
                sb.append(", ");
            }
            sb.append(period.getMinutes()).append(" minutes");
        }
        if (period.getSeconds() > 0) {
            if (!sb.toString().isEmpty()) {
                sb.append(", ");
            }
            sb.append(period.getSeconds()).append(" seconds");
        }
        if (period.getMillis() > 0) {
            if (!sb.toString().isEmpty()) {
                sb.append(", ");
            }
            sb.append(period.getMillis()).append(" milliseconds");
        }
        return sb.toString().isEmpty() ? "Negligible" : sb.toString();
    }

    /**
     * Delete a folder and all its contents
     *
     * @param folder Folder to delete
     */
    public static void deleteFolder(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            for (File temp : folder.listFiles()) {
                if (temp.isDirectory()) {
                    deleteFolder(temp);
                } else {
                    temp.delete();
                }
            }
            folder.delete();
        }
    }

    //Obtain the image URL
    public static Image createImage(String module_id, String path, String description)
            throws MalformedURLException, Exception {
        File icon = InstalledFileLocator.getDefault().locate(path,
                module_id, false);
        URL imageURL = icon.toURI().toURL();

        if (imageURL == null) {
            throw new Exception("Resource not found: " + path);
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
    
    /**
     * Load image
     * @param comp Component
     * @param image Image to load
     * @return ImageIcon
     */
    public static ImageIcon loadImage(Component comp, Image image) {
        MediaTracker tracker = new MediaTracker(comp);
        tracker.addImage(image, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
            Logger.getLogger(Tool.class.getSimpleName()).log(Level.FINE, 
                    "Interrupted while loading Image", false);
        }
        return new ImageIcon(image);
    }
}
