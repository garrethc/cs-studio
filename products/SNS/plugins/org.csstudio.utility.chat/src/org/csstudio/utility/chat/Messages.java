/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.chat;

import org.eclipse.osgi.util.NLS;

/** Localized messages
 *  @author Kay Kasemir
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.csstudio.utility.chat.messages"; //$NON-NLS-1$

	public static String AcceptInvitationFmt;
	public static String ChatInvitation;
	public static String ConnectionErrorFmt;
	public static String Error;
	public static String IndividualChatTitleFmt;
	public static String LeaveChatFmt;
	public static String OpenViewErrorFmt;
	public static String Participants;
	public static String PreferencesGroup;
	public static String PreferencesServer;
	public static String PreferencesTitle;
	public static String Send;
	public static String SendErrorFmt;
	public static String UserName;
	public static String UserSERVER;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
		// Prevent instantiation
	}
}
