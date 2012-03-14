package dreamer.card.game.gui.glazedlist;

import ca.odell.glazedlists.gui.TableFormat;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.openide.util.Lookup;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class CardTableFormat implements TableFormat<ICard> {

    private final ICardGame game;
    private final ArrayList<String> columns = new ArrayList<String>();

    public CardTableFormat(ICardGame game) {
        this.game = game;
        columns.add("Name");
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
        if (columnName.equals("name")) {
            return card.getName();
        } else if (columnName.equals("cardid")) {
            return card.getCardId();
        } else if (columnName.equals("set")) {
            return card.getSetName();
        } else {
            return Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardAttribute(card, columnName);
        }
    }
}
