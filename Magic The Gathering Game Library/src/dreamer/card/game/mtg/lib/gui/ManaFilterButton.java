package dreamer.card.game.mtg.lib.gui;

import ca.odell.glazedlists.matchers.TextMatcherEditor;
import com.reflexit.magiccards.core.model.ICard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class ManaFilterButton extends JButton {

    public ManaFilterButton(final String mana, final List<String> manaFilters,
            final TextMatcherEditor<ICard> manaMatcherEditor, Icon icon) {
        super(icon);
        addActionListener(new ActionListener() {
            boolean enabled = false;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (enabled) {
                    //Remove from filter
                    manaFilters.remove(mana);
                    enabled = false;
                } else {
                    //Add to filter
                    manaFilters.add(mana);
                    enabled = true;
                }
                String[] filters = manaFilters.toArray(new String[manaFilters.size()]);
                manaMatcherEditor.setFilterText(filters);
            }
        });
    }
}
