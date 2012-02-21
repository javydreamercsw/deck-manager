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
import mtg.card.MagicCard;

/**
 * Retrieve legality info
 */
public class ParseGathererBasicInfo extends ParseGathererDetails {

    public ParseGathererBasicInfo() {
    }

    @Override
    protected String getUrl() {
        return super.getUrl() + "&printed=true";
    }

    @Override
    protected boolean isOracle() {
        return false;
    }

    public static void main(String[] args) throws IOException {
        MagicCard card = new MagicCard();
        card.setCardId(191338);
        ParseGathererBasicInfo parser = new ParseGathererBasicInfo();
        parser.setCard(card);
        parser.load();
        System.out.println(card.getName());
        System.out.println(card.getType());
        System.out.println(card.getOracleText());
    }
}
