package dreamer.card.game.gui;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class RootNode extends AbstractNode {

    private final IGameChildFactory childFactory;

    /**
     * Creates a new instance of RootNode
     *
     * @param childFactory
     */
    public RootNode(IGameChildFactory childFactory) {
        super(Children.create(childFactory, true));
        this.childFactory = childFactory;
    }

    //TODO: Add icons
//    @Override
//    public Image getIcon(int type) {
//        Image icon = null;
//        try {
//            icon = Tool.createImage("simple.marauroa.application.gui",
//                    "resources/images/right.png", "Root closed icon");
//        } catch (MalformedURLException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        return icon;
//    }
//    @Override
//    public Image getOpenedIcon(int type) {
//        Image icon = null;
//        try {
//            icon = Tool.createImage("simple.marauroa.application.gui",
//                    "resources/images/down.png", "Root opened icon");
//        } catch (MalformedURLException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        return icon;
//    }
    @Override
    public boolean canDestroy() {
        return false;
    }

//    @Override
//    public Action[] getActions(boolean popup) {
//        return new Action[]{new RootNode.RootNodeAction()};
//    }
//
//    private class RootNodeAction extends AbstractAction {
//
//        public RootNodeAction() {
//            putValue(NAME, "Add Marauroa Application");
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent ae) {
//            if (dialog == null) {
//                dialog = Lookup.getDefault().lookup(
//                        IAddApplicationDialogProvider.class).getDialog();
//            }
//            dialog.setLocationRelativeTo(null);
//            dialog.setVisible(true);
//        }
//    }

    public void refresh() {
        this.childFactory.refresh();
    }
}
