/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamer.card.game.gui;

import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public abstract class GameCardComponent extends TopComponent
        implements ExplorerManager.Provider,
        DataBaseStateListener, LookupListener {

    private Lookup.Result<ICardGame> result = Utilities.actionsGlobalContext().lookupResult(ICardGame.class);

    public GameCardComponent() {
        result.allItems();
        result.addLookupListener(GameCardComponent.this);
    }
}
