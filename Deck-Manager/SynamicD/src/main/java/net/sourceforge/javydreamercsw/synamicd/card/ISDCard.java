package net.sourceforge.javydreamercsw.synamicd.card;

import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ISellableCard;

public interface ISDCard extends ISellableCard {

    /**
     * Get the page cost.
     *
     * @return page cost
     */
    public abstract String getCost();

    public abstract String getRarity();

    public ICard cloneCard();

    public abstract String getText();

    public abstract String getLanguage();

    public abstract int getEnglishCardId();

    public abstract int getFlipId();

    public abstract boolean matches(ICardField left, String right);

    public String getCardType();

    public void setCardType(String cardType);

    public void setRarity(String rarity);

    public String getDamage();

    public Object getHp();
}
