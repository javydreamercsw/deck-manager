/**
 * *****************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 * *****************************************************************************
 */
package mtg.card;

import com.reflexit.magiccards.core.model.CardGroup;
import com.reflexit.magiccards.core.model.ICardCountable;
import com.reflexit.magiccards.core.model.storage.ICardStore;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Alena
 *
 */
public final class CardStoreUtils {

    public static CardStoreUtils getInstance() {
        if (instance == null) {
            instance = new CardStoreUtils();
        }
        return instance;
    }
    public static CardStoreUtils instance;
    private static final CardTypes MTYPES = CardTypes.getInstance();

    /**
     * mana curve is array 0 .. 8 of card counts, where non-land is counted,
     * arr[8] - is cards with X cost in it, arr[7] - is 7+
     *
     * @param store
     * @return mana curve for given store
     */
    public int[] buildManaCurve(ICardStore store) {
        int bars[] = new int[9];
        for (Iterator iterator = store.iterator(); iterator.hasNext();) {
            IMagicCard elem = (IMagicCard) iterator.next();
            int cost = elem.getCmc();
            if (elem.getCost().length() == 0) {
                continue; // land
            }
            int count;
            if (elem instanceof ICardCountable) {
                count = ((ICardCountable) elem).getCount();
            } else {
                count = 1;
            }
            if (elem.getCost().contains("X")) //$NON-NLS-1$
            {
                bars[8] += count;
            } else if (cost < 7 && cost >= 0) {
                bars[cost] += count;
            } else if (cost >= 7) {
                bars[7] += count;
            }
        }
        return bars;
    }

    public float getAverageManaCost(ICardStore store) {
        int total = 0;
        int sum = 0;
        for (Iterator iterator = store.iterator(); iterator.hasNext();) {
            IMagicCard elem = (IMagicCard) iterator.next();
            int cost = elem.getCmc();
            if (elem.getCost().length() == 0) {
                continue; // land
            }
            int count;
            if (elem instanceof ICardCountable) {
                count = ((ICardCountable) elem).getCount();
            } else {
                count = 1;
            }
            sum += cost;
            total += count;
        }
        if (total == 0) {
            return 0;
        }
        return sum / (float) total;
    }

    public static Collection<IMagicCard> randomize(ICardStore store) {
        ArrayList<IMagicCard> filteredList = new ArrayList<IMagicCard>();
        for (Iterator<IMagicCard> iterator = store.iterator(); iterator.hasNext();) {
            IMagicCard elem = iterator.next();
            int count;
            if (elem instanceof ICardCountable) {
                ICardCountable card = (ICardCountable) elem;
                count = card.getCount();
                for (int i = 0; i < count; i++) {
                    MagicCardPhisical nc = new MagicCardPhisical(elem);
                    nc.setCount(1);
                    filteredList.add(nc);
                }
            } else {
                filteredList.add(elem);
            }
        }
        ArrayList<IMagicCard> newList = new ArrayList<IMagicCard>(filteredList.size());
        Random r = new Random(System.currentTimeMillis() * filteredList.hashCode());
        while (filteredList.size() > 0) {
            int index = r.nextInt(filteredList.size());
            newList.add(filteredList.get(index));
            filteredList.remove(index);
        }
        return newList;
    }

    private CardStoreUtils() {
        // CardTextEN.EN = "EN"; // fake initializion
    }

