package dreamer.card.game.gui.node;

import java.awt.*;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardSet;

import dreamer.card.game.core.Tool;
import dreamer.card.game.gui.node.factory.ICardChildFactory;

/**
 * Represents a ICardSet element within the system
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public final class ICardSetNode extends BeanNode {

    private final ICardSet set;
    private static final Logger LOG
            = Logger.getLogger(ICardSetNode.class.getName());
    private Image image = null;

    public ICardSetNode(ICardSet set, ICardChildFactory childFactory)
            throws IntrospectionException {
        super(set, Children.create(childFactory, false),
                Lookups.singleton(set));
        setDisplayName(set.getName());
        this.set = set;
        //Retrieve icon in advance
        getIcon(0);
    }

    /**
     * @return the set
     */
    public ICardSet getSet() {
        return set;
    }

    @Override
    public Image getIcon(int type) {
        if (image == null) {
            ICardCache cache = Lookup.getDefault().lookup(ICardCache.class);
            if (cache == null) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                                "Unable to find cache!",
                                NotifyDescriptor.WARNING_MESSAGE));
            } else {
                //Try to get from local files
                File imageFile = new File(cache.getSetIconPath(set));
                if (imageFile.exists()) {
                    try {
                        //The file is there, lets' load it!
                        image = (new ImageIcon(Utilities.toURI(imageFile).toURL(),
                                "icon")).getImage();
                    } catch (MalformedURLException ex) {
                      LOG.log(Level.SEVERE, null, ex);
                    }
                } else {
                    //File not there, load from internet.
                    try {
                        ImageIcon loadImage = Tool.loadImage(new JFrame(),
                                cache.getSetIcon(set));
                        if (loadImage != null) {
                            image = loadImage.getImage();
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
          }
        }
        return image;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    @Override
    public boolean canDestroy() {
        return false;
    }
}
