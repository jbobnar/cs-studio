/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.jaasauthentication.ui;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/** Content provider for list of strings.
 *  <p>
 *  Will provide Integer index values which allow the label provider
 *  and editor to access the correct elements in the string list.
 *  @author Kay Kasemir, Xihui Chen
 */
public class ModuleTableContentProvider implements IStructuredContentProvider
{
    /** Magic number for the final 'add' element */
    final public static Integer ADD_ELEMENT = new Integer(-1);
    private List<?> items;

    /** {@inheritDoc} */
    @Override
    public void inputChanged(final Viewer viewer, final Object old, final Object new_input)
    {
        items = (List<?>) new_input;
    }

    /** {@inheritDoc} */
    @Override
    public Object[] getElements(Object arg0)
    {
        int N = items.size();
        final Integer result[] = new Integer[N+1];
        for (int i=0; i<N; ++i)
            result[i] = i;
        result[N] = ADD_ELEMENT;
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void dispose()
    {
        // NOP
    }
}
