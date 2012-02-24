package mtg.card.data;

import dreamer.card.game.ICardHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
public class DataManager {

    private static ICardHandler handler;

    public synchronized static ICardHandler getCardHandler() {
        try {
            if (handler != null) {
                return handler;
            }
            // String variant1 =
            // "com.reflexit.magiccards.core.sql.handlers.CardHolder";
            String variant2 = "com.reflexit.magiccards.core.xml.XmlCardHolder";
            @SuppressWarnings("rawtypes")
            Class c = Class.forName(variant2);
            Object x = c.newInstance();
            handler = (ICardHandler) x;
            return handler;
        } catch (InstantiationException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private DataManager() {
    }
}
