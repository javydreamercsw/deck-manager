/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mtg.card.seller;

import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({mtg.card.seller.ParseMtgFanaticPricesTest.class, mtg.card.seller.FindMagicCardsPricesTest.class})
public class SellerSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    private static final Logger LOG = Logger.getLogger(SellerSuite.class.getName());
    
}
