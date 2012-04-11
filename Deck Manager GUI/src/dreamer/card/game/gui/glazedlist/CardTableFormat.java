package dreamer.card.game.gui.glazedlist;

import ca.odell.glazedlists.gui.TableFormat;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardAttribute;
import com.reflexit.magiccards.core.model.ICardAttributeFormatter;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class CardTableFormat implements TableFormat<ICard> {

    private final ICardGame game;
    private final ArrayList<String> columns = new ArrayList<String>();
    private static final Logger LOG = Logger.getLogger(CardTableFormat.class.getName());

    public CardTableFormat(ICardGame game) {
        this.game = game;
        try {
            columns.add("Name");
            HashMap parameters = new HashMap();
            parameters.put("game", game.getName());
            List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).createdQuery(
                    "select distinct chca.cardAttribute from "
                    + "CardHasCardAttribute chca, Card c, CardSet cs, Game g"
                    + " where cs.game =g and g.name =:game and cs member of c.cardSetList"
                    + " and chca.card =c order by chca.cardAttribute.name", parameters);
            for(Object obj:result){
                ICardAttribute attr=(ICardAttribute) obj;
                if(!columns.contains(attr.getName())){
                    columns.add(attr.getName());
                }
            }
        } catch (DBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        String name = columns.get(column);
        return name.equals("CardId") ? "Card Id" : name;
    }

    @Override
    public Object getColumnValue(ICard card, int column) {
        String columnName = getColumnName(column).toLowerCase().replaceAll("_", "");
        Object result;
        if (columnName.equals("name")) {
            result = card.getName();
        } else if (columnName.equals("cardid")) {
            result = card.getCardId();
        } else if (columnName.equals("set")) {
            result = card.getSetName();
        } else {
            result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardAttribute(card, getColumnName(column));
        }
        for (ICardAttributeFormatter formatter : game.getGameCardAttributeFormatterImplementations()) {
            if (result instanceof String) {
                String string = (String) result;
                LOG.log(Level.FINER, "Formatting string: {0}", result);
                result = formatter.format(string);
                LOG.log(Level.FINER, "Done!");
            }
        }
        return result;
    }
}
