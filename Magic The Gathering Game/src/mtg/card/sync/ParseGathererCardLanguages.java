/**
 * *****************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation Terry Long -
 * refactored ParseGathererLegality to instead retrieve rulings on cards
 *
 ******************************************************************************
 */
package mtg.card.sync;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Retrieve legality info
 */
public class ParseGathererCardLanguages extends ParseGathererPage {

    static final String SET_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Card/Languages.aspx";
    private String lang;
    private int cardId;
    private int langId;
    private int page;
    /*-
     <tr class="cardItem oddItem">
     <td class="fullWidth" style="text-align: center;">
     <a id="ctl00_ctl00_ctl00_MainContent_SubContent_SubContent_languageList_listRepeater_ctl07_cardTitle" href="Details.aspx?multiverseid=172550">Бурав Выжженной Пустоши</a>
     </td>
     <td style="text-align: center;">
     Russian
     </td>
     <td style="text-align: center;">

     русский язык
     </td>
     </tr>
     */
    private static Pattern rowPattern = Pattern.compile("<tr class=\"cardItem(.*?multiverseid=(\\d+).*?)</tr>");

    public static void main(String[] args) throws IOException {
        ParseGathererCardLanguages parser = new ParseGathererCardLanguages();
        parser.setCardId(153981);
        parser.setLanguage("Russian");
        parser.load();
    }

    public void setLanguage(String string) {
        lang = string;
    }

    public void setCardId(int i) {
        cardId = i;
    }

    @Override
    protected void loadHtml(String html) {
        html = html.replaceAll("\r?\n", " ");
        Matcher matcher = rowPattern.matcher(html);
        int count = 0;
        while (matcher.find()) {
            count++;
            String all = matcher.group(1);
            String id = matcher.group(2);
            if (all.contains(lang)) {
                langId = Integer.valueOf(id);
                break;
            }
        }
        if (langId == 0 && count >= 25 && page <= 3) {
            page++;
            try {
                load();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    @Override
    protected String getUrl() {
        return SET_QUERY_URL_BASE + "?page=" + page + "&multiverseid=" + cardId;
    }

    public int getLangCardId() {
        return langId;
    }
    private static final Logger LOG = Logger.getLogger(ParseGathererCardLanguages.class.getName());
}
