/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import org.dreamer.event.bus.EventBus;
import org.dreamer.event.bus.EventBusListener;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public abstract class GameCardComponent extends TopComponent
        implements ExplorerManager.Provider,
        DataBaseStateListener, EventBusListener<ICardGame> {

    public GameCardComponent(Lookup lookup) {
        super(lookup);
        register();
    }

    public GameCardComponent() {
        register();
    }

    @Override
    public boolean canClose() {
        EventBus.getDefault().unsubscribe(ICardGame.class, this);
        return super.canClose();
    }

    private void register() {
        //Register to listen for Card games
        EventBus.getDefault().subscribe(ICardGame.class, this);
    }
}
