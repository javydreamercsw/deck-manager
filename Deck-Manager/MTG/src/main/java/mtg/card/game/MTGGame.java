package mtg.card.game;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

import com.reflexit.magiccards.core.cache.ICardCache;
import com.reflexit.magiccards.core.model.DefaultCardGame;
import com.reflexit.magiccards.core.model.ICardGame;
import com.reflexit.magiccards.core.model.IGameCellRendererImageFactory;

/**
 *
 * @author Javier A. Ortiz Bultrón <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = ICardGame.class)
public class MTGGame extends DefaultCardGame
{

  private static final Logger LOG = Logger.getLogger(MTGGame.class.getName());

  @Override
  public String getName()
  {
    return "Magic the Gathering";
  }

  @Override
  public Image getBackCardIcon()
  {
    try
    {
      return ImageIO.read(getClass().getResource("/images/back.jpg"));
    }
    catch (MalformedURLException ex)
    {
      LOG.log(Level.SEVERE, null, ex);
      return null;
    }
    catch (IOException ex)
    {
      LOG.log(Level.SEVERE, null, ex);
      return null;
    }
  }

  @Override
  public IGameCellRendererImageFactory getCellRendererImageFactory()
  {
    return (String string, Object value) ->
    {
      if (value != null)
      {
        List<ICardCache> impls = getCardCacheImplementations();
        if (impls.size() > 0 && ((String) value).contains("{")
                && ((String) value).contains("}"))
        {
          JLabel container = new JLabel();
          container.setLayout(new BoxLayout(container,
                  BoxLayout.X_AXIS));
          ArrayList<String> values = new ArrayList<>();
          StringTokenizer st
                  = new StringTokenizer((String) value, "}");
          while (st.hasMoreTokens())
          {
            String token = st.nextToken();
            values.add(token.substring(1));
          }
          for (Iterator<String> it = values.iterator(); it.hasNext();)
          {
            try
            {
              String v = it.next();
              JLabel iconLabel
                      = new JLabel(new ImageIcon((toBufferedImage(((MTGCardCache) impls.get(0)).getManaIcon(v)))));
              container.add(iconLabel);
              if (it.hasNext())
              {
                container.add(Box.createRigidArea(new Dimension(5, 0)));
              }
            }
            catch (IOException ex)
            {
              Exceptions.printStackTrace(ex);
            }
          }
          return container;
        }
        return new JLabel(((String) value));
      }
      else
      {
        return new JLabel();
      }
    };
  }
}
