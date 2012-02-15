/*******************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.scan.ui.scantree.actions;

import java.util.List;

import org.csstudio.scan.command.ScanCommand;
import org.csstudio.scan.ui.scantree.ScanEditor;
import org.csstudio.scan.ui.scantree.TreeManipulator;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/** Handler to remove selected command from tree,
 *  putting it onto clipboard
 *  @author Kay Kasemir
 */
public class CutCommandHandler extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException
    {
        final ScanEditor editor = ScanEditor.getActiveEditor();
        if (editor == null)
            return null;

        // Remove command from scan
        final List<ScanCommand> commands = editor.getCommands();
        final ScanCommand command = editor.getSelectedCommand();
        TreeManipulator.remove(commands, command);
        editor.refresh();

        // Put command onto clipboard
        final String text = command.toXML();
        final Clipboard clip = new Clipboard(Display.getCurrent());
        clip.setContents(new Object[] { text }, new Transfer[] { TextTransfer.getInstance() });
        clip.dispose();

        return null;
    }
}
