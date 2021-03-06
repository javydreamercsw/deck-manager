package mtg.card.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.io.FileUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

import com.reflexit.magiccards.core.CannotDetermineSetAbbriviation;
import com.reflexit.magiccards.core.cache.AbstractCardCache;
import com.reflexit.magiccards.core.cache.ICacheData;
import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.Editions.Edition;
import com.reflexit.magiccards.core.model.ICard;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.ICardType;
import com.reflexit.magiccards.core.model.IGame;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.DataBaseStateListener;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;
import com.reflexit.magiccards.core.storage.DataBaseCardStorage;
import com.reflexit.magiccards.core.storage.database.Card;
import com.reflexit.magiccards.core.storage.database.CardSet;
import com.reflexit.magiccards.core.storage.database.CardType;
import com.reflexit.magiccards.core.storage.database.Game;
import com.reflexit.magiccards.core.storage.database.controller.CardSetJpaController;
import com.reflexit.magiccards.core.storage.database.controller.exceptions.NonexistentEntityException;

import dreamer.card.game.core.GameUpdater;
import dreamer.card.game.core.UpdateProgressListener;
import mtg.card.MagicCard;
import mtg.card.sync.ParseGathererSets;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProviders(
        {
          @ServiceProvider(service = DataBaseStateListener.class),
          @ServiceProvider(service = GameUpdater.class)
        })
