package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import java.awt.BorderLayout;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.dreamer.event.bus.EventBusListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.util.NbBundle.Messages;
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
@TopComponent.OpenActionRegistration(displayName = "#CTL_TableViewAction",
preferredID = "TableViewTopComponent")
@Messages({
    "CTL_TableViewAction=Table View",
    "CTL_TableViewTopComponent=Table View Window",
    "HINT_TableViewTopComponent=This is a TableView window"
})
public final class TableViewTopComponent extends TopComponent
        implements ExplorerManager.Provider, EventBusListener<ICard> {

    private final ExplorerManager mgr = new ExplorerManager();
    private AbstractNode root;
    private OutlineView ov;

    public TableViewTopComponent() {
        initComponents();
        setName(Bundle.CTL_TableViewTopComponent());
        setToolTipText(Bundle.HINT_TableViewTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(getExplorerManager()));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(getExplorerManager()));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(getExplorerManager()));
        map.put("delete", ExplorerUtils.actionDelete(getExplorerManager(), true));
        this.setLayout(new BorderLayout());
        //Create the OutlineView:
        ov = new OutlineView();
        ov.setEnabled(false);

        //Set the columns to show
        updateColumns();

        //Add the OutlineView to the TopComponent:
        add(ov, BorderLayout.CENTER);

        //Set the root of the ExplorerManager:
        root = new RootNode(new IGameAllChildFactory());
        getExplorerManager().setRootContext(root);

        //Put the Nodes into the Lookup of the TopComponent,
        //so that the Properties window will be synchronized:
        associateLookup(ExplorerUtils.createLookup(getExplorerManager(), getActionMap()));
        getExplorerManager().getRootContext().setDisplayName("Available Games");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    private void updateColumns() {
        //Set the columns of the outline view,
        //using the name of the property
        //followed by the text to be displayed in the column header:
        ov.setPropertyColumns(
                "name", "Name",
                "set", "Set");
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
    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    @Override
    public void notify(ICard card) {
        System.out.println("Added card: " + card.getName());
    }
}
