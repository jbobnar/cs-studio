package org.csstudio.trends.databrowser2.editor;

import org.csstudio.trends.databrowser2.Activator;
import org.csstudio.trends.databrowser2.Messages;
import org.csstudio.trends.databrowser2.ui.ModelBasedPlot;
import org.csstudio.trends.databrowser2.ui.RemoveMarkersDialog;
import org.eclipse.jface.action.Action;

/**
 * <code>ShowRemoveMarkersDialog</code> displays dialog which allows removing
 * markers from the plot.
 * 
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class ShowRemoveMarkersDialog extends Action {

    private ModelBasedPlot plot;
    
    /**
     * Constructs action.
     * 
     * @param plot plot
     */
    public ShowRemoveMarkersDialog(ModelBasedPlot plot) {
        super(Messages.RemoveMarkers, Activator.getDefault()
                .getImageDescriptor("icons/remove_unused.gif"));
        this.plot = plot;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        new RemoveMarkersDialog(plot.getPlot().getShell(), plot.getPlot()).open();
    }
}