public final class MTGUpdater extends GameUpdater implements DataBaseStateListener,
        UpdateProgressListener
{

  private String currentSet = "";
  private static final Pattern COUNT
          = Pattern.compile("SEARCH:<p class=\"termdisplay\">  <span id=\"ctl00_ctl00_ctl00_MainContent_SubContent_SubContentHeader_searchTermDisplay\".*><i>.*</i>  \\((\\d+)\\)</span>");
  private static final Pattern LAST
          = Pattern.compile("\\Q<span style=\"visibility:hidden;\">&nbsp;&gt;</span></div>");
  private static final Pattern SPAN
          = Pattern.compile("class=[^>]*>(.*)</span>");
  private static final Pattern DIV
          = Pattern.compile("class=[^>]*>(.*?)</div>");
  private static final Pattern ID
          = Pattern.compile("href=.*/Card/Details.aspx\\?multiverseid=(\\d+)");
  private static final Pattern SET
          = Pattern.compile("title=\"(.*) \\((.*)\\)\" src=.*set=(\\w+)");
  private static final Pattern NAME
          = Pattern.compile(".*>(.*)</a></span>");
  private static final Pattern POW
          = Pattern.compile("\\((\\d+/)?(\\d+)\\)");
  private static String LONG_MINUS;
  private final static Charset UTF_8 = Charset.forName("utf-8");
  private final static Map MANAMAP = new LinkedHashMap();
  private static final Logger LOG
          = Logger.getLogger(MTGUpdater.class.getSimpleName());
  protected final String source
          = "http://gatherer.wizards.com/Pages/Search/Default.aspx?set=%5b%22";
  private final AtomicInteger updateCount = new AtomicInteger();
  private ProgressHandle ph;
  private final InputOutput console
          = IOProvider.getDefault().getIO("Database Update", true);
  private final AtomicBoolean pause = new AtomicBoolean(false);
  private final ArrayList<SetUpdateData> data = new ArrayList<>();
  private final HashMap parameters = new HashMap();
  private final AtomicInteger progress = new AtomicInteger();

  static
  {
    MANAMAP.put("\\Q{500}", "{0.5}");
    MANAMAP.put("\\{(\\d)([BUGRW])\\}", "{$1/$2}");
    MANAMAP.put("\\{([BUGRW])([BUGRW])\\}", "{$1/$2}");
    MANAMAP.put("\\Q{tap}", "{T}");
    MANAMAP.put("\\Q{untap}", "{Q}");
  }

  static
  {
    try
    {
      LONG_MINUS = new String(new byte[]
      {
        (byte) 0xe2, (byte) 0x80,
        (byte) 0x94
      }, UTF_8.name());
    }
    catch (UnsupportedEncodingException e)
    {
      LOG.log(Level.SEVERE, null, e);
    }
  }

  public MTGUpdater()
  {
    super(new MTGGame());
    setCodeNameBase("net.sourceforge.javydreamercsw.deck.manager.MTG");
  }

  @Override
  public void updateRemote()
  {
    synchronized (this)
    {
      if (!remoteUpdated.get())
      {
        if (!dbError.get())
        {
          updating.set(true);
          //Now update from the internet
          try
          {
            ParseGathererSets parser = new ParseGathererSets();
            parser.load();
            Collection<Editions.Edition> editions
                    = Editions.getInstance().getEditions();
            data.clear();
            //This section updates from the internet
            //Get the sets
            updateProgressMessage("Gathering stats before start processing...");
            parameters.clear();
            parameters.put("name", getGame().getName());
            List temp = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                    .namedQuery("CardSet.findAll");
            LOG.log(Level.FINE, "{0} sets found in database.", temp.size());
            int i = 0;
            if (LOG.isLoggable(Level.FINE))
            {
              for (Iterator it = temp.iterator(); it.hasNext();)
              {
                processStatus();
                CardSet cs = (CardSet) it.next();
                LOG.log(Level.FINE,
                        (MessageFormat.format("{0} {1}", ++i,
                                cs.getName())));
              }
            }
            //Chek to see if there's something new to update.
            ArrayList<Editions.Edition> setsToLoad = new ArrayList<>();
            for (Editions.Edition edition : editions)
            {
              processStatus();
              if (!setsToLoad.contains(edition))
              {
                LOG.log(Level.FINE,
                        "Unable to find set: {0}",
                        edition.getName());
                setsToLoad.add(edition);
              }
            }
            if (!setsToLoad.isEmpty())
            {
              updateProgressMessage("Calculating amount of pages to process...");
              try
              {
                for (Iterator iterator = setsToLoad.iterator(); iterator.hasNext();)
                {
                  processStatus();
                  if (!dbError.get())
                  {
                    Editions.Edition edition = (Editions.Edition) iterator.next();
                    String from = MessageFormat.format("{0}{1}%22%5d", source,
                            edition.getName().replaceAll(" ", "+"));
                    int urlAmount = checkAmountOfPagesForSet(from);
                    parameters.clear();
                    parameters.put("name", edition.getName());
                    List result = Lookup.getDefault()
                            .lookup(IDataBaseCardStorage.class)
                            .namedQuery("CardSet.findByName", parameters);
                    long amount = 0;
                    CardSet set;
                    if (!result.isEmpty())
                    {
                      set = (CardSet) result.get(0);
                      parameters.clear();
                      parameters.put("gameId",
                              set.getCardSetPK().getGameId());
                      amount = (Long) Lookup.getDefault()
                              .lookup(IDataBaseCardStorage.class)
                              .createdQuery("SELECT count(c) FROM CardSet c "
                                      + "WHERE c.cardSetPK.gameId = :gameId",
                                      parameters).get(0);
                    }
                    if (amount < urlAmount)
                    {
                      LOG.log(Level.FINE,
                              "Adding set: {0} to processing list because {1}.",
                              new Object[]
                              {
                                edition.getName(), result.isEmpty()
                                ? "it was not in the database"
                                : "is missing pages in the database"
                              });
                      data.add(new SetUpdateData(edition.getName(),
                              from, edition, urlAmount - amount));
                    }
                    else
                    {
                      LOG.log(Level.FINE,
                              "Skipping processing of set: {0}",
                              edition.getName());
                    }
                  }
                  else
                  {
                    LOG.log(Level.SEVERE, "Database Error!");
                    break;
                  }
                }
              }
              catch (DBException ex)
              {
                LOG.log(Level.SEVERE, null, ex);
                dbError.set(true);
              }
              int totalPages = 0;
              for (SetUpdateData setData : data)
              {
                processStatus();
                totalPages += setData.getPagesInSet();
              }
              LOG.log(Level.FINE, "Pages to update: {0}", totalPages);
              //Update card cache
              if (totalPages > 0)
              {
                setSize(totalPages);
                console.select();
                for (Iterator<SetUpdateData> it = data.iterator(); it.hasNext();)
                {
                  processStatus();
                  if (!dbError.get())
                  {
                    SetUpdateData setData = it.next();
                    SetDownloadThread ct
                            = new SetDownloadThread(setData,
                                    console,
                                    (Game) Lookup.getDefault().lookup(IDataBaseCardStorage.class).getGame(getGame().getName()),
                                    Lookup.getDefault().lookup(MTGCardCache.class));
                    ct.addListener(this);
                    executor.execute(ct);
                  }
                  else
                  {
                    updateProgressMessage("Database error prevented further processing!");
                    break;
                  }
                }
              }
              try
              {
                if (!dbError.get())
                {
                  for (Object o : Lookup.getDefault()
                          .lookup(IDataBaseCardStorage.class)
                          .getSetsForGame((Game) Lookup.getDefault().lookup(IDataBaseCardStorage.class).getGame(getGame().getName())))
                  {
                    processStatus();
                    CardSet cs = (CardSet) o;
                    for (ICardCache cache : getGame().getCardCacheImplementations())
                    {
                      processStatus();
                      if (!new File(cache.getSetIconPath((ICardSet) cs)).exists())
                      {
                        cache.getSetIcon((ICardSet) cs);
                      }
                    }
                  }
                }
              }
              catch (IOException ex)
              {
                LOG.log(Level.SEVERE, null, ex);
              }
            }
          }
          catch (DBException ex)
          {
            LOG.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            dbError.set(true);
          }
          catch (IOException ex)
          {
            LOG.log(Level.SEVERE, null, ex);
          }
        }
        remoteUpdated.set(true);
      }
    }
    updating.set(false);
    remoteUpdating.set(false);
  }

  @Override
  public String getActionName()
  {
    return MessageFormat.format("Updating {0} game...", getGame().getName());
  }

  /**
   * Create pages from set URL
   *
   * @param from URL containing pages for the set
   * @throws MalformedURLException
   * @throws IOException
   */
  protected void createCardsForSet(String from) throws MalformedURLException,
          IOException
  {
    LOG.log(Level.FINE, "Retrieving from url: {0}", from);
    int i = 0;
    boolean lastPage = false;
    while (lastPage == false)
    {
      try
      {
        URL url = new URL(MessageFormat.format("{0}&page={1}", from, i));
        lastPage = loadUrl(url);
        i++;
      }
      catch (NonexistentEntityException ex)
      {
        LOG.log(Level.SEVERE, null, ex);
        dbError.set(true);
        break;
      }
    }
    LOG.log(Level.FINE, "Created {0} pages for set!", i);
    updateProgressMessage("Created " + i + " pages for set!");
  }

  private boolean loadUrl(URL url) throws IOException, NonexistentEntityException
  {
    InputStream openStream;
    InputStreamReader is = null;
    BufferedReader st = null;
    boolean res = false;
    try
    {
      openStream = url.openStream();
      LOG.log(Level.FINE, "Loading from: {0}", url.toString());
      is = new InputStreamReader(openStream, UTF_8);
      st = new BufferedReader(is);
      res = processFile(st);
    }
    finally
    {
      if (st != null)
      {
        st.close();
      }
      if (is != null)
      {
        is.close();
      }
    }
    return res;
  }

  private int checkAmountOfPagesForSet(String from) throws IOException
  {
    InputStream openStream;
    InputStreamReader is = null;
    BufferedReader st = null;
    int amount = 0;
    try
    {
      URL url = new URL(from);
      openStream = url.openStream();
      is = new InputStreamReader(openStream, UTF_8);
      st = new BufferedReader(is);
      amount = getAmountOfPagesForSet(st);
    }
    finally
    {
      if (st != null)
      {
        st.close();
      }
      if (is != null)
      {
        is.close();
      }
    }
    return amount;
  }

  private int getAmountOfPagesForSet(BufferedReader st) throws IOException
  {
    String line;
    int amount = 0;
    while ((line = st.readLine()) != null)
    {
      Matcher cm = COUNT.matcher(line);
      if (cm.find())
      {
        amount = Integer.parseInt(cm.group(1));
        break;
      }
    }
    return amount;
  }

  private boolean processFile(BufferedReader st) throws IOException,
          NonexistentEntityException
  {
    String line = "";
    int state = 0;
    boolean lastPage = false;
    boolean cards = false;
    while ((state == 0 && (line = st.readLine()) != null)
            || (state == 1) && !dbError.get())
    {
      processStatus();
      if (LAST.matcher(line).find())
      {
        lastPage = true;
      }
      if (line.matches(".*class=\"cardItem .*"))
      {
        try
        {
          StringBuilder tr = new StringBuilder();
          do
          {
            if (line.matches(".*</tr>.*"))
            {
              state = 1;
              break;
            }
            tr.append(line).append(" ");
          }
          while ((line = st.readLine()) != null);
          parseRecord(tr.toString());
          cards = true;
          continue;
        }
        catch (DBException ex)
        {
          LOG.log(Level.SEVERE, null, ex);
          throw new IOException(ex);
        }
      }
      state = 0;
    }
    if (!cards)
    {
      try
      {
        LOG.log(Level.SEVERE, "Unable to retrieve pages from set: {0}",
                getCurrentSet());
        //Delete it from database
        HashMap properties = new HashMap();
        properties.put("name", getCurrentSet());
        List result = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .namedQuery("CardSet.findByName", properties);
        if (!result.isEmpty())
        {
          CardSet temp = (CardSet) result.get(0);
          CardSetJpaController controller
                  = new CardSetJpaController(((DataBaseCardStorage) Lookup.getDefault()
                          .lookup(IDataBaseCardStorage.class))
                          .getEntityManagerFactory());
          controller.destroy(temp.getCardSetPK());
        }
        return true;
      }
      catch (DBException ex)
      {
        LOG.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        dbError.set(true);
        return false;
      }
    }
    return lastPage;
  }

  private void parseRecord(String line) throws DBException
  {
    MagicCard card = new MagicCard();
    // split by td
    String[] rows = line.split("<td");
    String[] fields = rows[2].split("<span|<div");
    String id = getMatch(ID, fields[3]);
    card.setId(id);
    card.setName(getMatch(NAME, fields[3]));
    String cost = getMatch(SPAN, fields[4]);
    card.setCost(cost);
    String type = getMatch(SPAN, fields[6]);
    String powerCombo = type;
    String pow = getMatch(POW, powerCombo, 1).replaceFirst("/", "");
    String tou = getMatch(POW, powerCombo, 2);
    type = type.replaceAll("\\(.*", "").trim();
    card.setType(type);
    String text = fixText(getMatch(DIV, fields[7]));
    card.setOracleText(text);
    card.setPower(pow);
    card.setToughness(tou);
    String[] sets = rows[3].split("<a onclick");
    for (String temp : sets)
    {
      processStatus();
      String edition = getMatch(SET, temp, 1);
      String rarity = getMatch(SET, temp, 2);
      String setId = getMatch(ID, temp, 1);
      if (edition.length() <= 1)
      {
        continue;
      }
      edition = edition.trim();
      if (id.equals(setId))
      {
        addCardToSet(card, edition, rarity, type);
        updateProgressMessage("Added " + card.getName() + " to set: "
                + edition);
      }
      else if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class)
              .cardSetExists(edition, new MTGGame()))
      {
        LOG.log(Level.WARNING, "Is this a printing for card: {0} ID: {1} Set: {2}?",
                new Object[]
                {
                  card.getName(), id, edition
                });
      }
    }
  }

  private synchronized Card addCardToSet(MagicCard card, String edition, String rarity,
          String type) throws DBException
  {
    Card c = null;
    if (!dbError.get())
    {
      String message = MessageFormat.format("Updating card: {0}",
              card.getName());
      console.getOut().println(message);
      if (ph != null)
      {
        ph.progress(message);
      }
      List<Object> result;
      CardType ct;
      card.setSetName(edition);
      card.setRarity(rarity.trim());
      parameters.clear();
      parameters.put("name", card.getSetName());
      result = Lookup.getDefault()
              .lookup(IDataBaseCardStorage.class).namedQuery("CardSet.findByName",
              parameters);
      if (result.isEmpty())
      {
        throw new DBException("Unable to find set: " + card.getSetName());
      }
      CardSet set = (CardSet) result.get(0);
      //Handle card type, it might be new
      parameters.put("name", type);
      try
      {
        if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class).cardTypeExists(type))
        {
          ct = (CardType) Lookup.getDefault()
                  .lookup(IDataBaseCardStorage.class).createCardType(type);
        }
        else
        {
          ct = (CardType) Lookup.getDefault()
                  .lookup(IDataBaseCardStorage.class).getCardType(type);
        }
      }
      catch (DBException ex)
      {
        LOG.log(Level.SEVERE, null, ex);
        return null;
      }
      ICardSet temp = null;
      List<ICardSet> setsForGame
              = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                      .getSetsForGame(getGame());
      for (ICardSet x : setsForGame)
      {
        processStatus();
        if (x.getName().equals(set.getName()))
        {
          temp = x;
          break;
        }
      }
      //Create the card
      if (Lookup.getDefault().lookup(IDataBaseCardStorage.class)
              .cardExists(card.getName(), temp))
      {
        parameters.clear();
        parameters.put("name", card.getName());
        c = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .namedQuery("Card.findByName", parameters).get(0);
      }
      else
      {
        try
        {
          c = (Card) Lookup.getDefault().lookup(IDataBaseCardStorage.class).createCard(ct,
                  card.getName(), card.getOracleText() == null
                  ? "".getBytes() : card.getOracleText().getBytes(), temp);
          LOG.log(Level.FINE, "Created card: {0}", c.getName());
        }
        catch (DBException ex)
        {
          LOG.log(Level.SEVERE, "Error creating card: " + card.getName(), ex);
        }
      }
      boolean updateCache = false;
      if (c != null)
      {
        //Add the card attributes
        try
        {
          HashMap<String, String> attributes = new HashMap<>();
          attributes.put("Rarity", card.getRarity());
          attributes.put("Cost", card.getCost());
          attributes.put("Color Type", card.getColorType());
          attributes.put("Language", card.getLanguage());
          attributes.put("Part", card.getPart());
          attributes.put("Power", card.getPower());
          attributes.put("Rulings", card.getRulings());
          attributes.put("Toughness", card.getToughness());
          attributes.put("Type", card.getType());
          attributes.put("CardId", "" + card.getCardId());
          IDataBaseCardStorage storage
                  = Lookup.getDefault().lookup(IDataBaseCardStorage.class);
          synchronized (this)
          {
            //This only adds it if it doesn't exist
            storage.addAttributesToCard(c, attributes);
            if (storage.cardExists(c.getName(), set))
            {
              //Update card instead
              storage.updateCard(ct, c.getName(), c.getText(), set);
            }
            else
            {
              //Add the card to the set
              storage.addCardToSet(c, set);
              updateCache = true;
            }
          }
        }
        catch (DBException ex)
        {
          LOG.log(Level.SEVERE, "Error adding attributes to card: "
                  + card.getName(), ex);
          dbError.set(true);
          return null;
        }
      }
      if (updateCache)
      {
        try
        {
          parameters.clear();
          parameters.put("name", card.getName());
          c = (Card) Lookup.getDefault()
                  .lookup(IDataBaseCardStorage.class)
                  .namedQuery("Card.findByName", parameters).get(0);
          //Add to caching list
          c.setSetName(edition);
          Lookup.getDefault().lookup(ICacheData.class).add(c);
          if (temp != null)
          {
            MTGCardCache cache = new MTGCardCache();
            cache.getCardImage(c, temp, cache.createRemoteImageURL(c,
                    Editions.getInstance().getEditionByName(temp.getName())),
                    true, false);
          }
        }
        catch (IOException | CannotDetermineSetAbbriviation ex)
        {
          LOG.log(Level.SEVERE, "Error updating card: "
                  + card.getName(), ex);
        }
      }
      increaseProgress();
    }
    return c;
  }

  private String getMatch(Pattern textPattern, String typeF)
  {
    return getMatch(textPattern, typeF, 1);
  }

  private String getMatch(Pattern textPattern, String line, int g)
  {
    Matcher matcher;
    matcher = textPattern.matcher(line);
    String text = "";
    if (matcher.find())
    {
      text = matcher.group(g);
      if (text == null)
      {
        text = "";
      }
    }
    String res = htmlToString(text).trim();
    if (res.length() == 0)
    {
      res = " ";
    }
    return res;
  }

  private String fixText(String str1)
  {
    String str = str1;
    str = str.replaceAll("</p><p>", "<br>");
    str = str.replaceFirst("<p>", "");
    str = str.replaceFirst("</p>", "");
    return str;
  }

  public String htmlToString(String str)
  {
    str = str.replaceAll(MessageFormat.format("Q {0}", LONG_MINUS), "-");
    str = str.replaceAll("&nbsp;", " ");
    str = str.replaceAll("&amp;", "&");
    str = str.replaceAll("&apos;", "'");
    str = str.replaceAll("&quot;", "\"");
    if (str.contains("img"))
    {
      str = str.replaceAll("<img [^<]*name=([^&]*)&[^>]*/>", "{$1}");
      for (Iterator iterator = MANAMAP.keySet().iterator(); iterator.hasNext();)
      {
        processStatus();
        String alt = (String) iterator.next();
        String to = (String) MANAMAP.get(alt);
        str = str.replaceAll(alt, to);
      }
    }
    return str;
  }

  /**
   * @return the currentSet
   */
  public String getCurrentSet()
  {
    return currentSet;
  }

  /**
   * @param currentSet the currentSet to set
   */
  public void setCurrentSet(String currentSet)
  {
    this.currentSet = currentSet;
  }

  public static void main(String[] args)
  {
    File cacheDir = new File(System.getProperty("user.dir")
            + "/target/cache");
    LOG.log(Level.INFO, "Setting cache directory at: {0}",
            cacheDir.getAbsolutePath());
    AbstractCardCache.setCacheDir(cacheDir);
    MTGUpdater updater = new MTGUpdater();
    try
    {
      Lookup.getDefault().lookup(IDataBaseCardStorage.class).initialize();
    }
    catch (DBException ex)
    {
      Exceptions.printStackTrace(ex);
      return;
    }
    LOG.log(Level.INFO, "Waiting for cache...");
    updater.getGame().getCardCacheImplementations().stream().map((cache)
            -> (MTGCardCache) cache).forEachOrdered((c) ->
    {
      while (c.isLoading())
      {
        try
        {
          Thread.sleep(100);
        }
        catch (InterruptedException ex)
        {
          Exceptions.printStackTrace(ex);
        }
      }
    });
    LOG.log(Level.INFO, "Done!");

    //Let's copy it to the resources folder. This is done to update the pre-packaged database.
    File target = new File(System.getProperty("user.dir")
            + System.getProperty("file.separator")
            + "src"
            + System.getProperty("file.separator")
            + "main"
            + System.getProperty("file.separator")
            + "resources"
            + System.getProperty("file.separator"));
    if (target.isDirectory())
    {
      //Is a valid folder path, make sure to create any needed folders
      target.mkdirs();
      //Retrieve the database path
      EntityManagerFactory emf = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
              .getEntityManagerFactory();
      Map<String, Object> emfProperties = emf.getProperties();
      String url = (String) emfProperties.get("javax.persistence.jdbc.url");
      LOG.log(Level.FINE, "URL: {0}", url);
      //This assumes no parameters after the actual path.
      String path = url.substring(url.lastIndexOf(":") + 1,
              url.lastIndexOf("/"));
      LOG.log(Level.FINE, "Path: {0}", Paths.get(path));
      File db = new File(path);
      if (db.isDirectory() && db.exists())
      {
        try
        {
          //Let's copy it
          FileUtils.copyDirectoryToDirectory(db, target);
          LOG.log(Level.FINE, "Data base copied to {0}",
                  new Object[]
                  {
                    target.getAbsolutePath()
                  });
        }
        catch (IOException ex)
        {
          Exceptions.printStackTrace(ex);
        }
      }
      else
      {
        LOG.log(Level.WARNING, "Unable to find: {0}", db.getAbsolutePath());
      }
    }
    System.exit(0);
  }

  @Override
  public void loadDefaultCards(ProgressHandle ph) throws DBException
  {
    try
    {
      updateCount.set(0);
      EntityManagerFactory emf
              = Persistence.createEntityManagerFactory("Card_Game_InterfacePU");
      CardSetJpaController controller = new CardSetJpaController(emf);
      IDataBaseCardStorage storage
              = Lookup.getDefault().lookup(IDataBaseCardStorage.class);
      String gameName = new MTGGame().getName();
      IGame game;
      if (!storage.gameExists(gameName))
      {
        game = storage.createGame(new MTGGame().getName());
      }
      else
      {
        game = storage.getGame(gameName);
      }
      List<CardSet> sets = controller.findCardSetEntities();
      int amount = 0;
      //Calculate work
      amount = sets.stream().map((cs)
              -> cs.getCardList().size()).reduce(amount, Integer::sum);
      ph.switchToDeterminate(amount);
      sets.forEach((cs) ->
      {
        processStatus();
        copySet(cs, storage, game, ph);
      });
      localUpdating.set(false);
    }
    catch (DBException ex)
    {
      Exceptions.printStackTrace(ex);
    }
  }

  private void copySet(CardSet cs, IDataBaseCardStorage storage,
          IGame game, ProgressHandle ph)
  {
    try
    {
      LOG.log(Level.FINE, "Processing set: {0}",
              new Object[]
              {
                cs.getName()
              });
      if (ph != null)
      {
        ph.progress("Processing set: " + cs.getName());
      }
      updateProgressMessage("Processing set: " + cs.getName());
      ICardSet ncs = storage.createCardSet(game, cs.getName(),
              cs.getAbbreviation(),
              cs.getReleased());
      for (Card c : cs.getCardList())
      {
        processStatus();
        ICardType ct;
        if (storage.cardTypeExists(c.getCardType().getName()))
        {
          ct = storage.getCardType(c.getCardType().getName());
        }
        else
        {
          ct = storage.createCardType(c.getCardType().getName());
        }
        ICard card = storage.createCard(ct, c.getName(),
                c.getText(), ncs);
        //Add attributes
        HashMap<String, String> attributes = new HashMap<>();
        c.getCardHasCardAttributeList().forEach((chca) ->
        {
          attributes.put(chca.getCardAttribute().getName(),
                  chca.getValue());
        });
        storage.addAttributesToCard(card, attributes);
        if (ph != null)
        {
          ph.progress(updateCount.addAndGet(1));
        }
        updateProgressMessage("Copied card: "
                + c.getName() + " to set " + ncs.getName());
        LOG.log(Level.FINE, "Copied card: {0} to set: {1}",
                new Object[]
                {
                  c.getName(), ncs.getName()
                });
      }
      LOG.log(Level.FINE, "Done copying set: {0}",
              new Object[]
              {
                cs.getName()
              });
      updateProgressMessage("Done copying set: " + ncs.getName());
    }
    catch (DBException ex)
    {
      Exceptions.printStackTrace(ex);
    }
  }

  @Override
  public void reportDone()
  {
    if (ph != null)
    {
      ph.progress(updateCount.addAndGet(1));
    }
  }

  @Override
  public void changeMessage(String message)
  {
    if (ph != null)
    {
      ph.progress(message);
    }
  }

  @Override
  public void suspend()
  {
    pause.set(true);
    if (ph != null)
    {
      ph.suspend("Paused...");
    }
  }

  @Override
  public void resume()
  {
    pause.set(false);
    if (ph != null)
    {
      ph.progress("Resuming...");
    }
  }

  @Override
  public void shutdown()
  {
    suspend();
    if (ph != null)
    {
      ph.progress("Shutting down...");
    }
  }

  private SetUpdateData getUpdateData(ICardSet set)
  {
    SetUpdateData setData = null;
    //See if the set has been parsed already.
    for (SetUpdateData entry : data)
    {
      if (entry.getName().equals(set.getName()))
      {
        setData = entry;
        break;
      }
    }
    return setData;
  }

  @Override
  public void updateSet(ICardSet set)
  {
    //It might be parsed already
    SetUpdateData setData = getUpdateData(set);

    //If not parsed let's wait.
    while (setData == null && updating.get())
    {
      try
      {
        Thread.sleep(100);
        setData = getUpdateData(set);
      }
      catch (InterruptedException ex)
      {
        LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
      }
    }
    if (setData != null)
    {
      MTGCardCache cache = null;
      for (ICardCache c : Lookup.getDefault().lookupAll(ICardCache.class))
      {
        if (c.getGameName().equals(getGame().getName()))
        {
          cache = (MTGCardCache) c;
          break;
        }
      }
      //We have the data, let's queue an update.
      if (cache != null)
      {
        SetDownloadThread ct
                = new SetDownloadThread(setData,
                        console,
                        (Game) getGame(),
                        cache);
        ct.addListener(this);
        executor.execute(ct);
      }
    }
    else
    {
      LOG.log(Level.WARNING, "Unable to update: {0}. It was never parsed?",
              set.getName());
    }
  }

  @Override
  public void updateCard(ICard card)
  {

  }

  private void processStatus()
  {
    while (pause.get())
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException ex)
      {
        LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
      }
    }
  }

  private abstract class NotifyingThread extends Thread
  {

    private final Set<UpdateProgressListener> listeners
            = new CopyOnWriteArraySet<>();

    public final void addListener(final UpdateProgressListener listener)
    {
      listeners.add(listener);
    }

    public final void removeListener(final UpdateProgressListener listener)
    {
      listeners.remove(listener);
    }

    private void notifyListeners()
    {
      listeners.forEach((listener) ->
      {
        listener.reportDone();
      });
    }

    @Override
    public final void run()
    {
      try
      {
        doRun();
      }
      finally
      {
        notifyListeners();
      }
    }

    public abstract void doRun();
  }

  private class SetDownloadThread extends NotifyingThread
  {

    private final SetUpdateData setData;
    private final InputOutput console;
    private final IGame mtg;
    private final MTGCardCache cache;

    public SetDownloadThread(SetUpdateData setData, InputOutput console,
            Game mtg, MTGCardCache cache)
    {
      this.setData = setData;
      this.console = console;
      this.mtg = mtg;
      this.cache = cache;
    }

    @Override
    public void doRun()
    {
      try
      {
        Edition edition = setData.getEdition();
        String message;
        if (!Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                .cardSetExists(edition.getName(),
                        new MTGGame()))
        {
          ICardSet set
                  = Lookup.getDefault().lookup(IDataBaseCardStorage.class)
                          .createCardSet(mtg,
                                  edition.getName(), edition.getMainAbbreviation(),
                                  edition.getReleaseDate());
          message = "Created set: " + edition.getName();
          LOG.log(Level.FINE, message);
          updateProgressMessage(message);
          cache.getSetIcon(set);
        }
        message = MessageFormat.format("Updating set: {0}",
                setData.getName());
        console.getOut().println(message);
        LOG.log(Level.FINE, message);
        updateProgressMessage(message);
        setCurrentSet(setData.getName());
        createCardsForSet(setData.getUrl());
      }
      catch (IOException | DBException ex)
      {
        LOG.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
      }
    }
  }

  @Override
  public void updateLocal()
  {
    //Nothing?
  }
}
