package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardAttributeFormatter;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import dreamer.card.game.core.Tool;
import dreamer.card.game.gui.node.ICardNode;
import dreamer.card.game.gui.node.factory.IGameChildFactory;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.RowModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//dreamer.card.game.gui//Game//EN",
        autostore = false)
@TopComponent.Description(preferredID = "GameTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false, roles = "game_view")
@ActionID(category = "Window", id = "dreamer.card.game.gui.GameTopComponent")
@ActionReference(path = "Menu/Window" /*
         * , position = 333
         */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_GameAction",
        preferredID = "GameTopComponent")
@Messages({
    "CTL_GameAction=Game",
    "CTL_GameTopComponent=Game Window",
    "HINT_GameTopComponent=This is a Game window"
})
public final class GameTopComponent extends TopComponent implements ExplorerManager.Provider {

    private ExplorerManager em = new ExplorerManager();
    private ICardGame game = null;
    private long start;
    //Kept for update purposes
    private IGameChildFactory gameFactory;
    private final static Logger LOG = Logger.getLogger(GameTopComponent.class.getSimpleName());

    public GameTopComponent() {
        initComponents();
        start = System.currentTimeMillis();
        LOG.info("Looking for available games...");
        Collection<? extends ICardGame> games = Lookup.getDefault().lookupAll(ICardGame.class);
        if (games.isEmpty()) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(
                    GameTopComponent.class,
                    "error.no_games"),
                    NotifyDescriptor.ERROR_MESSAGE));
        } else if (games.size() > 1) {
            String[] choices = new String[games.size()];
            int i = 0;
            for (Iterator<? extends ICardGame> it = games.iterator(); it.hasNext();) {
                ICardGame g = it.next();
                choices[i] = g.getName();
                i++;
            }
            DialogPanel dialogPanel = new DialogPanel();
            dialogPanel.setInstruction(NbBundle.getMessage(
                    GameTopComponent.class,
                    "select.game"));
            dialogPanel.setMessage("");
            dialogPanel.setChoices(choices);

            DialogDescriptor dd = new DialogDescriptor(dialogPanel,
                    NbBundle.getMessage(
                    GameTopComponent.class,
                    "select.game"));

            Object reply = DialogDisplayer.getDefault().notify(dd);

            if (reply == NotifyDescriptor.OK_OPTION) {
                game = games.toArray(new ICardGame[games.size()])[dialogPanel.getSelectedIndex()];
            } else if (reply == NotifyDescriptor.CANCEL_OPTION) {
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                        NbBundle.getMessage(
                        GameTopComponent.class,
                        "error.no_game_selected"),
                        NotifyDescriptor.ERROR_MESSAGE));
                LifecycleManager.getDefault().saveAll();
                LifecycleManager.getDefault().exit();
            }
        } else {
            game = games.toArray(new ICardGame[games.size()])[0];
        }
        LOG.log(Level.INFO, "Time getting available games: {0}", Tool.elapsedTime(start));
        LOG.info("Loading game...");
        start = System.currentTimeMillis();
        try {
            loadGame();
        } catch (DBException ex) {
            LOG.log(Level.SEVERE, "Error loading game!", ex);
        }
        LOG.log(Level.INFO, "Time loading game: {0}", Tool.elapsedTime(start));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        gamePane = new OutlineView();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 357;
        gridBagConstraints.ipady = 255;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 11, 10);
        add(gamePane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane gamePane;
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
        return em;
    }

    /**
     * Load the current game into the application
     */
    private void loadGame() throws DBException {
        if (gameFactory == null) {
            gameFactory = new IGameChildFactory();
        }
        if (game != null) {
            Node root = new AbstractNode(Children.create(gameFactory, true));
            setName(Bundle.CTL_GameTopComponent());
            setToolTipText(Bundle.HINT_GameTopComponent());
            start = System.currentTimeMillis();
            Lookup.getDefault().lookup(IDataBaseCardStorage.class).initialize();
            final List<String> columns = game.getColumns();
            LOG.log(Level.INFO, "Getting game columns: {0}", Tool.elapsedTime(start));
            String[] properties = new String[columns.size() * 2];
            int i = 0;
            start = System.currentTimeMillis();
            for (Iterator<String> it = columns.iterator(); it.hasNext();) {
                String prop = it.next();
                properties[i] = prop.toLowerCase(Locale.getDefault());
                i++;
                properties[i] = prop;
                i++;
            }
            ((OutlineView) gamePane).setPropertyColumns(properties);
            ((OutlineView) gamePane).getOutline().setDefaultRenderer(
                    String.class,
                    new ICardOutlineCellRenderer(game));
            ((OutlineView) gamePane).getOutline().setModel(
                    DefaultOutlineModel.createOutlineModel(
                    new GameTreeModel(root),
                    new GameRowModel(columns),
                    true, NbBundle.getMessage(
                    GameTopComponent.class,
                    "general.set")));
            LOG.log(Level.INFO, "Preparing outline: {0}", Tool.elapsedTime(start));
            em.setRootContext(root);
            associateLookup(ExplorerUtils.createLookup(getExplorerManager(), getActionMap()));
        }
    }

    private static class GameTreeModel implements TreeModel {

        private final Node root;

        public GameTreeModel(Node root) {
            this.root = root;
        }

        @Override
        public Object getRoot() {
            return root;
        }

        @Override
        public Object getChild(Object parent, int index) {
            Node n = (Node) parent;
            return n.getChildren().getNodeAt(index);
        }

        @Override
        public int getChildCount(Object parent) {
            Node n = (Node) parent;
            return n.getChildren().getNodesCount();
        }

        @Override
        public boolean isLeaf(Object node) {
            if (node instanceof Node) {
                Node n = (Node) node;
                return n.isLeaf();
            } else {
                return true;
            }
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            //Do nothing
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            Node n = (Node) parent;
            int i = 0;
            for (Node c : n.getChildren().getNodes()) {
                if (c.equals(child)) {
                    return i;
                }
                i++;
            }
            return -1;
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            //Do nothing
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            //Do nothing
        }
    }

    private class GameRowModel implements RowModel {

        private final List<String> columns;

        public GameRowModel(List<String> columns) {
            this.columns = columns;
        }

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public Object getValueFor(Object o, int i) {
            Object result = null;
            if (o instanceof ICardNode) {
                ICardNode node = (ICardNode) o;
                String columnName = columns.get(i).toLowerCase(Locale.getDefault()).replaceAll("_", "");
                if (columnName.equals("name")) {
                    result = node.getCard().getName();
                } else if (columnName.equals("cardid")) {
                    result = node.getCard().getCardId();
                } else if (columnName.equals("set")) {
                    result = node.getCard().getSetName();
                } else {
                    result = node.getAttribute(getColumnName(i));
                }
                for (ICardAttributeFormatter formatter : game.getGameCardAttributeFormatterImplementations()) {
                    if (result instanceof String) {
                        String string = (String) result;
                        result = formatter.format(string);
                        break;
                    }
                }
            }
            return result == null ? "" : result.toString();
        }

        @Override
        public Class getColumnClass(int i) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(Object o, int i) {
            return false;
        }

        @Override
        public void setValueFor(Object o, int i, Object o1) {
            //Do nothing
        }

        @Override
        public String getColumnName(int column) {
            String name = columns.get(column);
            if (name.equals("CardId")) {
                return "Card Id";
            } else {
                return name;
            }
        }
    }
}
