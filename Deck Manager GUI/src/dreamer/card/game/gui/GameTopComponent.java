package dreamer.card.game.gui;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.DefaultCardGame;
import com.reflexit.magiccards.core.model.ICardAttribute;
import com.reflexit.magiccards.core.model.ICardAttributeFormatter;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.awt.Image;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.RowModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//dreamer.card.game.gui//Game//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "GameTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "dreamer.card.game.gui.GameTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_GameAction",
preferredID = "GameTopComponent")
@Messages({
    "CTL_GameAction=Game",
    "CTL_GameTopComponent=Game Window",
    "HINT_GameTopComponent=This is a Game window"
})
public final class GameTopComponent extends TopComponent implements ExplorerManager.Provider {

    private ExplorerManager em = new ExplorerManager();
    private ICardGame game = new DefaultCardGame() {
        @Override
        public Runnable getUpdateRunnable() {
            return null;
        }

        @Override
        public Image getBackCardIcon() {
            return null;
        }

        @Override
        public Image getGameIcon() {
            try {
                return Lookup.getDefault().lookup(ICardCache.class).getGameIcon((ICardGame) this);
            } catch (IOException ex) {
                return null;
            }
        }

        @Override
        public String getName() {
            return "Magic the Gathering";
        }
    };

    public GameTopComponent() {
        initComponents();
        Node root = null;
        try {
            root = new IGameNode(game, new ICardSetChildFactory((ICardGame) game));
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
        }
        setName(Bundle.CTL_GameTopComponent());
        setToolTipText(Bundle.HINT_GameTopComponent());
        final List<String> columns = getColumns();
        String[] properties = new String[columns.size() * 2];
        int i = 0;
        for (Iterator<String> it = columns.iterator(); it.hasNext();) {
            String prop = it.next();
            properties[i] = prop.toLowerCase(Locale.getDefault());
            i++;
            properties[i] = prop;
            i++;
        }
        ((OutlineView) gamePane).setPropertyColumns(properties);
        ((OutlineView) gamePane).getOutline().setModel(DefaultOutlineModel.createOutlineModel(
                new GameTreeModel(root),
                new GameRowModel(columns),
                //TODO: use resource bundle
                true, "Set"));
        em.setRootContext(root);
        associateLookup(ExplorerUtils.createLookup(getExplorerManager(), getActionMap()));
    }

    public List<String> getColumns() {
        ArrayList<String> columns = new ArrayList<String>();
        try {
            columns.add("Name");
            columns.add("Set");
            HashMap parameters = new HashMap();
            parameters.put("game", game.getName());
            List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).createdQuery(
                    "select distinct chca.cardAttribute from "
                    + "CardHasCardAttribute chca, Card c, CardSet cs, Game g"
                    + " where cs.game =g and g.name =:game and cs member of c.cardSetList"
                    + " and chca.card =c order by chca.cardAttribute.name", parameters);
            for (Object obj : result) {
                ICardAttribute attr = (ICardAttribute) obj;
                if (!columns.contains(attr.getName())) {
                    columns.add(attr.getName());
                }
            }
        } catch (DBException ex) {
            Exceptions.printStackTrace(ex);
        }
        return columns;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gamePane = new OutlineView();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gamePane, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gamePane, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane gamePane;
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
    public ExplorerManager getExplorerManager() {
        return em;
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
                    result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardAttribute(node.getCard(), getColumnName(i));
                }
                for (ICardAttributeFormatter formatter : game.getGameCardAttributeFormatterImplementations()) {
                    if (result instanceof String) {
                        String string = (String) result;
                        result = formatter.format(string);
                    }
                }
            }
            return result == null ? "" : result.toString();
        }

        @Override
        public Class getColumnClass(int i) {
            return Integer.class;
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
