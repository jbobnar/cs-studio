package org.csstudio.trends.databrowser2.editor;

import org.csstudio.trends.databrowser2.Activator;
import org.csstudio.trends.databrowser2.Messages;
import org.csstudio.trends.databrowser2.ui.ModelBasedPlot;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;

/**
 * <code>ShowWaveformSnapshotAction</code> displays the waveform snapshot viewer.
 * 
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class ShowWaveformSnapshotAction extends Action {

	private ModelBasedPlot plot;
	private Composite composite;
	
	/**
	 * Constructs action.
	 * 
	 * @param plot plot
	 * @param composite composite
	 */
	public ShowWaveformSnapshotAction(ModelBasedPlot plot, Composite composite) {
		super(composite.getVisible() ? Messages.HideSnapshotViewer : Messages.ShowSnapshotViewer,
	              Activator.getDefault().getImageDescriptor("icons/add.gif"));
		this.composite = composite;
		this.plot = plot;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		boolean visible = !composite.getVisible();
		plot.getPlot().showCrosshair(visible);
		composite.setVisible(visible);
		composite.getParent().layout();
	}
}