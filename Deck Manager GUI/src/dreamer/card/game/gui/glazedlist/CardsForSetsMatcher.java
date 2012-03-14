package dreamer.card.game.gui.glazedlist;

import ca.odell.glazedlists.matchers.Matcher;
import com.reflexit.magiccards.core.model.ICard;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class CardsForSetsMatcher implements Matcher<ICard> {

    /**
     * the cards to match
     */
    private Set<String> cards = new HashSet<String>();

    /**
     * Create a new {@link IssuesForUsersMatcher} that matches only {@link Issue}s
     * that have one or more user in the specified list.
     */
    public CardsForSetsMatcher(Collection<String> cards) {
        // defensive copy all the users
        this.cards.addAll(cards);
    }

    /**
     * Test whether to include or not include the specified card based on
     * whether or not their user is selected.
     */
    @Override
    public boolean matches(ICard card) {
        if (card == null) {
            return false;
        }
        if (cards.isEmpty()) {
            return true;
        }

        String user = card.getSetName();
        return cards.contains(user);
    }
}
