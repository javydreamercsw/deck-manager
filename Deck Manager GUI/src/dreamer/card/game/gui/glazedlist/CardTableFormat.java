package dreamer.card.game.gui.glazedlist;

import ca.odell.glazedlists.gui.TableFormat;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardAttributeFormatter;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class CardTableFormat implements TableFormat<ICard>, DataBaseStateListener {

    private final ICardGame game;
    private final ArrayList<String> columns = new ArrayList<String>();
    private static final Logger LOG = Logger.getLogger(CardTableFormat.class.getName());

    public CardTableFormat(ICardGame game) {
        this.game = game;
        columns.add("Name");
        Lookup.getDefault().lookup(IDataBaseCardStorage.class).addDataBaseStateListener(CardTableFormat.this);
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

    public void refresh() {
        for (Iterator<ICard> it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForGame(game).iterator(); it.hasNext();) {
            ICard card = it.next();
            Map<java.lang.String, java.lang.String> attrs =
                    Lookup.getDefault().lookup(IDataBaseCardStorage.class).getAttributesForCard(card);
            for (Iterator<Entry<String, String>> it2 = attrs.entrySet().iterator(); it2.hasNext();) {
                Entry<String, String> attr = it2.next();
                if (!columns.contains(attr.getKey())) {
                    columns.add(attr.getKey());
                }
            }
        }
    }

    @Override
    public void initialized() {
        refresh();
    }
}
