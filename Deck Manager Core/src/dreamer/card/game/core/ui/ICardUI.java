package dreamer.card.game.core.ui;

import com.reflexit.magiccards.core.model.ICard;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public interface ICardUI extends ICard {

    /**
     * Set the card set.
     *
     * @param set new set
     */
    public void setSet(String set);

    /**
     * Get the card set.
     */
    public String getSet();
}
