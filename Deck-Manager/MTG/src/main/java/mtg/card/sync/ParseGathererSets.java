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

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Retrieve legality info
 */
public class ParseGathererSets extends ParseGathererPage {

    private static final String SET_QUERY_URL_BASE = GATHERER_URL_BASE + "Pages/Default.aspx";
    /*-
     <b>
     Filter Card Set:
     </b>
     <p>
     <select name="ctl00$ctl00$MainContent$Content$SearchControls$setAddText" id="ctl00_ctl00_MainContent_Content_SearchControls_setAddText">
     <option value=""></option>
     <option value="Alara Reborn">Alara Reborn</option>
     <option value="Alliances">Alliances</option>
     <option value="Antiquities">Antiquities</option>
     ...
     </select>
     */
    private static Pattern setStartPattern = Pattern.compile("<b>\\s*Filter Card Set:.*?<option value=\"\"></option>(.*?)</select>");
    private static Pattern oneSetPattern = Pattern.compile("<option.*?>(.*?)</option>");
    private Collection<Edition> newSets = new ArrayList<Editions.Edition>();

    public ParseGathererSets() {
        setTitle("Updating sets...");
    }

    @Override
    protected void loadHtml(String html) {
        Editions editions = Editions.getInstance();
        html = html.replaceAll("\r?\n", " ");
        Matcher matcher = setStartPattern.matcher(html);
        if (matcher.find()) {
            String sets = matcher.group(1);
            Matcher mset = oneSetPattern.matcher(sets);
            while (mset.find()) {
                String name = mset.group(1).trim();
                if (name.length() == 0) {
                    continue;
                }
                name = name.replaceAll("&quot;", "\"");
                if (!editions.containsName(name)) {
                    Edition ed = editions.addEdition(name, null);
                    newSets.add(ed);
                } else {
                    editions.addEdition(name, null);
                }
            }
        }
    }

    public Collection<Edition> getNew() {
        return newSets;
    }

    @Override
    protected String getUrl() {
        return SET_QUERY_URL_BASE;
    }
}
