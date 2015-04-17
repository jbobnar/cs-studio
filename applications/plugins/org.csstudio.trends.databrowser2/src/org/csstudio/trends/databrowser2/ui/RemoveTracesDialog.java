package org.csstudio.trends.databrowser2.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.csstudio.swt.rtplot.RTValuePlot;
import org.csstudio.trends.databrowser2.Messages;
import org.csstudio.trends.databrowser2.model.AxisConfig;
import org.csstudio.trends.databrowser2.model.Model;
import org.csstudio.trends.databrowser2.model.ModelItem;
import org.csstudio.trends.databrowser2.propsheet.DeleteAxisCommand;
import org.csstudio.trends.databrowser2.propsheet.DeleteItemsCommand;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
* <class>RemoveTracesDialog</class> provides dialog which allows removing
* selected traces from the plot.
* 
* @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
*
*/
public class RemoveTracesDialog extends Dialog {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;
    
    private RTValuePlot plot;
    private Model model;
    private CheckboxTableViewer traceTable;
    
    /**
     * Constructs remove traces dialog.
     * 
     * @param plot plot
     * @param model model
     */
    public RemoveTracesDialog(RTValuePlot plot, Model model) {
        super(plot.getShell());
        this.plot = plot;
        this.model = model;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.RemoveTraces);
        shell.setSize(WIDTH, HEIGHT);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        traceTable = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.MULTI);
        final Table table = traceTable.getTable();
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        traceTable.setContentProvider(new ArrayContentProvider());
        traceTable.setInput(model.getItems());
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {        
        removeSelectedTraces();
        removeUnusedAxis();
        super.okPressed();
    }
    
    /**
     * Removes selected traces.
     */
    private void removeSelectedTraces() {
        List<ModelItem> items = new ArrayList<ModelItem>();
        int N = traceTable.getTable().getItemCount();
        for (int i = 0; i < N ; i++) {
            ModelItem item = (ModelItem) traceTable.getElementAt(i);
            if (traceTable.getChecked(item)) {
                items.add(item);
            }
        }
        new DeleteItemsCommand(traceTable.getTable().getShell(),
                plot.getUndoableActionManager(), model, items.toArray(new ModelItem[items.size()]));
    }
    
    /**
     * Removes unused axis.
     */
    private void removeUnusedAxis() {
        Optional<AxisConfig> axis = model.getEmptyAxis();
        while (axis.isPresent())
        {
            new DeleteAxisCommand(plot.getUndoableActionManager(), model, axis.get());
            axis = model.getEmptyAxis();
        }        
    }
}
