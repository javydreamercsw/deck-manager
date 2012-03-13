package dreamer.card.game.core;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.IGame;
import java.util.List;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public interface ICardDataManager {

    /**
     * Get the cards for the current game
     *
     * @param set 
     * @return List of cards
     */
    public List<ICard> getCardsForSet(ICardSet set);
    
    /**
     * Get all games
     * @return List of games
     */
    public List<IGame> getGames();

    /**
     * Get sets for game
     * @param game game to get sets for
     * @return list of sets
     */
    public List<ICardSet> getSetsForGame(ICardGame game);

    /**
     * Get cards for game
     * @param game game to get cards for
     * @return list of cards
     */
    public List<ICard> getCardsForGame(ICardGame game);
}
