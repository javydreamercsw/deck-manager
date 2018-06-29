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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;

/**
 * Retrieve legality info
 */
public final class ParseGathererSets extends AbstractParseGathererPage
{

  private static final Logger LOG
          = Logger.getLogger(ParseGathererSets.class.getSimpleName());
  private static final String SET_QUERY_URL_BASE
          = MessageFormat.format("{0}Pages/Default.aspx", GATHERER_URL_BASE);
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
  private static final Pattern SET_START_PATTERN
          = Pattern.compile("Card Set:.*?<option value=\"\"></option>(.*?)</select>");
  private static final Pattern ONE_SET_PATTERN
          = Pattern.compile("<option.*?>(.*?)</option>");
  private final Collection<Edition> newSets
          = new ArrayList<>();
  private final Collection<String> allParsed = new ArrayList<>();

  public ParseGathererSets()
  {
    setTitle("Updating sets...");
  }

  @Override
  protected void loadHtml(String html)
  {
    html = html.replaceAll("\r?\n", " ");
    Matcher matcher = SET_START_PATTERN.matcher(html);
    if (matcher.find())
    {
      String sets = matcher.group(1);
      Matcher mset = ONE_SET_PATTERN.matcher(sets);
      while (mset.find())
      {
        String name = mset.group(1).trim();
        if (name.length() == 0)
        {
          continue;
        }
        name = name.replaceAll("&quot;", "\"");
        LOG.log(Level.FINE, "Parsed set: {0}", name);
        allParsed.add(name);
        if (!Editions.getInstance().containsName(name))
        {
          Edition ed = Editions.getInstance().addEdition(name, null);
          newSets.add(ed);
        }
        else
        {
          Editions.getInstance().addEdition(name, null);
        }
      }
    }
  }

  public Collection<Edition> getNew()
  {
    return newSets;
  }

  @Override
  protected String getUrl()
  {
    return SET_QUERY_URL_BASE;
  }

  public Collection<String> getAll()
  {
    return allParsed;
  }
}
