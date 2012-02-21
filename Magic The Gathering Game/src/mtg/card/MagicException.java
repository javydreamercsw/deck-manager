/**
 * *****************************************************************************
 * Copyright (c) 2008 Alena Laskavaia. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Alena Laskavaia - initial API and implementation
 ******************************************************************************
 */
package mtg.card;

public class MagicException extends RuntimeException {

    private static final long serialVersionUID = 242374882L;

    public MagicException() {
        super();
    }

    public MagicException(String message, Throwable cause) {
        super(message, cause);
    }

    public MagicException(String message) {
        super(message);
    }

    public MagicException(Throwable cause) {
        super(cause);
    }
}
