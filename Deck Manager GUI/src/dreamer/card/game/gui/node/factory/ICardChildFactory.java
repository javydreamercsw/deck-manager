package dreamer.card.game.gui.node.factory;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import dreamer.card.game.core.Tool;
import dreamer.card.game.gui.node.ICardNode;
import dreamer.card.game.gui.node.actions.Reloadable;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.Loader;
import javassist.Modifier;
import javassist.NotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardChildFactory extends ChildFactory<ICard> implements Lookup.Provider {

    private final ICardSet set;
    private static final Logger LOG = Logger.getLogger(ICardChildFactory.class.getName());
    private List<ICard> cards = new ArrayList<ICard>();
    private ICardGame game;
    private ClassPool pool = ClassPool.getDefault();
    private Loader cl;
    /**
     * The lookup for Lookup.Provider
     */
    private Lookup lookup;
    /**
     * The InstanceContent that keeps this entity's abilities
     */
    private InstanceContent instanceContent;

    ICardChildFactory(final ICardSet set) {
        this.set = set;
        for (Iterator<? extends ICardGame> it = Lookup.getDefault().lookupAll(ICardGame.class).iterator(); it.hasNext();) {
            ICardGame g = it.next();
            if (g.getName().equals(set.getGameName())) {
                game = g;
                break;
            }
        }
        AccessController.doPrivileged(new PrivilegedAction() {
            @Override
            public Object run() {
                cl = new Loader(pool);
                return null; // nothing to return
            }
        });
        // Create an InstanceContent to hold abilities...
        instanceContent = new InstanceContent();
        // Create an AbstractLookup to expose InstanceContent contents...
        lookup = new AbstractLookup(instanceContent);
        // Add a "Reloadable" ability to this entity
        instanceContent.add(new Reloadable() {
            @Override
            public void reload() throws Exception {
                long start = System.currentTimeMillis();
                for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForSet(set).iterator(); it.hasNext();) {
                    ICard card = (ICard) it.next();
                    if (!cards.contains(card)) {
                        cards.add(card);
                    }
                }
                LOG.log(Level.INFO, "DB query for set: {1} took: {0} hits: {2}",
                        new Object[]{Tool.elapsedTime(start), set.getName(), cards.size()});
            }
        });
    }

    @Override
    protected boolean createKeys(List<ICard> toPopulate) {
        // The query node is reloadable, isn't it? Then just
        // get this ability from the lookup ...
        Reloadable r = getLookup().lookup(Reloadable.class);
        // ... and  use the ability
        int size = cards.size();
        if (r != null) {
            try {
                r.reload();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        toPopulate.addAll(cards);
        return size == cards.size();
    }

    @Override
    protected Node createNodeForKey(ICard card) {
        ICardNode node = null;
        try {
            node = new ICardNode(card, createBean(card), set.getGameName());
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return node == null ? new AbstractNode(Children.LEAF) : node;
    }

    private Object createBean(ICard card) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        String className = ICard.class.getName()
                + game.getName().replaceAll(" ", "") + "Bean";
        if (pool.getOrNull(className) == null) {
            CtClass cc = pool.makeClass(className);
            for (Iterator<String> it = game.getColumns().iterator(); it.hasNext();) {
                String c = it.next();
                try {
                    addGetterAndSetter(cc, getColumnName(c));
                } catch (CannotCompileException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (NotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        Object bean = null;
        try {
            Class c = cl.loadClass(className);
            bean = c.newInstance();
            for (Iterator<String> it = game.getColumns().iterator(); it.hasNext();) {
                String col = it.next();
                bean.getClass().getMethod("set" + getColumnName(col),
                        String.class).invoke(bean,
                        Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardAttribute(card,
                        getColumnName(col)));
            }
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return bean;
    }

    private void addGetterAndSetter(CtClass cc, String name) throws CannotCompileException, NotFoundException {
        CtField field = new CtField(pool.get(String.class.getName()), name, cc);
        field.setModifiers(Modifier.PUBLIC);
        cc.addField(field);
        cc.addMethod(CtNewMethod.getter("get" + name, field));
        cc.addMethod(CtNewMethod.setter("set" + name, field));
    }

    private String getColumnName(String name) {
        if (name.equals("CardId")) {
            return "Card Id";
        } else {
            return name;
        }
    }

    @Override
    protected Node[] createNodesForKey(ICard key) {
        long start = System.currentTimeMillis();
        Node[] nodes = new Node[]{createNodeForKey(key)};
        LOG.log(Level.FINE, "Creating nodes for card: {1} took {0}",
                new Object[]{Tool.elapsedTime(start), key.getName()});
        return nodes;
    }

    public void refresh() {
        refresh(true);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }
}
