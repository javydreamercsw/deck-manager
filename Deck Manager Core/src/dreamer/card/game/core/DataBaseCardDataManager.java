package dreamer.card.game.core;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.IGame;
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

    @Override
    public List<ICard> getCardsForSet(ICardSet set) {
        ArrayList<ICard> cards = new ArrayList<ICard>();
        try {
            //Fill lookup with pages for the selected game
            HashMap parameters = new HashMap();
            parameters.put("name", set.getName());
            List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName", parameters);
            if (!result.isEmpty()) {
                CardSet temp = (CardSet) result.get(0);
                for (Card card : temp.getCardList()) {
                    cards.add(card);
                }
            }
        } catch (DBException ex) {
            Exceptions.printStackTrace(ex);
        }
        return cards;
    }

    @Override
    public List<IGame> getGames() {
        ArrayList<IGame> games = new ArrayList<IGame>();
        try {
            //Fill lookup with games
            List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Game.findAll");
            if (!result.isEmpty()) {
                Game game = (Game) result.get(0);
                games.add(game);
            }
        } catch (DBException ex) {
            Exceptions.printStackTrace(ex);
        }
        return games;
    }

    @Override
    public List<ICardSet> getSetsForGame(IGame game) {
        ArrayList<ICardSet> sets = new ArrayList<ICardSet>();
        try {
            //Fill lookup with games
            List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class).namedQuery("Game.findAll");
            if (!result.isEmpty()) {
                Game temp = (Game) result.get(0);
                sets.addAll(temp.getCardSetList());
            }
        } catch (DBException ex) {
            Exceptions.printStackTrace(ex);
        }
        return sets;
    }
}
