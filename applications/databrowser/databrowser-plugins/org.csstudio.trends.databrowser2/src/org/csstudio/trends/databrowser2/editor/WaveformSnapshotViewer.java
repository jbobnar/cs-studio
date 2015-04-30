package org.csstudio.trends.databrowser2.editor;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.csstudio.archive.vtype.ArchiveVNumberArray;
import org.csstudio.csdata.ProcessVariable;
import org.csstudio.swt.rtplot.RTPlot;
import org.csstudio.swt.rtplot.RTValuePlot;
import org.csstudio.swt.rtplot.Trace;
import org.csstudio.swt.rtplot.YAxis;
import org.csstudio.swt.rtplot.data.PlotDataProvider;
import org.csstudio.swt.rtplot.undo.UndoableActionManager;
import org.csstudio.trends.databrowser2.Messages;
import org.csstudio.trends.databrowser2.archive.ArchiveFetchJob;
import org.csstudio.trends.databrowser2.archive.ArchiveFetchJobListener;
import org.csstudio.trends.databrowser2.imports.FileImportDialog;
import org.csstudio.trends.databrowser2.imports.ImportArchiveReaderFactory;
import org.csstudio.trends.databrowser2.model.ArchiveDataSource;
import org.csstudio.trends.databrowser2.model.AxisConfig;
import org.csstudio.trends.databrowser2.model.ChannelInfo;
import org.csstudio.trends.databrowser2.model.Model;
import org.csstudio.trends.databrowser2.model.ModelItem;
import org.csstudio.trends.databrowser2.model.ModelListener;
import org.csstudio.trends.databrowser2.model.ModelListenerAdapter;
import org.csstudio.trends.databrowser2.model.PVItem;
import org.csstudio.trends.databrowser2.preferences.Preferences;
import org.csstudio.trends.databrowser2.propsheet.AddArchiveCommand;
import org.csstudio.trends.databrowser2.propsheet.AddAxisCommand;
import org.csstudio.trends.databrowser2.ui.AddModelItemCommand;
import org.csstudio.trends.databrowser2.ui.AddPVAction;
import org.csstudio.trends.databrowser2.waveformview.WaveformValueDataProvider;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.csstudio.ui.util.dnd.ControlSystemDropTarget;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.epics.vtype.VType;

