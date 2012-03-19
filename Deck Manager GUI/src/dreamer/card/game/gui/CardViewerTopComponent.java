/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamer.card.game.gui;

import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import dreamer.card.game.core.Tool;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.dreamer.event.bus.EventBus;
import org.dreamer.event.bus.EventBusListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//dreamer.card.game.gui//CardViewer//EN",
autostore = false)
@TopComponent.Description(preferredID = "CardViewerTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "dreamer.card.game.gui.CardViewerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_CardViewerAction",
preferredID = "CardViewerTopComponent")
@Messages({
    "CTL_CardViewerAction=Card Viewer",
    "CTL_CardViewerTopComponent=Card Viewer Window",
    "HINT_CardViewerTopComponent=Card Viewer window"
})
public final class CardViewerTopComponent extends TopComponent implements EventBusListener<ICard> {

    public CardViewerTopComponent() {
        initComponents();
        setName(Bundle.CTL_CardViewerTopComponent());
        setToolTipText(Bundle.HINT_CardViewerTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
        //Set up the Lookp listener stuff
        EventBus.getDefault().subscribe(ICard.class, CardViewerTopComponent.this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cardLabel = new javax.swing.JLabel();

        cardLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(cardLabel, org.openide.util.NbBundle.getMessage(CardViewerTopComponent.class, "CardViewerTopComponent.cardLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cardLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cardLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void notify(ICard card) {
        if (card != null) {
            try {
                HashMap parameters = new HashMap();
                parameters.put("name", card.getSetName());
                List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", parameters);
                if (result.isEmpty()) {
                    cardLabel.setIcon(null);
                    cardLabel.setText("No card selected");
                } else {
                    ICardSet cs = (ICardSet) result.get(0);
                    ICardCache cache = null;
                    for (Iterator<? extends ICardGame> it = Lookup.getDefault().lookupAll(ICardGame.class).iterator(); it.hasNext();) {
                        ICardGame game = it.next();
                        if (game.getName().equals(cs.getGameName())) {
                            List<ICardCache> impl = game.getCardCacheImplementations();
                            if (!impl.isEmpty()) {
                                cache = impl.get(0);
                            }
                        }
                    }
                    ImageIcon icon = Tool.loadImage(this, ImageIO.read(cache.getCardImage(card, cs, cache.createRemoteImageURL(card, Editions.getInstance().getEditionByName(cs.getName())), true, false)));
                    icon.getImage().flush();
                    cardLabel.setIcon(icon);
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CannotDetermineSetAbbriviation ex) {
                Exceptions.printStackTrace(ex);
            } catch (DBException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
