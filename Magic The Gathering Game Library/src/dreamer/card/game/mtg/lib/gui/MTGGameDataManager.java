package dreamer.card.game.mtg.lib.gui;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.*;
import com.dreamer.outputhandler.OutputHandler;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.IGameDataManager;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import dreamer.card.game.core.Tool;
import dreamer.card.game.gui.glazedlist.CardTableFormat;
import dreamer.card.game.mtg.lib.MTGCardCache;
import dreamer.card.game.mtg.lib.MTGRCPGame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Panel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import mtg.card.MagicCardField;
import org.dreamer.event.bus.EventBus;
import org.dreamer.event.bus.EventBusListener;
import org.jdesktop.swingx.JXTable;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = IGameDataManager.class)
public final class MTGGameDataManager implements IGameDataManager, EventBusListener<ICard> {

    private ICardGame game;
    private EventList<ICard> eventList = GlazedListsSwing.swingThreadProxyList(GlazedLists.threadSafeList(new BasicEventList<ICard>()));
    private DefaultEventTableModel<ICard> tableModel;
    private SortedList<ICard> sortedCards = new SortedList<ICard>(eventList);
    private boolean loaded = false, loading = false;
    private JXTable cards;
    private JTextField filterEdit = new JTextField(10);
    private Panel panel;
    private FilterList<ICard> textFilteredList, manaFilteredList;
    private InstanceContent content = new InstanceContent();
    private Lookup dynamicLookup = new AbstractLookup(content);
    private static final Logger LOG = Logger.getLogger(MTGGameDataManager.class.getName());
    private CardTableFormat tableFormat;
    private boolean stop;

    public MTGGameDataManager() {
        setGame(new MTGRCPGame());
    }

