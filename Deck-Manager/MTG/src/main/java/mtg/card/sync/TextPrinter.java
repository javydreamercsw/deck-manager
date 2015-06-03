package mtg.card.sync;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import mtg.card.MagicCard;

public class TextPrinter {

    public static void printHeader(MagicCard card, PrintStream out) {
        out.println(join(card.getHeaderNames(), SEPARATOR));
    }
    public static String SEPARATOR = "|";

    public static void print(MagicCard card, PrintStream out) {
        out.println(join(card.getValues(), SEPARATOR));
    }

    private static String join(Collection list, String sep) {
        StringBuilder buf = new StringBuilder();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Object element = iter.next();
            if (element != null) {
                String string = element.toString();
                if (string.contains("\n")) {
                    string = string.replaceAll("\n", "<br>");
                }
                buf.append(string);
            } else {
                buf.append("");
            }
            if (iter.hasNext()) {
                buf.append(sep);
            }
        }
        return buf.toString();
    }
}
