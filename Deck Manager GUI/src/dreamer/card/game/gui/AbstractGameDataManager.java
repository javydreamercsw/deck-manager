package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.IGameDataManager;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public abstract class AbstractGameDataManager implements IGameDataManager {

    private Lookup.Result<ICard> result = Utilities.actionsGlobalContext().lookupResult(ICard.class);

    public AbstractGameDataManager() {
        result.allItems();
        result.addLookupListener(AbstractGameDataManager.this);
    }
}
