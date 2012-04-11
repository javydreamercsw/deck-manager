package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardCollection;
import com.reflexit.magiccards.core.model.ICardCollectionType;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.IGameDataManager;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.dreamer.event.bus.EventBus;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//dreamer.card.game.gui//TableView//EN",
autostore = false)
@TopComponent.Description(preferredID = "TableViewTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "dreamer.card.game.gui.TableViewTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@Messages({
    "CTL_TableViewTopComponent=Game Card List",
    "HINT_TableViewTopComponent=This is a Game Card List window"
})
@ServiceProvider(service = DataBaseStateListener.class)
public final class TableViewTopComponent extends GameCardComponent {

    @Override
    public void notify(ICardGame game) {
        //TODO: Enable on platform 7.2
        //makeBusy(true);
        try {
            IGameDataManager implementation = game.getGameDataManagerImplementation();
            if (implementation != null && !gameManagers.containsKey(game)) {
                //Create a game data manager
                gameManagers.put(game, implementation);
                //Add a table to contain the cards
                Component component = gameManagers.get(game).getComponent();
                component.addMouseListener(new MouseListener() {

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        notifySelection(e.getComponent());
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        notifyDeselection(e.getComponent());
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        notifyDeselection(e.getComponent());
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        notifySelection(e.getComponent());
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        notifyDeselection(e.getComponent());
                    }
                });
                gameTabbedPane.addTab(game.getName(), new ImageIcon(game.getGameIcon()), component);
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(game.getName()
                        + " doesn't have a Data Manager implementation. "
                        + "Some functionality won't work or will be limited.",
                        NotifyDescriptor.ERROR_MESSAGE));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        //TODO: Enable on platform 7.2
        //makeBusy(false);
    }
    private final ExplorerManager mgr = new ExplorerManager();
    private HashMap<ICardGame, IGameDataManager> gameManagers = new HashMap<ICardGame, IGameDataManager>();
    private static final Logger LOG = Logger.getLogger(TableViewTopComponent.class.getName());

    public TableViewTopComponent() {
        super();
        initComponents();
        setName(Bundle.CTL_TableViewTopComponent());
        setToolTipText(Bundle.HINT_TableViewTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
    }

    private void notifySelection(Component source) {
        for (Iterator<Entry<ICardGame, IGameDataManager>> it = gameManagers.entrySet().iterator(); it.hasNext();) {
            Entry<ICardGame, IGameDataManager> entry = it.next();
            if (entry.getValue().getComponent().equals(source)) {
                EventBus.getDefault().publish(entry.getKey());
            }
        }
    }

    private void notifyDeselection(Component source) {
        for (Iterator<Entry<ICardGame, IGameDataManager>> it = gameManagers.entrySet().iterator(); it.hasNext();) {
            Entry<ICardGame, IGameDataManager> entry = it.next();
            if (entry.getValue().getComponent().equals(source)) {
                EventBus.getDefault().remove(entry.getKey());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gameTabbedPane = new javax.swing.JTabbedPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gameTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gameTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane gameTabbedPane;
    // End of variables declaration//GEN-END:variables

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
    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    @Override
    public void initialized() {
        try {
            System.out.println("Collections:");
            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardCollection.findAll").iterator(); it.hasNext();) {
                ICardCollection cc = (ICardCollection) it.next();
                System.out.println(cc.getName());
            }
            System.out.println("Collection Types:");
            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardCollectionType.findAll").iterator(); it.hasNext();) {
                ICardCollectionType cc = (ICardCollectionType) it.next();
                System.out.println(cc.getName());
            }
        } catch (DBException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        //TODO: Enable on platform 7.2
        //makeBusy(false);
    }
}
