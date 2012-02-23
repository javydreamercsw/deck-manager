package dreamer.card.game;

import dreamer.card.game.DefaultCardGame;
import dreamer.card.game.ICardGame;
import java.util.ArrayList;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = ICardGame.class)
public class MTGGame extends DefaultCardGame {

    private static ArrayList<String> rarities = new ArrayList<String>();
    private static ArrayList<String> creatureAttribs = new ArrayList<String>();

    static {
        rarities.add("rarity.common");
        rarities.add("rarity.uncommon");
        rarities.add("rarity.rare");
        rarities.add("rarity.mythic.rare");
        rarities.add("rarity.land");
        
        creatureAttribs.add("power");
        creatureAttribs.add("toughness");
        attribs.put("rarity", rarities);
        attribs.put("creature", creatureAttribs);
    }

    @Override
    public String getName() {
        return "Magic the Gathering";
    }

    @Override
    public void init() {
        super.init();
        //Add the magic specific stuff
    }
}