    public int[] buildTypeStats(ICardStore store) {
        int bars[] = new int[3]; // land, creatures, non-creatures
        for (Iterator iterator = store.iterator(); iterator.hasNext();) {
            IMagicCard elem = (IMagicCard) iterator.next();
            String type = elem.getType();
            if (type == null) {
                continue;
            }
            int count = 1;
            if (elem instanceof ICardCountable) {
                count = ((ICardCountable) elem).getCount();
            }
            if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Land"))) {
                bars[0] += count;
            } else if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Creature"))) {
                bars[1] += count;
            } else {
                bars[2] += count;
            }
        }
        return bars;
    }

    public static int countCards(Iterable store) {
        int count = 0;
        synchronized (store) {
            for (Object element : store) {
                if (element instanceof ICardCountable) {
                    count += ((ICardCountable) element).getCount();
                } else {
                    count++;
                }
            }
            return count;
        }
    }

    public static Collection<String> buildColors(Iterable store) {
        HashSet<String> colors = new HashSet<String>();
        for (Object element : store) {
            IMagicCard elem = (IMagicCard) element;
            if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Land"))) {
                continue;
            }
            String name = Colors.getColorName(elem.getCost());
            String[] split = name.split("-"); //$NON-NLS-1$
            colors.addAll(Arrays.asList(split));
        }
        return colors;
    }

    public static String buildColorsCost(Iterable store) {
        String res = "";
        HashSet<String> colors = new HashSet<String>();
        for (Object element : store) {
            IMagicCard elem = (IMagicCard) element;
            if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Land"))) {
                continue;
            }
            String name = Colors.getColorName(elem.getCost());
            String[] split = name.split("-"); //$NON-NLS-1$
            colors.addAll(Arrays.asList(split));
        }
        for (Iterator iterator = Colors.getInstance().getNames().iterator(); iterator.hasNext();) {
            String c = (String) iterator.next();
            if (colors.contains(c)) {
                String encodeByName = Colors.getInstance().getEncodeByName(c);
                res += MessageFormat.format("{{0}}", encodeByName);
            }
        }
        return res;
    }

    public static Collection<CardGroup> buildSpellColorStats(Iterable store) {
        HashMap<CardGroup, CardGroup> groupsList = new HashMap();
        for (Object element : store) {
            IMagicCard elem = (IMagicCard) element;
            if (elem.getType() == null || MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Land"))) {
                continue;
            }
            String name = Colors.getColorName(elem.getCost());
            CardGroup g = new CardGroup(MagicCardField.COST, name);
            if (groupsList.containsKey(g)) {
                groupsList.get(g).addCount(1);
            } else {
                g.addCount(1);
                groupsList.put(g, g);
            }
        }
        return groupsList.keySet();
    }

    public CardGroup buildTypeGroups(Iterable iterable) {
        CardGroup spellNode = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Spell"));
        CardGroup landNode = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Land"));
        CardGroup unknownNode = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Unknown"));
        CardGroup basic = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Basic"));
        landNode.add(basic);
        CardGroup noncreatureNode = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Non_Creature"));
        spellNode.add(noncreatureNode);
        CardGroup creatureNode = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Creature"));
        spellNode.add(creatureNode);
        CardGroup instant = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Instant"));
        noncreatureNode.add(instant);
        CardGroup sorcery = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Sorcery"));
        noncreatureNode.add(sorcery);
        CardGroup ench = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Enchantment"));
        noncreatureNode.add(ench);
        CardGroup artifact = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Artifact"));
        noncreatureNode.add(artifact);
        CardGroup walker = new CardGroup(MagicCardField.TYPE, CardTypes.TYPES.getString("Type_Planeswalker"));
        noncreatureNode.add(walker);
        int total = 0;
        for (Iterator iterator = iterable.iterator(); iterator.hasNext();) {
            IMagicCard elem = (IMagicCard) iterator.next();
            int count = 1;
            try {
                String type = elem.getType();
                if (type == null) {
                    unknownNode.add(elem);
                    continue;
                }
                if (elem instanceof ICardCountable) {
                    count = ((ICardCountable) elem).getCount();
                }
                if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Land"))) {
                    if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Basic"))) {
                        basic.add(elem);
                        landNode.addCount(count);
                    } else {
                        landNode.add(elem);
                    }
                } else {
                    spellNode.addCount(count);
                    if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Creature"))) {
                        creatureNode.add(elem);
                    } else {
                        noncreatureNode.addCount(count);
                        if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Instant"))) {
                            instant.add(elem);
                        } else if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Enchantment"))) {
                            ench.add(elem);
                        } else if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Sorcery"))) {
                            sorcery.add(elem);
                        } else if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Artifact"))) {
                            artifact.add(elem);
                        } else if (MTYPES.hasType(elem, CardTypes.TYPES.getString("Type_Planeswalker"))) {
                            walker.add(elem);
                        } else {
                            noncreatureNode.addCount(-count);
                            noncreatureNode.add(elem);
                        }
                    }
                }
            } catch (Exception e) {
                unknownNode.add(elem);
            }
            total += count;
        }
        CardGroup root = new CardGroup(MagicCardField.TYPE, ""); //$NON-NLS-1$
        root.setCount(total);
        root.add(landNode);
        root.add(spellNode);
        if (unknownNode.getCount() > 0) {
            root.add(unknownNode);
        }
        return root;
    }
    private static final Logger LOG = Logger.getLogger(CardStoreUtils.class.getName());
}
