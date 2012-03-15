package dreamer.card.game.gui.glazedlist;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.IGameDataManager;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.JXTable;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = IGameDataManager.class)
public class GameDataManager implements IGameDataManager {

    private ICardGame game;
    private EventList<ICard> eventList = new BasicEventList<ICard>();
    private DefaultEventTableModel<ICard> tableModel;
    private SortedList<ICard> sortedCards = new SortedList<ICard>(eventList);
    private Result<ICard> result;
    private boolean loaded = false;
    private JXTable cards;
    private JTextField filterEdit = new JTextField(10);
    private Panel panel;
    private FilterList<ICard> textFilteredIssues;
    private InstanceContent content = new InstanceContent();
    private Lookup dynamicLookup = new AbstractLookup(content);

    public GameDataManager() {
    }

    @Override
    public void setGame(ICardGame game) {
        this.game = game;
        SetSelect setSelect = new SetSelect(eventList);
        FilterList<ICard> setsFilteredIssues = new FilterList<ICard>(sortedCards, setSelect);
        MatcherEditor<ICard> textMatcherEditor =
                new TextComponentMatcherEditor<ICard>(filterEdit,
                new TextFilterator<ICard>() {

                    @Override
                    public void getFilterStrings(List<String> list, ICard e) {
                        for (int i = 0; i < getTableModel().getTableFormat().getColumnCount(); i++) {
                            Object columnValue = getTableModel().getTableFormat().getColumnValue(e, i);
                            if (columnValue != null) {
                                list.add(columnValue.toString());
                            }
                        }
                    }
                });
        textFilteredIssues = new FilterList<ICard>(setsFilteredIssues, textMatcherEditor);
        //Create the card list
        tableModel = new DefaultEventTableModel<ICard>(textFilteredIssues,
                new CardTableFormat(game));
        cards = new JXTable(getTableModel());
        //Enable the controls for the table
        cards.setColumnControlVisible(true);
        TableComparatorChooser.install(
                cards, sortedCards, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
        //Set up the Lookp listener stuff
        result = Lookup.getDefault().lookupResult(ICard.class);
        result.allInstances();
        result.addLookupListener(GameDataManager.this);
        //Create Panel for the game
        panel = new Panel();
        panel.setName(game.getName());
        panel.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane(cards);
        Panel filterPane = new Panel();
        filterPane.add(new JLabel("Filter: "), BorderLayout.WEST);
        filterPane.add(filterEdit, BorderLayout.CENTER);
        Panel westPane = new Panel();
        westPane.add(filterPane, BorderLayout.NORTH);
        panel.add(westPane, BorderLayout.WEST);
        panel.add(sp, BorderLayout.CENTER);
        //Populate the table
        SwingUtilities.invokeLater(new DataLoader());
    }

    /**
     * @return the tableModel
     */
    public final DefaultEventTableModel<ICard> getTableModel() {
        if (tableModel == null) {
            tableModel = new DefaultEventTableModel<ICard>(textFilteredIssues,
                    new CardTableFormat(game));
        }
        return tableModel;
    }

    @Override
    public void resultChanged(LookupEvent lookupEvent) {
        if (loaded) {
            Lookup.Result r = (Lookup.Result) lookupEvent.getSource();
            Collection c = r.allInstances();
            for (int i = 0; i < c.size(); i++) {
                ICard card = (ICard) c.iterator().next();
                System.err.println("Saw " + card.getName());
                if (!eventList.contains(card)) {
                    addCard(card);
                }
            }
        }
    }

    /**
     * @return the sortedCards
     */
    @Override
    public SortedList<ICard> getSortedCards() {
        return sortedCards;
    }

    @Override
    public Component getComponent() {
        return panel;
    }

    @Override
    public Lookup getLookup() {
        return dynamicLookup;
    }

    @Override
    public ICardGame getGame() {
        return game;
    }

    private class DataLoader implements Runnable {

        @Override
        public void run() {
            eventList.getReadWriteLock().readLock().lock();
            try {
                for (Iterator it = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getCardsForGame(game).iterator(); it.hasNext();) {
                    addCard((ICard) it.next());
                }
                loaded = true;
            } finally {
                eventList.getReadWriteLock().readLock().unlock();
            }
        }
    }

    private void addCard(ICard card) {
        content.add(card);
        eventList.add(card);
    }
}
