package com.reflexit.magiccards.core;

import java.io.IOException;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class CachedImageNotFoundException extends IOException {
	public CachedImageNotFoundException(String s) {
		super(s);
	}
    private static final Logger LOG = Logger.getLogger(CachedImageNotFoundException.class.getName());
}
