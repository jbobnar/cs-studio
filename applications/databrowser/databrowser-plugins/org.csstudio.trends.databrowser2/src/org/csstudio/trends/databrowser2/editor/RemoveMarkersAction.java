package org.csstudio.trends.databrowser2.editor;

import java.time.Instant;

import org.csstudio.swt.rtplot.Marker;
import org.csstudio.trends.databrowser2.Activator;
import org.csstudio.trends.databrowser2.Messages;
import org.csstudio.trends.databrowser2.ui.ModelBasedPlot;
import org.eclipse.jface.action.Action;

/**
 * <code>ShowRemoveMarkersDialog</code> displays dialog which allows removing markers from the plot.
 * 
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class RemoveMarkersAction extends Action {

    private ModelBasedPlot plot;

    /**
     * Constructs action.
     * 
     * @param plot plot
     */
    public RemoveMarkersAction(ModelBasedPlot plot) {
        super(Messages.RemoveMarkers, Activator.getDefault().getImageDescriptor("icons/remove_unused.gif"));
        this.plot = plot;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        for (Marker<Instant> m : plot.getPlot().getMarkers()) {
            plot.getPlot().removeMarker(m);
        }
    }
}
