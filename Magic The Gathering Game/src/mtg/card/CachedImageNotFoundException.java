package com.reflexit.magiccards.core;

import java.io.IOException;

@SuppressWarnings("serial")
public class CachedImageNotFoundException extends IOException {
	public CachedImageNotFoundException(String s) {
		super(s);
	}
}
