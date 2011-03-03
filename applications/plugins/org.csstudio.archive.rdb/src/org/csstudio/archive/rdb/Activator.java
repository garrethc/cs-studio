/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.archive.rdb;

import java.util.logging.Logger;

import org.eclipse.core.runtime.Plugin;

/** Plugin Activator
 *  @author Kay Kasemir
 */
public class Activator extends Plugin
{
    /** Plugin ID defined in MANIFEST.MF */
    final public static String ID = "org.csstudio.archive.rdb"; //$NON-NLS-1$

    final private static Logger logger = Logger.getLogger(ID);

    /** @return Logger for plugin ID */
    public static Logger getLogger()
    {
        return logger;
    }
}