/**
 * <code>WaveformSnapshotViewer</code> is a viewer for waveform types, which is capable of showing waveform samples. The
 * viewer displays a single waveform value at a time, which is fetched from the archiving service on request. Every time
 * when a new timestamp is set the viewer will fetch the waveform data for the selected timestamp.
 * 
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class WaveformSnapshotViewer {

    private final Map<ModelItem, List<ArchiveFetchJob>> itemJobMap = new HashMap<>();
    private final Map<ModelItem, Trace<Double>> itemTraceMap = new HashMap<>();

    private final Model plotModel;
    private final RTValuePlot plot;
    private final RTPlot<Instant> parentPlot;

    private final Shell shell;
    private Instant currentTimestamp;

    private Composite composite;

    /**
     * Constructs a new viewer.
     * 
     * @param parent parent
     */
    public WaveformSnapshotViewer(Composite parent, RTPlot<Instant> parentPlot) {
        composite = parent;
        shell = parent.getShell();
        plotModel = new Model();
        plotModel.addListener(createPlotModelListener());
        
        this.parentPlot = parentPlot;

        plot = new RTValuePlot(parent);

        plot.getXAxis().setName(Messages.WaveformIndex);
        plot.getYAxes().get(0).setName(Messages.WaveformAmplitude);
        plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        createWaveformSnapshotContextMenu(plot.getPlotControl());

        hookDragAndDrop(plot);
    }

    /**
     * Retrieves sample with waveform and shows it in waveform plot.
     * 
     * @param timestamp timestamp
     */
    public void retrieveSample(Instant timestamp) {
        currentTimestamp = timestamp;
        for (ModelItem item : plotModel.getItems()) {
            retrieveSample(item, timestamp);
        }
    }

    /**
     * @return waveform plot.
     */
    public RTValuePlot getPlot() {
        return plot;
    }

    /**
     * Attach to drag-and-drop, notifying the plot listener
     *
     * @param parent parent
     */
    private void hookDragAndDrop(final Composite parent) {
        // Allow dropped arrays
        new ControlSystemDropTarget(parent, ChannelInfo[].class, ProcessVariable[].class, ArchiveDataSource[].class,
                File.class, String.class) {
            @Override
            public void handleDrop(final Object item) {
                if (item instanceof ChannelInfo[]) {
                    final ChannelInfo[] channels = (ChannelInfo[]) item;
                    for (ChannelInfo channel : channels)
                        droppedPVName(channel.getProcessVariable(), channel.getArchiveDataSource());
                } else if (item instanceof ProcessVariable[]) {
                    final ProcessVariable[] pvs = (ProcessVariable[]) item;
                    for (ProcessVariable pv : pvs)
                        droppedPVName(pv, null);
                } else if (item instanceof ArchiveDataSource[]) {
                    final ArchiveDataSource[] archives = (ArchiveDataSource[]) item;
                    for (ArchiveDataSource archive : archives)
                        droppedPVName(null, archive);
                } else if (item instanceof String)
                    droppedName(item.toString());
                else if (item instanceof String[]) { // File names arrive as
                                                     // String[]...
                    final String[] files = (String[]) item;
                    try {
                        for (String filename : files)
                            droppedFilename(filename);
                    } catch (Exception ex) {
                        ExceptionDetailsErrorDialog.openError(parent.getShell(), Messages.Error, ex);
                    }
                }
            }
        };
    }

    /**
     * Received a PV name and/or archive data source via drag & drop
     * 
     * @param name PV name or <code>null</code>
     * @param archive Archive data source or <code>null</code>
     */
    private void droppedPVName(final ProcessVariable name, final ArchiveDataSource archive) {
        if (name == null) {
            if (archive == null)
                return;
            // Received only an archive. Add to all PVs
            for (ModelItem item : plotModel.getItems()) {
                if (!(item instanceof PVItem))
                    continue;
                final PVItem pv = (PVItem) item;
                if (pv.hasArchiveDataSource(archive))
                    continue;
                new AddArchiveCommand(plot.getUndoableActionManager(), pv, archive);
            }
        } else { // Received PV name
                 // Add the given PV to the model 
            String pvname = name.getName();
            for (ModelItem item : plotModel.getItems()) {
                if (item.getName().equals(pvname)) {
                    return;
                }
            }
            final UndoableActionManager operations_manager = plot.getUndoableActionManager();

            // Add to first empty axis, or create new axis
            final AxisConfig axis = plotModel.getEmptyAxis().orElseGet(
                    () -> new AddAxisCommand(operations_manager, plotModel).getAxis());
            // Add new PV
            AddModelItemCommand.forPV(shell, operations_manager, plotModel, name.getName(),
                    Preferences.getScanPeriod(), axis, archive);
        }
    }

    /**
     * Received a name, presumably a PV name via drag & drop
     * 
     * @param name PV(?) name
     */
    private void droppedName(final String name) {
        // Offer potential PV name in dialog so user can edit/cancel
        final AddPVAction add = new AddPVAction(plot.getUndoableActionManager(), shell, plotModel, false);
        // Allow passing in many names, assuming that white space separates them
        final String[] names = name.split("[\\r\\n\\t ]+"); //$NON-NLS-1$
        for (String one_name : names) { // Might also have received
                                        // "[pv1, pv2, pv2]", turn that into
                                        // "pv1", "pv2", "pv3"
            String suggestion = one_name;
            if (suggestion.startsWith("["))
                suggestion = suggestion.substring(1);
            if (suggestion.endsWith("]") && !suggestion.contains("["))
                suggestion = suggestion.substring(0, suggestion.length() - 1);
            if (suggestion.endsWith(","))
                suggestion = suggestion.substring(0, suggestion.length() - 1);
            if (!add.runWithSuggestedName(suggestion, null))
                break;
        }
    }

    /** Received a file name */
    private void droppedFilename(String file_name) {
        final FileImportDialog dlg = new FileImportDialog(shell, file_name);
        if (dlg.open() != Window.OK)
            return;

        final UndoableActionManager operations_manager = plot.getUndoableActionManager();

        // Add to first empty axis, or create new axis
        final AxisConfig axis = plotModel.getEmptyAxis().orElseGet(
                () -> new AddAxisCommand(operations_manager, plotModel).getAxis());

        // Add archivedatasource for "import:..." and let that load the file
        final String type = dlg.getType();
        file_name = dlg.getFileName();
        final String url = ImportArchiveReaderFactory.createURL(type, file_name);
        final ArchiveDataSource imported = new ArchiveDataSource(url, 1, type);
        // Add PV Item with data to model
        AddModelItemCommand.forPV(shell, operations_manager, plotModel, dlg.getItemName(), Preferences.getScanPeriod(),
                axis, imported);
    }

    /**
     * Updates axis.
     * 
     * @param index axis index
     * @param config axis configuration
     */
    private void updateAxis(final int index, final AxisConfig config) {
        final YAxis<Double> axis = getYAxis(index);
        axis.setName(config.getResolvedName());
        axis.useAxisName(config.isUsingAxisName());
        axis.useTraceNames(config.isUsingTraceNames());
        axis.setColor(config.getColor());
        axis.setLogarithmic(config.isLogScale());
        axis.setGridVisible(config.isGridVisible());
        axis.setAutoscale(config.isAutoScale());
        axis.setValueRange(config.getMin(), config.getMax());
        axis.setVisible(config.isVisible());
        axis.setOnRight(config.isOnRight());
    }

    /**
     * @param index axis index
     * 
     * @return y axis.
     */
    private YAxis<Double> getYAxis(final int index) {
        // Get Y Axis, creating new ones if needed
        int N = plot.getYAxes().size();
        while (N <= index) {
            plot.addYAxis(NLS.bind(Messages.Plot_ValueAxisNameFMT, N));
            N = plot.getYAxes().size();
        }
        return plot.getYAxes().get(index);
    }

    /**
     * Method which creates retrieval job for model item. When data are retrieved, data provider with waveform values is
     * created and shown on plot.
     * 
     * @param item model item
     * @param timestamp timestamp
     * 
     * @throws Exception exception
     */
    private void retrieveSample(final ModelItem item, Instant timestamp) {
        if (!(item instanceof PVItem)) {
            return;
        }
        PVItem pvItem = new PVItem(item.getResolvedName(), 0.0);
        pvItem.setArchiveDataSource(((PVItem) item).getArchiveDataSources());

        ArchiveFetchJob fetchJob = new ArchiveFetchJob(pvItem, timestamp, timestamp, new ArchiveFetchJobListener() {

            @Override
            public void fetchCompleted(ArchiveFetchJob job) {
                List<ArchiveFetchJob> fetchJobs = itemJobMap.get(item);
                synchronized (fetchJobs) {
                    fetchJobs.remove(job);
                    if (!fetchJobs.isEmpty()) {
                        fetchJobs.get(0).schedule();
                    }
                }

                Trace<Double> trace = itemTraceMap.get(item);
                VType data = job.getPVItem().getSamples().get(0).getVType();
                if (trace == null) {
                    WaveformValueDataProvider dataProvider = new WaveformValueDataProvider();
                    dataProvider.setValue(data);
                    trace = plot.addTrace(item.getResolvedDisplayName(), dataProvider, item.getColor(),
                            item.getTraceType(), item.getLineWidth(), item.getPointType(), item.getPointSize(),
                            item.getAxisIndex());
                    itemTraceMap.put(item, trace);
                    if (data instanceof ArchiveVNumberArray) {
                        plot.getXAxis()
                                .setValueRange(0., Double.valueOf(((ArchiveVNumberArray) data).getData().size()));
                    }
                } else {
                    PlotDataProvider<Double> dataProvider = trace.getData();
                    if (dataProvider instanceof WaveformValueDataProvider) {
                        boolean resize = dataProvider.size() == 0;
                        ((WaveformValueDataProvider) dataProvider).setValue(data);
                        if (resize && data instanceof ArchiveVNumberArray) {
                            plot.getXAxis().setValueRange(0.,
                                    Double.valueOf(((ArchiveVNumberArray) data).getData().size()));

                        }
                    }
                }

                plot.requestUpdate();
            }

            @Override
            public void archiveFetchFailed(ArchiveFetchJob job, ArchiveDataSource archive, Exception error) {
                List<ArchiveFetchJob> fetchJobs = itemJobMap.get(item);
                synchronized (fetchJobs) {
                    fetchJobs.remove(job);
                    if (!fetchJobs.isEmpty()) {
                        fetchJobs.get(0).schedule();
                    }
                }
            }

        });

        scheduleFetchJob(item, fetchJob);
    }

    /**
     * Schedule fetch job.
     * 
     * @param item item for which job is scheduled
     * @param job scheduled job
     */
    private void scheduleFetchJob(ModelItem item, ArchiveFetchJob job) {
        List<ArchiveFetchJob> fetchJobs = itemJobMap.get(item);
        synchronized (fetchJobs) {
            if (fetchJobs.isEmpty()) {
                fetchJobs.add(job);
                job.schedule();
            } else if (fetchJobs.size() == 1) {
                fetchJobs.add(job);
            } else {
                fetchJobs.remove(1);
                fetchJobs.add(job);
            }
        }
    }

    /**
     * @return created plot model listener.
     */
    private ModelListener createPlotModelListener() {
        return new ModelListenerAdapter() {
            @Override
            public void changedAxis(final Optional<AxisConfig> axis) {
                if (axis.isPresent()) { // Update specific axis
                    final AxisConfig the_axis = axis.get();
                    int i = 0;
                    for (AxisConfig axis_config : plotModel.getAxes()) {
                        if (axis_config == the_axis) {
                            updateAxis(i, the_axis);
                            return;
                        }
                        ++i;
                    }
                } else // New or removed axis: Recreate the whole plot
                {
                    int N = plot.getYAxes().size();
                    while (N > 1) {
                        plot.removeYAxis(--N);
                    }
                    int i = 0;
                    for (AxisConfig axisConfig : plotModel.getAxes()) {
                        updateAxis(i++, axisConfig);
                    }
                }
            }

            @Override
            public void itemAdded(final ModelItem item) {
                itemJobMap.put(item, new ArrayList<ArchiveFetchJob>(2));
                if (currentTimestamp != null && item instanceof PVItem) {
                    retrieveSample(item, currentTimestamp);
                } else {
                    WaveformValueDataProvider dataProvider = new WaveformValueDataProvider();
                    Trace<Double> trace = plot.addTrace(item.getResolvedDisplayName(), dataProvider, item.getColor(),
                            item.getTraceType(), item.getLineWidth(), item.getPointType(), item.getPointSize(),
                            item.getAxisIndex());
                    itemTraceMap.put(item, trace);
                }

            }

            @Override
            public void itemRemoved(ModelItem item) {
                final Trace<Double> trace = itemTraceMap.get(item);
                plot.removeTrace(trace);
                itemTraceMap.remove(trace);
            }
        };
    }

    /** Create context menu for waveform snapshot */
    private void createWaveformSnapshotContextMenu(final Control parent) {
        final MenuManager mm = new MenuManager();
        mm.setRemoveAllWhenShown(true);
        final Menu menu = mm.createContextMenu(parent);
        parent.setMenu(menu);
        mm.addMenuListener(this::fillSnapshotWaveformContextMenu);
    }

    /**
     * Dynamically fill context menu
     * 
     * @param manager
     */
    private void fillSnapshotWaveformContextMenu(final IMenuManager manager) {
        manager.add(new ShowRemoveTracesDialogAction(plot, plotModel));
        manager.add(new ShowWaveformSnapshotAction(parentPlot, composite));
    }
}
