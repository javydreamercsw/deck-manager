package mtg.card.game;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openide.util.Lookup;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.ICardSet;
import com.reflexit.magiccards.core.model.storage.db.DBException;
import com.reflexit.magiccards.core.model.storage.db.IDataBaseCardStorage;

import dreamer.card.game.core.GameUpdater;
import dreamer.card.game.core.UpdateProgressListener;

public class MTGCardCacheNGTest implements UpdateProgressListener
{
  private ICardCache instance;
  private IDataBaseCardStorage storage;
  private final AtomicBoolean setupDone = new AtomicBoolean(false);

  @BeforeClass
  public void setup() throws DBException, InterruptedException
  {
    Lookup.getDefault().lookup(GameUpdater.class).addListener(this);

    System.out.println("Initializing storage...");
    //This might take some time while everything is updated and database populated.
    storage = Lookup.getDefault().lookup(IDataBaseCardStorage.class);
    assertNotNull(storage);
    storage.initialize();
    System.out.println("Done!");

    System.out.println("Initializing cache...");
    instance = Lookup.getDefault().lookup(ICardCache.class);
    assertNotNull(instance);
    assertTrue(instance instanceof MTGCardCache);

    while (!setupDone.get())
    {
      Thread.sleep(100);
    }
    System.out.println("Test setup done!");
  }

  @AfterClass
  public void cleanup()
  {
    storage.close();
    Lookup.getDefault().lookup(GameUpdater.class).removeListener(this);
  }
  /**
   * Test of createSetImageRemoteURL method, of class MTGCardCache.
   * @throws com.reflexit.magiccards.core.model.storage.db.DBException
   * @throws java.net.MalformedURLException
   */
  @Test
  public void testCreateSetImageRemoteURL() throws DBException,
          MalformedURLException,
          IOException
  {
    System.out.println("createSetImageRemoteURL");
    ICardSet set = storage.getCardSet("Limited Edition Alpha");
    assertNotNull(instance.getSetIcon(set));
  }

//  /**
//   * Test of getCacheTask method, of class MTGCardCache.
//   */
//  @Test
//  public void testGetCacheTask()
//  {
//    System.out.println("getCacheTask");
//    MTGCardCache instance = new MTGCardCache();
//    Runnable expResult = null;
//    Runnable result = instance.getCacheTask();
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of createRemoteImageURL method, of class MTGCardCache.
//   */
//  @Test
//  public void testCreateRemoteImageURL() throws Exception
//  {
//    System.out.println("createRemoteImageURL");
//    ICard icard = null;
//    Edition edtn = null;
//    MTGCardCache instance = new MTGCardCache();
//    URL expResult = null;
//    URL result = instance.createRemoteImageURL(icard, edtn);
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of loadCardImageOffline method, of class MTGCardCache.
//   */
//  @Test
//  public void testLoadCardImageOffline() throws Exception
//  {
//    System.out.println("loadCardImageOffline");
//    ICard icard = null;
//    Edition edtn = null;
//    boolean bln = false;
//    MTGCardCache instance = new MTGCardCache();
//    boolean expResult = false;
//    boolean result = instance.loadCardImageOffline(icard, edtn, bln);
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getGamePath method, of class MTGCardCache.
//   */
//  @Test
//  public void testGetGamePath()
//  {
//    System.out.println("getGamePath");
//    MTGCardCache instance = new MTGCardCache();
//    String expResult = "";
//    String result = instance.getGamePath();
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of isLoading method, of class MTGCardCache.
//   */
//  @Test
//  public void testIsLoading()
//  {
//    System.out.println("isLoading");
//    MTGCardCache instance = new MTGCardCache();
//    boolean expResult = false;
//    boolean result = instance.isLoading();
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of createManaImageURL method, of class MTGCardCache.
//   */
//  @Test
//  public void testCreateManaImageURL()
//  {
//    System.out.println("createManaImageURL");
//    String symbol = "";
//    URL expResult = null;
//    URL result = MTGCardCache.createManaImageURL(symbol);
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getManaIconPath method, of class MTGCardCache.
//   */
//  @Test
//  public void testGetManaIconPath()
//  {
//    System.out.println("getManaIconPath");
//    String mana = "";
//    MTGCardCache instance = new MTGCardCache();
//    String expResult = "";
//    String result = instance.getManaIconPath(mana);
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getGameIcon method, of class MTGCardCache.
//   */
//  @Test
//  public void testGetGameIcon() throws Exception
//  {
//    System.out.println("getGameIcon");
//    ICardGame game = null;
//    MTGCardCache instance = new MTGCardCache();
//    Image expResult = null;
//    Image result = instance.getGameIcon(game);
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getSetIcon method, of class MTGCardCache.
//   */
//  @Test
//  public void testGetSetIcon() throws Exception
//  {
//    System.out.println("getSetIcon");
//    ICardSet set = null;
//    MTGCardCache instance = new MTGCardCache();
//    Image expResult = null;
//    Image result = instance.getSetIcon(set);
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of getManaIcon method, of class MTGCardCache.
//   */
//  @Test
//  public void testGetManaIcon() throws Exception
//  {
//    System.out.println("getManaIcon");
//    String mana = "";
//    MTGCardCache instance = new MTGCardCache();
//    Image expResult = null;
//    Image result = instance.getManaIcon(mana);
//    assertEquals(result, expResult);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }

  @Override
  public void reportProgress(int amount)
  {
    System.out.println("Progress reported: " + amount);
  }

  @Override
  public void reportDone()
  {
    System.out.println("Completion reported!");
    setupDone.set(true);
  }

  @Override
  public void reportSize(int size)
  {
    System.out.println("Size reported: " + size);
  }

  @Override
  public void changeMessage(String message)
  {
    System.out.println(message);
  }

  @Override
  public void suspend()
  {
    System.out.println("Progress suspended!");
  }

  @Override
  public void resume()
  {
    System.out.println("Process resumed!");
  }

  @Override
  public void shutdown()
  {
    System.out.println("Shutting down!");
  }
}
