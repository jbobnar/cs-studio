package org.csstudio.trends.databrowser2;

import org.csstudio.trends.databrowser2.editor.DataBrowserEditor;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/** Handler connected to workbench menu for opening a new editor with waveform.
 *  @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class NewDataBrowserWithWaveformHandler extends AbstractHandler {
	
    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException
    {
        DataBrowserEditor.createInstanceWithWaveform();
        try
        {
        	Perspective.showPerspective();
        }
        catch (Exception ex)
        {
        	// never mind
        }
        return null;
    }
}
