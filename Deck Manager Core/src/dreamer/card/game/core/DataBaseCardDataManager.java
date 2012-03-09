package dreamer.card.game.core;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.Card;
import com.reflexit.magiccards.core.storage.database.CardSet;
import com.reflexit.magiccards.core.storage.database.Game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = ICardDataManager.class)
public class DataBaseCardDataManager implements ICardDataManager {

    private String currentGame;

    @Override
    public List<ICard> getCardsForGame() {
        ArrayList<ICard> cards = new ArrayList<ICard>();
        try {
            //Fill lookup with pages for the selected game
            HashMap parameters = new HashMap();
            parameters.put("name", currentGame);
            List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Game.findByName", parameters);
            if (!result.isEmpty()) {
                Game game = (Game) result.get(0);
                for (CardSet set : game.getCardSetList()) {
                    for (Card card : set.getCardList()) {
                        cards.add(card);
                    }
                }
            }
        } catch (DBException ex) {
            Exceptions.printStackTrace(ex);
        }
        return cards;
    }

    @Override
    public String getCurrentGame() {
        return currentGame;
    }

    @Override
    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }
}
