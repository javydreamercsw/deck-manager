package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import dreamer.card.game.core.Tool;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
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
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardChildFactory extends ChildFactory<ICard> {

    private final ICardSet set;
    private static final Logger LOG = Logger.getLogger(ICardChildFactory.class.getName());
    private HashMap<ICard, ICardNode> cards = new HashMap<ICard, ICardNode>();
    private ICardGame game;
    private ClassPool pool = ClassPool.getDefault();
    private Loader cl;

    ICardChildFactory(ICardSet set) {
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
    }

    @Override
    protected boolean createKeys(List<ICard> toPopulate) {
        if (cards.isEmpty()) {
            long start = System.currentTimeMillis();
            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForSet(set).iterator(); it.hasNext();) {
                ICard card = (ICard) it.next();
                if (!cards.containsKey(card)) {
                    cards.put(card, null);
                }
            }
            LOG.log(Level.INFO, "DB query for set: {1} took: {0} hits: {2}",
                    new Object[]{Tool.elapsedTime(start), set.getName(), cards.size()});
        }
        toPopulate.addAll(cards.keySet());
        return true;
    }

    @Override
    protected Node createNodeForKey(ICard card) {
        if (cards.containsKey(card) && cards.get(card) == null) {
            try {
                ICardNode node = new ICardNode(card, createBean(card), set.getGameName());
                cards.put(card, node);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IntrospectionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return cards.get(card);
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
        LOG.log(Level.INFO, "Creating nodes for card: {1} took {0}",
                new Object[]{Tool.elapsedTime(start), key.getName()});
        return nodes;
    }

    public void refresh() {
        refresh(true);
    }
}
