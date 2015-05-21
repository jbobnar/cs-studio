/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.sysmon;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/** Plugin activator.
 *  @author Kay Kasemir
 */
public class Activator extends AbstractUIPlugin
{
    private static Activator plugin;

    /** Constructor */
    public Activator()
    {
        plugin = this;
    }

    /** @return The singleton instance. */
    static public Activator getDefault()
    {
        return plugin;
    }
}
