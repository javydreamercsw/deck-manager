package net.sourceforge.javydreamercsw.synamicd.game;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.DefaultCardGame;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.IGameCellRendererImageFactory;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = ICardGame.class)
public class SDGame extends DefaultCardGame {

    private static final Logger LOG = Logger.getLogger(SDGame.class.getName());

    @Override
    public String getName() {
        return "Magic the Gathering";
    }

    @Override
    public Image getBackCardIcon() {
        try {
            return ImageIO.read(getClass().getResource("/images/back.jpg"));
        } catch (MalformedURLException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public Runnable getUpdateRunnable() {
        return new SDUpdater();
    }

    //Obtain the image URL
    public static Image createImage(String module_id, String path, String description)
            throws MalformedURLException, Exception {
        File icon = InstalledFileLocator.getDefault().locate(path,
                module_id, false);
        URL imageURL = Utilities.toURI(icon).toURL();
        return (new ImageIcon(imageURL, description)).getImage();
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
            LOG.log(Level.SEVERE,
                    null, e);
            return false;
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm == null ? false : cm.hasAlpha();
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
            LOG.log(Level.SEVERE,
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

    @Override
    public IGameCellRendererImageFactory getCellRendererImageFactory() {
        return new IGameCellRendererImageFactory() {
            @Override
            public JLabel getRendering(String string, Object value) {
                if (value != null) {
                    List<ICardCache> impls = getCardCacheImplementations();
                    if (impls.size() > 0 && ((String) value).contains("{") && ((String) value).contains("}")) {
                        JLabel container = new JLabel();
                        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
                        ArrayList<String> values = new ArrayList<String>();
                        StringTokenizer st = new StringTokenizer((String) value, "}");
                        while (st.hasMoreTokens()) {
                            String token = st.nextToken();
                            values.add(token.substring(1));
                        }
                        for (Iterator<String> it = values.iterator(); it.hasNext();) {
                            try {
                                String v = it.next();
                                JLabel iconLabel = new JLabel(new ImageIcon((toBufferedImage(((SDCardCache) impls.get(0)).getManaIcon(v)))));
                                container.add(iconLabel);
                                if (it.hasNext()) {
                                    container.add(Box.createRigidArea(new Dimension(5, 0)));
                                }
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        return container;
                    }
                    return new JLabel(((String) value));
                } else {
                    return new JLabel();
                }
            }
        };
    }
}
