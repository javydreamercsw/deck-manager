package mtg.card;

public class CannotDetermineSetAbbriviation extends MagicException {

    private static final long serialVersionUID = 5548480990926987096L;

    public CannotDetermineSetAbbriviation(IMagicCard card) {
        super("Cannot determine set abbreviation for " + card.getSet());
    }
}
