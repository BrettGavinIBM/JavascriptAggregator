/*
 * Copyright (c) 2004-2012, The Dojo Foundation All Rights Reserved.
 * Available via Academic Free License >= 2.1 OR the modified BSD license.
 * see: http://dojotoolkit.org/license for details
 */

package com.ibm.jaggr.service.impl.config;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.ibm.jaggr.service.impl.config.messages"; //$NON-NLS-1$
	public static String ConfigImpl_1;
	public static String ConfigImpl_2;
	public static String ConfigImpl_4;
	public static String ConfigImpl_6;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
