package mtg.card;

import dreamer.card.game.ICard;
import dreamer.card.game.ICardField;
import dreamer.card.game.ISellableCard;
import mtg.card.MagicCardFilter.TextValue;

public interface IMagicCard extends ISellableCard {

    public static final MagicCard DEFAULT = new MagicCard();
    public static final float STAR_POWER = 911.0F;
    public static final float NOT_APPLICABLE_POWER = Float.NaN;

    public abstract String getCost();

    public abstract String getOracleText();

    public abstract String getRarity();

    public abstract String getType();

    public abstract String getPower();

    public abstract String getToughness();

    public abstract String getColorType();

    public abstract int getCmc();

    public abstract float getCommunityRating();

    public abstract String getArtist();

    public abstract String getRulings();

    public ICard cloneCard();

    public abstract MagicCard getBase();

    public abstract String getText();

    public abstract String getLanguage();

    public abstract int getEnglishCardId();

    public abstract int getFlipId();

    //TODO: maybe move to ICard
    public abstract boolean matches(ICardField left, TextValue right);
}