package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.beans.IntrospectionException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardChildFactory extends ChildFactory<ICard> {

    private final ICardSet set;
    private static final Logger LOG = Logger.getLogger(ICardChildFactory.class.getName());

    ICardChildFactory(ICardSet set) {
        this.set = set;
    }

    @Override
    protected boolean createKeys(List<ICard> toPopulate) {
        toPopulate.addAll(Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForSet(set));
        return true;
    }

//    @Override
//    protected boolean createKeys(List<DynaBean> toPopulate) {
//        for (Iterator<? extends ICardGame> it = Lookup.getDefault().lookupAll(ICardGame.class).iterator(); it.hasNext();) {
//            ICardGame temp = it.next();
//            if (temp.getName().equals(set.getGameName())) {
//                game = temp;
//                break;
//            }
//        }
//        if (game != null) {
//            for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForSet(set).iterator(); it.hasNext();) {
//                ICard card = (ICard) it.next();
//                toPopulate.add(getBean(card, game));
//            }
//        }
//        return true;
//    }
//    private static DynaBean getBean(ICard card, ICardGame game) {
//        List<String> columns = getColumns(game);
//        ArrayList<DynaProperty> props = new ArrayList<DynaProperty>();
//        for (Iterator<String> it = columns.iterator(); it.hasNext();) {
//            String prop = it.next();
//            props.add(new DynaProperty(prop, String.class));
//        }
//        BasicDynaClass clazz = new BasicDynaClass("card", null, props.toArray(new DynaProperty[columns.size()]));
//        DynaBean bean = null;
//        try {
//            bean = clazz.newInstance();
//            for (Iterator<String> it = columns.iterator(); it.hasNext();) {
//                String columnName = it.next();
//                Object result;
//                if (columnName.toLowerCase(Locale.getDefault()).equals("name")) {
//                    result = card.getName();
//                } else if (columnName.toLowerCase(Locale.getDefault()).equals("cardid")) {
//                    result = card.getCardId();
//                } else if (columnName.toLowerCase(Locale.getDefault()).equals("set")) {
//                    result = card.getSetName();
//                } else {
//                    result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardAttribute(card, columnName);
//                }
//                for (ICardAttributeFormatter formatter : game.getGameCardAttributeFormatterImplementations()) {
//                    if (result instanceof String) {
//                        String string = (String) result;
//                        LOG.log(Level.FINER, "Formatting string: {0}", result);
//                        result = formatter.format(string);
//                        LOG.log(Level.FINER, "Done!");
//                    }
//                }
//                LOG.log(Level.INFO, "Adding: {0}, {1}", new Object[]{columnName, result});
//                bean.set(columnName, result == null ? "" : result.toString());
//            }
//        } catch (IllegalAccessException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (InstantiationException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        return bean;
//    }
//
//    private static List<String> getColumns(ICardGame game) {
//        ArrayList<String> columns = new ArrayList<String>();
//        try {
//            columns.add("Name");
//            columns.add("Set");
//            HashMap parameters = new HashMap();
//            parameters.put("game", game.getName());
//            List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).createdQuery(
//                    "select distinct chca.cardAttribute from "
//                    + "CardHasCardAttribute chca, Card c, CardSet cs, Game g"
//                    + " where cs.game =g and g.name =:game and cs member of c.cardSetList"
//                    + " and chca.card =c order by chca.cardAttribute.name", parameters);
//            for (Object obj : result) {
//                ICardAttribute attr = (ICardAttribute) obj;
//                if (!columns.contains(attr.getName())) {
//                    columns.add(attr.getName());
//                }
//            }
//        } catch (DBException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        return columns;
//    }
    
    @Override
    protected Node createNodeForKey(ICard card) {
        try {
            return new ICardNode(card, set.getGameName());
        } catch (IntrospectionException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    protected Node[] createNodesForKey(ICard key) {
        return new Node[]{createNodeForKey(key)};
    }

//    @Override
//    protected Node createNodeForKey(DynaBean card) {
//        try {
//            return new ICardDynaNode(card, game);
//        } catch (IntrospectionException ex) {
//            LOG.log(Level.SEVERE, null, ex);
//            return null;
//        }
//    }
//
//    @Override
//    protected Node[] createNodesForKey(DynaBean key) {
//        return new Node[]{createNodeForKey(key)};
//    }
    public void refresh() {
        refresh(true);
    }
}
