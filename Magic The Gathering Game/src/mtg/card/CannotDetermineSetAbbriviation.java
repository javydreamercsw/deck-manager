package mtg.card;

import java.util.logging.Logger;

public class CannotDetermineSetAbbriviation extends MagicException {

    private static final long serialVersionUID = 5548480990926987096L;

    public CannotDetermineSetAbbriviation(IMagicCard card) {
        super("Cannot determine set abbreviation for " + card.getSet());
    }
    private static final Logger LOG = Logger.getLogger(CannotDetermineSetAbbriviation.class.getName());
}
