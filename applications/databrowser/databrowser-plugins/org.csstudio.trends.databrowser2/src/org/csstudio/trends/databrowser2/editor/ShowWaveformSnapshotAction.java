package org.csstudio.trends.databrowser2.editor;

import java.time.Instant;
import java.util.List;

import org.csstudio.swt.rtplot.Marker;
import org.csstudio.swt.rtplot.RTPlot;
import org.csstudio.trends.databrowser2.Activator;
import org.csstudio.trends.databrowser2.Messages;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;

/**
 * <code>ShowWaveformSnapshotAction</code> displays the waveform snapshot viewer.
 * 
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class ShowWaveformSnapshotAction extends Action {

	@SuppressWarnings("rawtypes")
    private RTPlot plot;
	private Composite composite;
	
	/**
	 * Constructs action.
	 * 
	 * @param plot plot
	 * @param composite composite
	 */
	@SuppressWarnings("rawtypes")
	public ShowWaveformSnapshotAction(RTPlot plot, Composite composite) {
		super(composite.getVisible() ? Messages.HideSnapshotViewer : Messages.ShowSnapshotViewer,
	              composite.getVisible() ? Activator.getDefault().getImageDescriptor("icons/up.gif") : 
	                  Activator.getDefault().getImageDescriptor("icons/down.gif"));
		this.composite = composite;
		this.plot = plot;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
    @SuppressWarnings("unchecked")
	@Override
	public void run() {
		boolean visible = !composite.getVisible();
		if (!visible) {
		    // remove markers if waveform snapshot viewer is not visible
            List<Marker<Instant>> markers = plot.getMarkers();
		    for (Marker<Instant> marker : markers) {
		        plot.removeMarker(marker);
		    }
		}
		plot.showCrosshair(visible);
		composite.setVisible(visible);
		composite.getParent().layout();
	}
}