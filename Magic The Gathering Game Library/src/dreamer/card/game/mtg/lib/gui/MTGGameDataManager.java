package dreamer.card.game.mtg.lib.gui;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.IGameDataManager;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import dreamer.card.game.gui.glazedlist.CardTableFormat;
import dreamer.card.game.mtg.lib.MTGRCPGame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Panel;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import org.dreamer.event.bus.EventBus;
import org.jdesktop.swingx.JXTable;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = IGameDataManager.class)
public final class MTGGameDataManager implements IGameDataManager, LookupListener {
    
    private ICardGame game;
    private EventList<ICard> eventList = new BasicEventList<ICard>();
    private DefaultEventTableModel<ICard> tableModel;
    private SortedList<ICard> sortedCards = new SortedList<ICard>(eventList);
    private Lookup.Result<ICard> cardResult;
    private Lookup.Result<ICardSet> cardSetResult;
    private boolean loaded = false;
    private JXTable cards;
    private JTextField filterEdit = new JTextField(10);
    private Panel panel;
    private FilterList<ICard> textFilteredIssues;
    private InstanceContent content = new InstanceContent();
    private Lookup dynamicLookup = new AbstractLookup(content);
    
    public MTGGameDataManager() {
        setGame(new MTGRCPGame());
    }
    
    private TableCellRenderer getRendererForAttribute(String name) {
        if (name.toLowerCase().equals("cost")) {
            return new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value != null) {
                        return (JLabel) value;
                    } else {
                        return new JLabel();
                    }
                }

                // The following methods override the defaults for performance reasons
                public void validate() {
                }
                
                public void revalidate() {
                }
                
                protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
                }
                
                public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
                }
            };
        } else {
            return null;
        }
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
        DefaultEventSelectionModel selectionModel = new DefaultEventSelectionModel(eventList);
        cards = new JXTable(getTableModel());
        cards.setSelectionModel(selectionModel);
        cards.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                EventBus.getDefault().add(getTableModel().getElementAt(cards.getSelectedRow()));
            }
        });
        //Add custom renderers
        for (int i = 0; i < getTableModel().getColumnCount(); i++) {
            TableCellRenderer renderer = getRendererForAttribute(getTableModel().getColumnName(i));
            if (renderer != null) {
                cards.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
        }
        //Enable the controls for the table
        cards.setColumnControlVisible(true);
        TableComparatorChooser.install(
                cards, sortedCards, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
        //Set up the Lookp listener stuff
        cardResult = EventBus.getDefault().getCentralLookup().lookupResult(ICard.class);
        cardResult.allInstances();
        cardResult.addLookupListener(MTGGameDataManager.this);
        cardSetResult = EventBus.getDefault().getCentralLookup().lookupResult(ICardSet.class);
        cardSetResult.allInstances();
        cardSetResult.addLookupListener(MTGGameDataManager.this);
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
    }
    
    @Override
    public void load() {
        if (!loaded) {
            SwingUtilities.invokeLater(new DataLoader());
        }
    }

    /**
     * @return the tableModel
     */
    public DefaultEventTableModel<ICard> getTableModel() {
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
                Object next = c.iterator().next();
                if (next instanceof ICard) {
                    ICard card = (ICard) next;
                    if (!eventList.contains(card)) {
                        addCard(card);
                    }
                } else if (next instanceof ICardSet) {
                    ICardSet set = (ICardSet) next;
                }
            }
        }
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
