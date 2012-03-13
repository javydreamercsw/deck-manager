package dreamer.card.game;

import com.reflexit.magiccards.core.model.DefaultCardGame;
import java.awt.Image;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class MTGGame extends DefaultCardGame{

    @Override
    public String getName() {
        return "Magic the Gathering";
    }

    @Override
    public Image getBackCardIcon() {
        return null;
    }

    @Override
    public Image getGameIcon() {
        return null;
    }
}
