package dreamer.card.game.core;

import com.reflexit.magiccards.core.model.ICard;
import java.util.List;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public interface ICardDataManager {

    /**
     * @return the currentGame
     */
    String getCurrentGame();

    /**
     * @param currentGame the currentGame to set
     */
    void setCurrentGame(String currentGame);

    /**
     * Get the cards for the current game
     *
     * @return List of cards
     */
    public List<ICard> getCardsForGame();
}
