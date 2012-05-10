package dreamer.card.game;

import com.reflexit.magiccards.core.model.DefaultCardGame;
import com.reflexit.magiccards.core.model.IGameCellRendererImageFactory;
import java.awt.Image;
import javax.swing.JLabel;

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

    @Override
    public IGameCellRendererImageFactory getCellRendererImageFactory() {
        return new IGameCellRendererImageFactory() {

            @Override
            public JLabel getRendering(String string, Object o) {
                //Return the same object in a JLabel by default
                return new JLabel(o.toString());
            }
        };
    }
}
