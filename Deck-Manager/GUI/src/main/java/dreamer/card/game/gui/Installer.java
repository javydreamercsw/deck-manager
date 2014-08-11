package dreamer.card.game.gui;

import dreamer.card.game.gui.component.game.GameTopComponent;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import org.openide.modules.ModuleInstall;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

@ServiceProvider(service = DataBaseStateListener.class)
public class Installer extends ModuleInstall implements WindowSystemListener, DataBaseStateListener {

    @Override
    public void restored() {
        WindowManager.getDefault().addWindowSystemListener(this);
    }

    @Override
    public void beforeLoad(WindowSystemEvent event) {
        //Start in game view
        WindowManager.getDefault().setRole("game_view");
        WindowManager.getDefault().removeWindowSystemListener(this);
    }

    @Override
    public void afterLoad(WindowSystemEvent event) {
    }

    @Override
    public void beforeSave(WindowSystemEvent event) {
    }

    @Override
    public void afterSave(WindowSystemEvent event) {
    }

    @Override
    public void initialized() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                TopComponent gameTC = 
                        WindowManager.getDefault()
                                .findTopComponent("GameTopComponent");
                if (gameTC == null) {
                    gameTC = new GameTopComponent();
                }
                gameTC.open();
                gameTC.requestActive();
            }
        });
    }
}
