package dreamer.card.game.core;

import dreamer.card.game.core.gui.settings.DBSettings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Build",
id = "dreamer.card.game.core.DatabaseSelection")
@ActionRegistration(displayName = "#CTL_DatabaseSelection")
@ActionReferences({
    @ActionReference(path = "Menu/Options", position = 0, separatorBefore = -50, separatorAfter = 50)
})
@Messages("CTL_DatabaseSelection=Select Database")
public final class DatabaseSelection implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        new DBSettings(new JFrame(), true).setVisible(true);
    }
}
