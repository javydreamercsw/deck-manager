package dreamer.card.game.gui;

import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

public class Installer extends ModuleInstall implements WindowSystemListener {

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
}
