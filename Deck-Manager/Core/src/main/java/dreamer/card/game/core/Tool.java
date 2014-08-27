package dreamer.card.game.core;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
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
import org.openide.util.Utilities;

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
        component.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
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
        URL imageURL = Utilities.toURI(icon).toURL();
        return (new ImageIcon(imageURL, description)).getImage();
    }

    /**
     * Load image
     *
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
                    "Interrupted while loading Image. " + e, false);
        }
        return new ImageIcon(image);
    }

    // This method returns a buffered image with the contents of an image
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();

        // Determine if the image has transparent pixels; for this method's
        // implementation, see Determining If an Image Has Transparent Pixels
        boolean hasAlpha = hasAlpha(image);

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
            Logger.getLogger(Tool.class.getSimpleName()).log(Level.SEVERE,
                    "The system does not have a screen", e);

        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

    // This method returns true if the specified image has transparent pixels
    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) image;
            return bimage.getColorModel().hasAlpha();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            Logger.getLogger(Tool.class.getSimpleName()).log(Level.SEVERE,
                    null, e);
            return false;
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm == null ? false : cm.hasAlpha();
    }
}
