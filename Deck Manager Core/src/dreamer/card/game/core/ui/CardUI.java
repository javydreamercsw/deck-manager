package dreamer.card.game.core.ui;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class CardUI implements ICardUI {

    private final ICard card;
    private String set;

    public CardUI(ICard card) {
        this.card = card;
    }

    @Override
    public void setSet(String set) {
        this.set = set;
    }

    @Override
    public String getSet() {
        return set;
    }

    @Override
    public String getName() {
        return card.getName();
    }

    @Override
    public Object getObjectByField(ICardField field) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getCardId() {
        return card.getCardId();
    }

    @Override
    public int compareTo(Object o) {
        return card.compareTo(o);
    }
}
