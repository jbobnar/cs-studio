package org.csstudio.trends.databrowser2.editor;

import org.csstudio.swt.rtplot.RTValuePlot;
import org.csstudio.trends.databrowser2.Activator;
import org.csstudio.trends.databrowser2.Messages;
import org.csstudio.trends.databrowser2.model.Model;
import org.csstudio.trends.databrowser2.ui.RemoveTracesDialog;
import org.eclipse.jface.action.Action;

/**
 * <code>ShowRemoveTracesDialogAction</code> displays dialog which allows removing
 * traces from the plot.
 * 
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class ShowRemoveTracesDialogAction extends Action {
    
    private RTValuePlot plot;
    private Model model;

    /**
     * Constructs action.
     * 
     * @param plot plot
     * @param model model
     */
    public ShowRemoveTracesDialogAction(RTValuePlot plot, Model model) {
        super(Messages.RemoveTraces, Activator.getDefault().getImageDescriptor(
                "icons/remove_unused.gif"));
        this.plot = plot;
        this.model = model;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        new RemoveTracesDialog(plot, model).open();
    }
}