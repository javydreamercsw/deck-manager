package dreamer.card.game.gui.glazedlist;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import com.reflexit.magiccards.core.model.ICard;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class SetSelect extends AbstractMatcherEditor<ICard> implements ListSelectionListener{

     /** a list of users */
    EventList<String> cardsEventList;
    EventList<String> cardsSelectedList;

    /** a widget for selecting users */
    JList cardsJList;
    
    /**
     * Create a {@link ICardsForUsersMatcherEditor} that matches users from the
     * specified {@link EventList} of {@link ICard}s.
     */
    public SetSelect(EventList<ICard> source) {
        // derive the users list from the ICards list
        EventList<String> setsNonUnique = new CardToSetList(source);
        cardsEventList = new UniqueList<String>(setsNonUnique);

        // create a JList that contains users
        DefaultEventListModel<String> setsListModel = new DefaultEventListModel<String>(cardsEventList);
        cardsJList = new JList(setsListModel);

        // create an EventList containing the JList's selection
        DefaultEventSelectionModel<String> setsSelectionModel = new DefaultEventSelectionModel<String>(cardsEventList);
        cardsJList.setSelectionModel(setsSelectionModel);
        cardsSelectedList = setsSelectionModel.getSelected();
        
        // handle changes to the list's selection
        cardsJList.addListSelectionListener(SetSelect.this);
    }
    
    /**
     * Get the widget for selecting users.
     */
    public JList getJList() {
        return cardsJList;
    }

    /**
     * When the JList selection changes, create a new Matcher and fire
     * an event.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        Matcher<ICard> newMatcher = new CardsForSetsMatcher(cardsSelectedList);
        fireChanged(newMatcher);
    }
}
