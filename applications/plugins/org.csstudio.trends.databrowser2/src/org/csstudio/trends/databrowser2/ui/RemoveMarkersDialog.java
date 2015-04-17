package org.csstudio.trends.databrowser2.ui;

import java.time.Instant;

import org.csstudio.swt.rtplot.Marker;
import org.csstudio.swt.rtplot.RTTimePlot;
import org.csstudio.trends.databrowser2.Messages;
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
 * <class>RemoveMarkersDialog</class> provides dialog which allows removeing
 * selected markers from the plot.
 * 
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 *
 */
public class RemoveMarkersDialog extends Dialog{

    private RTTimePlot plot;
    private CheckboxTableViewer markerTable;
    
    /**
     * Constructs new remove markers dialog.
     * 
     * @param shell shell
     * @param plot plot
     */
    public RemoveMarkersDialog(Shell shell, RTTimePlot plot) {
        super(shell);
        this.plot = plot;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.RemoveMarkers);
        shell.setSize(300, 300);
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
        markerTable = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.MULTI);
        final Table table = markerTable.getTable();
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        markerTable.setContentProvider(new ArrayContentProvider());
        markerTable.setInput(plot.getMarkers());
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void okPressed() {
        int N = markerTable.getTable().getItemCount();
        for (int i = 0; i < N ; i++) {
            Marker<Instant> marker = (Marker<Instant>) markerTable.getElementAt(i);
            if (markerTable.getChecked(marker)) {
                plot.removeMarker(marker);
            }
        }
        super.okPressed();
    }
}