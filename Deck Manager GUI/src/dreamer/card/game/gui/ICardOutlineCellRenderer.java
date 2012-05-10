package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
import java.awt.Component;
import javax.swing.*;
import org.netbeans.swing.outline.DefaultOutlineCellRenderer;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class ICardOutlineCellRenderer extends DefaultOutlineCellRenderer {

    private final ICardGame game;

    public ICardOutlineCellRenderer(ICardGame game) {
        this.game = game;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (value != null) {
            return game.getCellRendererImageFactory().getRendering(null,value);
        } else {
            return new JLabel();
        }
    }
}
