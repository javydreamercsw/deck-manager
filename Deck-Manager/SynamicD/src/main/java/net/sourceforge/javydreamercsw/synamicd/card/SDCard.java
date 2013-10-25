package net.sourceforge.javydreamercsw.synamicd.card;

import com.reflexit.magiccards.core.model.CardImpl;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.ICardModifiable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class SDCard extends CardImpl implements ISDCard, ICardModifiable {

    private String rarity;
    private final List<String> keywords = new ArrayList<>();
    private static final Logger LOG
            = Logger.getLogger(SDCard.class.getSimpleName());
    private String cost;
    private String damage;
    private String moveType;
    private String cardType;
    private String text;
    private String hp;
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCardId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean setObjectByField(ICardField field, String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getCost() {
        return cost;
    }

    @Override
    public String getRarity() {
        return rarity;
    }

    @Override
    public ICard cloneCard() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getLanguage() {
        return "English";
    }

    @Override
    public int getFlipId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean matches(ICardField left, String right) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDbPrice(float price) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getDbPrice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ImageIcon> getImages() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public void addKeyword(String keyword) {
        if (!keywords.contains(keyword)) {
            LOG.log(Level.FINE, "Adding keyword: {0}", keyword);
            keywords.add(keyword);
        }
    }

    /**
     * @return the keywords
     */
    public List<String> getKeywords() {
        return keywords;
    }

    public void setCost(String cost) {
        LOG.log(Level.FINE, "Setting cost to: {0}", cost);
        this.cost = cost;
    }

    public void setDamage(String damage) {
        LOG.log(Level.FINE, "Setting damage to: {0}", damage);
        this.damage = damage;
    }

    /**
     * @return the damage
     */
    @Override
    public String getDamage() {
        return damage;
    }

    public void setMoveType(String type) {
        this.moveType = type;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    public void setHp(String hp) {
        LOG.log(Level.FINE, "Setting HP to: {0}", hp);
        this.hp = hp;
    }

    /**
     * @return the moveType
     */
    public String getMoveType() {
        return moveType;
    }

    /**
     * @return the HP
     */
    @Override
    public String getHp() {
        return hp;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the cardType
     */
    @Override
    public String getCardType() {
        return cardType;
    }

    /**
     * @param cardType the cardType to set
     */
    @Override
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    @Override
    public int getEnglishCardId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