    private TableCellRenderer getRendererForAttribute(String name) {
        if (name.toLowerCase().equals("cost")) {
            return new TableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value != null) {
                        List<ICardCache> impls = getGame().getCardCacheImplementations();
                        if (impls.size() > 0 && ((String) value).contains("{") && ((String) value).contains("}")) {
                            JLabel container = new JLabel();
                            container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
                            ArrayList<String> values = new ArrayList<String>();
                            StringTokenizer st = new StringTokenizer((String) value, "}");
                            while (st.hasMoreTokens()) {
                                String token = st.nextToken();
                                values.add(token.substring(1));
                            }
                            for (Iterator<String> it = values.iterator(); it.hasNext();) {
                                String v = it.next();
                                try {
                                    JLabel iconLabel = new JLabel(new ImageIcon((Tool.toBufferedImage(((MTGCardCache) impls.get(0)).getManaIcon(v)))));
                                    container.add(iconLabel);
                                    if (it.hasNext()) {
                                        container.add(Box.createRigidArea(new Dimension(5, 0)));
                                    }
                                } catch (IOException ex) {
                                    LOG.log(Level.SEVERE, null, ex);
                                }
                            }
                            return container;
                        }
                        return new JLabel(((String) value));
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
    }

    @Override
    public void load() {
        if (!loaded) {
            new Thread(new DataLoader(), game.getName() + " loader").start();
        }
    }

    /**
     * @return the tableModel
     */
    public DefaultEventTableModel<ICard> getTableModel() {
        if (tableModel == null) {
            tableFormat = new CardTableFormat(game);
            tableModel = new DefaultEventTableModel<ICard>(manaFilteredList,
                    tableFormat);
        }
        return tableModel;
    }

    @Override
    public Component getComponent() {
        if (panel == null) {
            SetSelect setSelect = new SetSelect(eventList);
            FilterList<ICard> setsFilteredList = new FilterList<ICard>(sortedCards, setSelect);
            MatcherEditor<ICard> textMatcherEditor =
                    new TextComponentMatcherEditor<ICard>(filterEdit,
                    new TextFilterator<ICard>() {

                        @Override
                        public void getFilterStrings(List<String> list, ICard e) {
                            list.add(e.getObjectByField(MagicCardField.NAME).toString());
                            list.add(e.getObjectByField(MagicCardField.TEXT).toString());
                            Object value = e.getObjectByField(MagicCardField.NAME);
                            if (value != null) {
                                list.add(value.toString());
                            }
                            value = e.getObjectByField(MagicCardField.TEXT);
                            if (value != null) {
                                list.add(value.toString());
                            }
                        }
                    });
            textFilteredList = new FilterList<ICard>(setsFilteredList, textMatcherEditor);
            final ArrayList<String> manaFilters = new ArrayList<String>();
            TextMatcherEditor<ICard> manaMatcherEditor = new TextMatcherEditor<ICard>(new TextFilterator<ICard>() {

                @Override
                public void getFilterStrings(List<String> list, ICard e) {
                    Object value = e.getObjectByField(MagicCardField.COST);
                    if (value != null) {
                        list.add(value.toString());
                    }
                }
            });
            manaMatcherEditor.setMode(TextMatcherEditor.CONTAINS);
            manaFilteredList = new FilterList<ICard>(textFilteredList, textMatcherEditor);
            //Create the card list
            DefaultEventSelectionModel selectionModel = new DefaultEventSelectionModel(eventList);
            cards = new JXTable(getTableModel());
            cards.setSelectionModel(selectionModel);
            cards.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    EventBus.getDefault().publish(getTableModel().getElementAt(cards.getSelectedRow()));
                }
            });
            TableComparatorChooser.install(
                    cards, sortedCards, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
            //Add custom renderers
            for (int i = 0; i < getTableModel().getColumnCount(); i++) {
                TableCellRenderer renderer = getRendererForAttribute(getTableModel().getColumnName(i));
                if (renderer != null) {
                    cards.getColumnModel().getColumn(i).setCellRenderer(renderer);
                }
            }
            //Enable the controls for the table
            cards.setColumnControlVisible(true);
            //Set up the Lookp listener stuff
            EventBus.getDefault().subscribe(ICard.class, this);
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
                                manaMatcherEditor,
                                new ImageIcon((Tool.toBufferedImage(((MTGCardCache) impls.get(0)).getManaIcon(mana))))));
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
            panel = new Panel();
            panel.setLayout(new BorderLayout());
            JScrollPane sp = new JScrollPane(cards);
            Panel filterPane = new Panel();
            filterPane.add(new JLabel("Filter: "), BorderLayout.WEST);
            filterPane.add(filterEdit, BorderLayout.CENTER);
            filterPane.add(manaFilterPanel, BorderLayout.EAST);
            panel.add(filterPane, BorderLayout.NORTH);
            panel.add(sp, BorderLayout.CENTER);
        }
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

    @Override
    public void notify(ICard card) {
        if (card != null && !eventList.contains(card)) {
            LOG.log(Level.FINE, "Adding card: {0}", card.getName());
            addCard(card);
        }
    }

    private class DataLoader implements Runnable {

        @Override
        public void run() {
            if (!loaded && !loading) {
                loading = true;
                OutputHandler.output("Output", "Loading data into Table...");
                cards.setEnabled(loaded);
                List setsForGame = Lookup.getDefault().lookup(IDataBaseCardStorage.class).getSetsForGame(game);
                LOG.log(Level.FINE, "Cards to load: {0}", setsForGame.size());
                int count = 0;
                for (Iterator it = setsForGame.iterator(); it.hasNext();) {
                    if (stop) {
                        break;
                    }
                    ICardSet set = (ICardSet) it.next();
                    for (Iterator it2 = set.getCards().iterator(); it2.hasNext();) {
                        ICard card=(ICard) it2.next();
                        card.setSetName(set.getName());
                        addCard(card);
                        count++;
                    }
                }
                LOG.log(Level.FINEST, "Cards loaded: {0}", count);
                loaded = true;
                loading = false;
                cards.setEnabled(loaded);
                OutputHandler.output("Output", "Done!");
            }
        }
    }

    private void addCard(ICard card) {
        content.add(card);
        eventList.add(card);
    }

    @Override
    public void stop() {
        stop = true;
    }
}
