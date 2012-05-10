package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardCollection;
import com.reflexit.magiccards.core.model.ICardCollectionType;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.IGameDataManager;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
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
@TopComponent.Registration(mode = "editor", openAtStartup = true, roles = "game_view")
@ActionID(category = "Window", id = "dreamer.card.game.gui.TableViewTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@Messages({
    "CTL_TableViewTopComponent=Game Card List",
    "HINT_TableViewTopComponent=This is a Game Card List window"
})
@ServiceProvider(service = DataBaseStateListener.class)
public final class TableViewTopComponent extends TopComponent
        implements DataBaseStateListener, LookupListener {

    private HashMap<ICardGame, IGameDataManager> gameManagers = new HashMap<ICardGame, IGameDataManager>();
    private static final Logger LOG = Logger.getLogger(TableViewTopComponent.class.getName());
    private Lookup.Result<ICardGame> result = Lookup.getDefault().lookupResult(ICardGame.class);
    private boolean dbInit = false;

    public TableViewTopComponent() {
        super();
        initComponents();
        setName(Bundle.CTL_TableViewTopComponent());
        setToolTipText(Bundle.HINT_TableViewTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        result.allItems();
        result.addLookupListener(TableViewTopComponent.this);
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Lookup.Result res = (Lookup.Result) le.getSource();
        Collection instances = res.allInstances();

        if (!instances.isEmpty()) {
            Iterator it = instances.iterator();
            while (it.hasNext()) {
                Object next = it.next();
                if (next instanceof ICardGame) {
                    ICardGame iCardGame = (ICardGame) next;
                    LOG.log(Level.INFO, "Received notification of game: {0}", iCardGame.getName());
                    if (dbInit) {
                        update();
                    }
                }
            }
        }
    }

    private void update() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ICardGame game : Lookup.getDefault().lookupAll(ICardGame.class)) {
                    //TODO: Enable on platform 7.2
                    //makeBusy(true);
                    IGameDataManager implementation = game.getGameDataManagerImplementation();
                    if (implementation != null && !gameManagers.containsKey(game)) {
                        //Create a game data manager
                        gameManagers.put(game, implementation);
                        //Add a table to contain the cards
                        Component component = gameManagers.get(game).getComponent();
                        ImageIcon icon = new ImageIcon(game.getGameIcon());
                        String name = game.getName();
//                        javax.swing.JFrame jFrame = new javax.swing.JFrame(name);
//                        javax.swing.JTabbedPane jtp= new javax.swing.JTabbedPane();
//                        jtp.addTab(name, icon, component);
//                        jFrame.add(jtp);
//                        jFrame.setVisible(true);
//                        jFrame.setIconImage(icon.getImage());
//                        jFrame.setSize(500, 250);
                        gameTabbedPane.addTab(name, icon, component);
                    } else {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                game.getName()
                                + " doesn't have a Data Manager implementation. "
                                + "Some functionality won't work or will be limited.",
                                NotifyDescriptor.ERROR_MESSAGE));
                    }
                    //TODO: Enable on platform 7.2
                    //makeBusy(false);
                }
            }
        }).start();
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
    public void initialized() {
        dbInit = true;
        try {
            update();
            System.out.println("Collections:");




            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardCollection.findAll").iterator(); it.hasNext();) {
                ICardCollection cc = (ICardCollection) it.next();
                System.out.println(cc.getName());
            }

            System.out.println(
                    "Collection Types:");
            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardCollectionType.findAll").iterator();
                    it.hasNext();) {
                ICardCollectionType cc = (ICardCollectionType) it.next();
                System.out.println(cc.getName());
            }
        } catch (DBException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
