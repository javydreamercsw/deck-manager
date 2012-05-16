package dreamer.card.game.mtg.lib.gui;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.IGameDataManager;
import dreamer.card.game.core.Tool;
import dreamer.card.game.gui.AbstractGameDataManager;
import dreamer.card.game.mtg.lib.MTGCardCache;
import dreamer.card.game.mtg.lib.MTGRCPGame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = IGameDataManager.class)
public final class MTGGameDataManager extends AbstractGameDataManager {

    private ICardGame game;
    private Panel panel;
    private InstanceContent content = new InstanceContent();
    private Lookup dynamicLookup = new AbstractLookup(content);
    private ProxyLookup proxy = new ProxyLookup(dynamicLookup, Lookup.getDefault());
    private static final Logger LOG = Logger.getLogger(MTGGameDataManager.class.getName());
    private boolean stop;

    public MTGGameDataManager() {
        setGame(new MTGRCPGame());
    }

    @Override
    public void setGame(ICardGame game) {
        this.game = game;
    }

    //TODO:Remove from Interface?
    @Override
    public void load() {
       //Do nothing
    }

    @Override
    public Component getComponent() {
        if (panel == null) {
            final List<String> manaFilters = new ArrayList<String>();
            //Set up the Lookp listener stuff
            //Create Panel for the game
            ArrayList<String> manaTypes = new ArrayList<String>();
            manaTypes.add("W");
            manaTypes.add("U");
            manaTypes.add("B");
            manaTypes.add("R");
            manaTypes.add("G");
            Panel manaFilterPanel = new Panel();
            List<ICardCache> impls = getGame().getCardCacheImplementations();
            if (impls.size() > 0) {
                for (String mana : manaTypes) {
                    try {
                        manaFilterPanel.add(new ManaFilterButton(mana, manaFilters,
                                new ImageIcon((Tool.toBufferedImage(((MTGCardCache) impls.get(0)).getManaIcon(mana))))));
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
            panel = new Panel();
            panel.setLayout(new BorderLayout());
            Panel filterPane = new Panel();
            filterPane.add(manaFilterPanel, BorderLayout.EAST);
            panel.add(filterPane, BorderLayout.NORTH);
        }
        return panel;
    }

    @Override
    public Lookup getLookup() {
        return proxy;
    }

    @Override
    public ICardGame getGame() {
        return game;
    }

    @Override
    public void resultChanged(LookupEvent le) {
        Lookup.Result res = (Lookup.Result) le.getSource();
        Collection instances = res.allInstances();

        if (!instances.isEmpty()) {
            Iterator it = instances.iterator();
            while (it.hasNext()) {
                Object item = it.next();
                if (item instanceof ICard) {
                    ICard card = (ICard) item;
                    LOG.log(Level.FINE, "Adding card: {0}", card.getName());
                    addCard(card);
                }
            }
        }
    }

    private void addCard(ICard card) {
        content.add(card);
    }

    //TODO:Remove from Interface?
    @Override
    public void stop() {
        stop = true;
    }
}
