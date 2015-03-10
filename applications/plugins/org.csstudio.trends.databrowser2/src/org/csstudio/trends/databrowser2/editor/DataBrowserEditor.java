/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.trends.databrowser2.editor;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.csstudio.apputil.ui.workbench.OpenViewAction;
import org.csstudio.email.EMailSender;
import org.csstudio.swt.rtplot.PointType;
import org.csstudio.swt.rtplot.RTValuePlot;
import org.csstudio.swt.rtplot.Timestamp;
import org.csstudio.swt.rtplot.Trace;
import org.csstudio.swt.rtplot.TraceType;
import org.csstudio.swt.rtplot.data.PlotDataSearch;
import org.csstudio.swt.rtplot.undo.UndoableActionManager;
import org.csstudio.trends.databrowser2.Activator;
import org.csstudio.trends.databrowser2.Messages;
import org.csstudio.trends.databrowser2.Perspective;
import org.csstudio.trends.databrowser2.archive.ArchiveFetchJob;
import org.csstudio.trends.databrowser2.archive.ArchiveFetchJobListener;
import org.csstudio.trends.databrowser2.exportview.ExportView;
import org.csstudio.trends.databrowser2.imports.SampleImporters;
import org.csstudio.trends.databrowser2.model.ArchiveDataSource;
import org.csstudio.trends.databrowser2.model.AxisConfig;
import org.csstudio.trends.databrowser2.model.Model;
import org.csstudio.trends.databrowser2.model.ModelItem;
import org.csstudio.trends.databrowser2.model.ModelListener;
import org.csstudio.trends.databrowser2.model.ModelListenerAdapter;
import org.csstudio.trends.databrowser2.model.PVItem;
import org.csstudio.trends.databrowser2.model.PlotSample;
import org.csstudio.trends.databrowser2.model.PlotSamples;
import org.csstudio.trends.databrowser2.model.RequestType;
import org.csstudio.trends.databrowser2.persistence.XMLPersistence;
import org.csstudio.trends.databrowser2.preferences.Preferences;
import org.csstudio.trends.databrowser2.propsheet.DataBrowserPropertySheetPage;
import org.csstudio.trends.databrowser2.propsheet.RemoveUnusedAxesAction;
import org.csstudio.trends.databrowser2.sampleview.SampleView;
import org.csstudio.trends.databrowser2.search.SearchView;
import org.csstudio.trends.databrowser2.ui.AddPVAction;
import org.csstudio.trends.databrowser2.ui.Controller;
import org.csstudio.trends.databrowser2.ui.ModelBasedPlot;
import org.csstudio.trends.databrowser2.ui.RefreshAction;
import org.csstudio.trends.databrowser2.waveformview.WaveformValueDataProvider;
import org.csstudio.trends.databrowser2.waveformview.WaveformView;
import org.csstudio.ui.util.EmptyEditorInput;
import org.csstudio.ui.util.dialogs.ExceptionDetailsErrorDialog;
import org.csstudio.ui.util.perspective.OpenPerspectiveAction;
import org.csstudio.utility.singlesource.PathEditorInput;
import org.csstudio.utility.singlesource.ResourceHelper;
import org.csstudio.utility.singlesource.SingleSourcePlugin;
import org.csstudio.utility.singlesource.UIHelper.UI;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;

/** Eclipse 'editor' for the Data Browser
 *  <p>
 *  plugin.xml registers this as an editor for data browser configuration
 *  files.
 *  @author Kay Kasemir
 *  @author Xihui Chen (Adjustment to make it work like a view in RAP)
 *  @author Naceur Benhadj (add property to hide "Property" view)
 *  @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a> (added fast waveform plot) 
 */
@SuppressWarnings("nls")
public class DataBrowserEditor extends EditorPart
{
    /** Editor ID (same ID as original Data Browser) registered in plugin.xml */
    final public static String ID = "org.csstudio.trends.databrowser.ploteditor.PlotEditor"; //$NON-NLS-1$
    
    /** True if show DataBrowserEditor with waveform */
    private static boolean showWaveform = false;

    /** Data model */
    private Model model;

    /** Listener to model that updates this editor*/
	private ModelListener model_listener;

    /** GUI for the plot */
    private ModelBasedPlot plot;

    /** Controller that links model and plot */
    private Controller controller = null;

    /** @see #isDirty() */
    private boolean is_dirty = false;
    
    /** Waveform plot */
    private RTValuePlot waveformPlot;
    
    /** Slider for selecting samples */
    private Slider sampleIndexSlider;
    
    /** Plot search */
    private PlotDataSearch<Instant> plotSearch;
    
    /** Executor service */
    private ExecutorService executorService;
    
    /** Waveform color */
    private RGB waveformColor = new RGB(212, 212, 111);
    
    /** Create data browser editor
     *  @param input Input for editor, must be data browser config file
     *  @return DataBrowserEditor or <code>null</code> on error
     */
    public static DataBrowserEditor createInstance(final IEditorInput input)
    {
        final DataBrowserEditor editor;
        try
        {
        	final IWorkbench workbench = PlatformUI.getWorkbench();
        	final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        	final IWorkbenchPage page = window.getActivePage();
        	showWaveform = false;
            editor = (DataBrowserEditor) page.openEditor(input, ID);
        }
        catch (Exception ex)
        {
            Activator.getLogger().log(Level.SEVERE, "Cannot create DataBrowserEditor", ex); //$NON-NLS-1$
            return null;
        }
        return editor;
    }

    /** Create an empty data browser editor
     *  @return DataBrowserEditor or <code>null</code> on error
     */
    public static DataBrowserEditor createInstance()
    {
        if (SingleSourcePlugin.isRAP())
        {
            if (Preferences.isDataBrowserSecured()
                    && !SingleSourcePlugin.getUIHelper().rapIsLoggedIn(
                            Display.getCurrent()))
            {
                if (!SingleSourcePlugin.getUIHelper().rapAuthenticate(
                        Display.getCurrent())) return null;
            }

        }

        return createInstance(new EmptyEditorInput()
        {
            @Override
            public String getName()
            {
                if (SingleSourcePlugin.isRAP()) return "Data Browser";
                return super.getName();
            }
        });
    }
    
    /** Create data browser editor with waveform.
     *  @param input Input for editor, must be data browser config file
     *  @return DataBrowserEditor or <code>null</code> on error
     */
    public static DataBrowserEditor createInstanceWithWaveform(final IEditorInput input)
    {
        final DataBrowserEditor editor;
        try
        {
        	final IWorkbench workbench = PlatformUI.getWorkbench();
        	final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        	final IWorkbenchPage page = window.getActivePage();
        	showWaveform = true;
            editor = (DataBrowserEditor) page.openEditor(input, ID);
        }
        catch (Exception ex)
        {
            Activator.getLogger().log(Level.SEVERE, "Cannot create DataBrowserEditor", ex); //$NON-NLS-1$
            return null;
        }
        return editor;
    }
    
    /** Create an empty data browser editor with waveform.
     *  @return DataBrowserEditor or <code>null</code> on error
     */
    public static DataBrowserEditor createInstanceWithWaveform()
    {
        if (SingleSourcePlugin.isRAP())
        {
            if (Preferences.isDataBrowserSecured()
                    && !SingleSourcePlugin.getUIHelper().rapIsLoggedIn(
                            Display.getCurrent()))
            {
                if (!SingleSourcePlugin.getUIHelper().rapAuthenticate(
                        Display.getCurrent())) return null;
            }

        }

        return createInstanceWithWaveform(new EmptyEditorInput()
        {
            @Override
            public String getName()
            {
                if (SingleSourcePlugin.isRAP()) return "Data Browser";
                return super.getName();
            }
        });
    }

    /** @return Model displayed/edited by this EditorPart */
    public Model getModel()
    {
        return model;
    }

    /** Initialize model from editor input
     *  {@inheritDoc}
     */
    @Override
    public void init(final IEditorSite site, final IEditorInput input)
            throws PartInitException
    {
        setSite(site);

        if (input instanceof DataBrowserModelEditorInput)
        {   // Received model with input
        	model = ((DataBrowserModelEditorInput)input).getModel();
        	setInput(input);
        }
        else
        {   // Create new model
	        model = new Model();
	        setInput(new DataBrowserModelEditorInput(input, model));

			// Load model content from file
	        try
	        (
                final InputStream stream = SingleSourcePlugin.getResourceHelper().getInputStream(input);
            )
	        {
        		if (stream != null)
        		    new XMLPersistence().load(model, stream);
			}
	        catch (Exception ex)
	        {
				throw new PartInitException(NLS.bind(
						Messages.ConfigFileErrorFmt, input.getName()), ex);
			}
        }

        // Update the editor's name from "Data Browser" to title of model or file name
        // See DataBrowserModelEditorInput.getName()
        setPartName(getEditorInput().getName());

        model_listener = new ModelListenerAdapter()
        {
            @Override
            public void changedSaveChangesBehavior(final boolean save_changes)
            {
                is_dirty = save_changes;
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }

            @Override
            public void changedTitle()
            {
                setPartName(getEditorInput().getName());
            }

            @Override
            public void changedTiming()
            {   setDirty(true);   }

            @Override
            public void changedArchiveRescale()
            {   setDirty(true);   }

            @Override
            public void changedColorsOrFonts()
            {   setDirty(true);   }

            @Override
            public void changedTimerange()
            {   setDirty(true);   }

            @Override
            public void changeTimeAxisConfig()
            {   setDirty(true);   }

            @Override
            public void changedAxis(final Optional<AxisConfig> axis)
            {   setDirty(true);   }

            @Override
            public void itemAdded(final ModelItem item)
            {   setDirty(true);   }

            @Override
            public void itemRemoved(final ModelItem item)
            {   setDirty(true);   }

            @Override
            public void changedItemVisibility(final ModelItem item)
            {   setDirty(true);   }

            @Override
            public void changedItemLook(final ModelItem item)
            {   setDirty(true);   }

            @Override
            public void changedItemDataConfig(PVItem item)
            {   setDirty(true);   }

            @Override
            public void scrollEnabled(final boolean scroll_enabled)
            {   setDirty(true);   }

			@Override
			public void changedAnnotations()
			{   setDirty(true);   }
        };
        model.addListener(model_listener);
        
        plotSearch = new PlotDataSearch<Instant>();
    }

    /** Provide custom property sheet for this editor */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(final Class adapter)
    {
        if (adapter == IPropertySheetPage.class)
            return new DataBrowserPropertySheetPage(model, plot.getPlot().getUndoableActionManager());
        return super.getAdapter(adapter);
    }

    /** Create Plot GUI, connect to model via Controller
     *  {@inheritDoc}
     */
    @Override
    public void createPartControl(final Composite parent)
    {
    	if (showWaveform) {
    		createPartControlWithWaveform(parent);
    	} else {
    		createPartControlWithoutWaveform(parent);
    	}
        // Create and start controller
        controller = new Controller(parent.getShell(), model, plot);
        try
        {
            controller.start();
        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(parent.getShell(), Messages.Error, ex);
        }

        // Only the 'page' seems to know if a part is visible or not,
        // so use PartListener to update controller's redraw handling
        getSite().getPage().addPartListener(new IPartListener2()
        {
            private boolean isThisEditor(final IWorkbenchPartReference part)
            {
                return part.getPart(false) == DataBrowserEditor.this;
            }
            // Enable redraws...
            @Override
            public void partOpened(final IWorkbenchPartReference part)
            {
                if (isThisEditor(part))
                    controller.suppressRedraws(false);
            }
            @Override
            public void partVisible(final IWorkbenchPartReference part)
            {
                if (isThisEditor(part))
                    controller.suppressRedraws(false);
            }
            // Suppress redraws...
            @Override
            public void partHidden(final IWorkbenchPartReference part)
            {
                if (isThisEditor(part))
                    controller.suppressRedraws(true);
            }
            @Override
            public void partClosed(final IWorkbenchPartReference part)
            {
                if (isThisEditor(part))
                    controller.suppressRedraws(true);
            }
            // Ignore
            @Override
            public void partInputChanged(final IWorkbenchPartReference part) { /* NOP */ }
            @Override
            public void partDeactivated(final IWorkbenchPartReference part)  { /* NOP */ }
            @Override
            public void partBroughtToTop(final IWorkbenchPartReference part) { /* NOP */ }
            @Override
            public void partActivated(final IWorkbenchPartReference part)    { /* NOP */ }
        });

        createContextMenu(plot.getPlot().getPlotControl());
    }
    
    /**
     * Create plot GUI with waveform.
     * 
     * @param parent composite parent
     */
    private void createPartControlWithWaveform(final Composite parent) {
    	 //Create GUI elements
    	 parent.setLayout(new GridLayout(2, true));
         plot = new ModelBasedPlot(parent);
         plot.getPlot().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
         onDoubleClickCreateTimestamp(plot);
         
         waveformPlot = new RTValuePlot(parent);
         waveformPlot.setSmartTracePainting(true);

         waveformPlot.getXAxis().setName(Messages.WaveformIndex);
         waveformPlot.getYAxes().get(0).setName(Messages.WaveformAmplitude);
         waveformPlot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
         
         sampleIndexSlider = new Slider(parent, SWT.HORIZONTAL);
         sampleIndexSlider.setToolTipText(Messages.WaveformTimeSelector);
         sampleIndexSlider.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false, 1, 1));
         onSelectionShowWaveform(sampleIndexSlider);
         
         plot.getPlot().getPlotControl().addControlListener(new ControlListener() {
 			
 			@Override
 			public void controlResized(ControlEvent e) {
 				setSliderRange();
 			}
 			
 			@Override
 			public void controlMoved(ControlEvent e) {
 			}
         });
    }
    
    /**
     * Create plot GUI.
     * 
     * @param parent composite parent
     */
    private void createPartControlWithoutWaveform(final Composite parent) {
    	// Create GUI elements (Plot)
    	parent.setLayout(new FillLayout());
        plot = new ModelBasedPlot(parent);
    }

    /** Create context menu */
    private void createContextMenu(final Control parent)
    {
        final MenuManager mm = new MenuManager();
        mm.setRemoveAllWhenShown(true);
        final Menu menu = mm.createContextMenu(parent);
        parent.setMenu(menu);
        getSite().registerContextMenu(mm, null);
        mm.addMenuListener(this::fillContextMenu);
    }

    /** Dynamically fill context menu
     *  @param manager
     */
    private void fillContextMenu(final IMenuManager manager)
    {
        final Activator activator = Activator.getDefault();
        final Shell shell = getSite().getShell();
        final UndoableActionManager op_manager = plot.getPlot().getUndoableActionManager();
        manager.add(plot.getPlot().getToolbarAction());
        manager.add(new Separator());
        manager.add(new AddPVAction(op_manager, shell, model, false));
        manager.add(new AddPVAction(op_manager, shell, model, true));
		final boolean is_rcp = SingleSourcePlugin.getUIHelper().getUI() == UI.RCP;
        if (is_rcp)
        {
	        try
	        {
	            for (IAction imp : SampleImporters.createImportActions(op_manager, shell, model))
	                    manager.add(imp);
	        }
	        catch (Exception ex)
	        {
	            ExceptionDetailsErrorDialog.openError(shell, Messages.Error, ex);
	        }
        }
        manager.add(new RemoveUnusedAxesAction(op_manager, model));
        manager.add(new RefreshAction(controller));
        manager.add(new Separator());

        if (is_rcp  ||  ! Preferences.hidePropertiesView())
            manager.add(new OpenViewAction(
            			IPageLayout.ID_PROP_SHEET,
            			Messages.OpenPropertiesView,
            			activator.getImageDescriptor("icons/prop_ps.gif")));
        if (is_rcp  ||  ! Preferences.hideSearchView())
            manager.add(new OpenViewAction(SearchView.ID, Messages.OpenSearchView,
                    activator.getImageDescriptor("icons/search.gif")));
        if (is_rcp)
            manager.add(new OpenViewAction(ExportView.ID, Messages.OpenExportView,
                    activator.getImageDescriptor("icons/export.png")));
		manager.add(new OpenViewAction(SampleView.ID, Messages.InspectSamples,
				activator.getImageDescriptor("icons/inspect.gif")));

		manager.add(new OpenPerspectiveAction(activator
				.getImageDescriptor("icons/databrowser.png"),
				Messages.OpenDataBrowserPerspective, Perspective.ID));
		manager.add(new OpenViewAction(WaveformView.ID,
				Messages.OpenWaveformView, activator
						.getImageDescriptor("icons/wavesample.gif")));

		manager.add(new Separator());
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		if (is_rcp)
		{
			manager.add(new Separator());
			if (EMailSender.isEmailSupported())
				manager.add(new SendEMailAction(shell, plot.getPlot()));
			manager.add(new PrintAction(shell, plot.getPlot()));
			if (SendToElogAction.isElogAvailable())
			    manager.add(new SendToElogAction(shell, plot.getPlot()));
		}
    }

    /** {@inheritDoc} */
    @Override
    public void dispose()
    {
    	model.removeListener(model_listener);
        if (controller != null)
        {
            controller.stop();
            controller = null;
        }
        super.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public void setFocus()
    {
        plot.getPlot().setFocus();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDirty()
    {
        return is_dirty;
    }

    /** Update the 'dirty' flag
     *  @param dirty <code>true</code> if model changed and needs to be saved
     */
    protected void setDirty(final boolean dirty)
    {   // No 'save', never 'dirty' based on model or when running as RAP
        if (!model.shouldSaveChanges()  ||  SingleSourcePlugin.isRAP())
            return;
        is_dirty = dirty;
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSaveAsAllowed()
    {
        return ! SingleSourcePlugin.isRAP();
    }

    /** {@inheritDoc} */
    @Override
    public void doSave(final IProgressMonitor monitor)
    {
        try
        {
            final ResourceHelper resources = SingleSourcePlugin.getResourceHelper();
            if (! resources.isWritable(getEditorInput()))
                doSaveAs();
            else
            {
                try
                (
                    final OutputStream stream = resources.getOutputStream(getEditorInput());
                )
                {
                    save(monitor, stream);
                }
            }
        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(getSite().getShell(), Messages.Error, ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void doSaveAs()
    {
        final Shell shell = getSite().getShell();
        final ResourceHelper resources = SingleSourcePlugin.getResourceHelper();
        final IPath original = resources.getPath(getEditorInput());
        final IPath file = SingleSourcePlugin.getUIHelper()
            .openSaveDialog(shell, original, Model.FILE_EXTENSION);
        if (file == null)
            return;
        try
        {
            final PathEditorInput new_input = new PathEditorInput(file);
            try
            (
                final OutputStream stream = resources.getOutputStream(new_input);
            )
            {
                save(new NullProgressMonitor(), stream);
            }
            // Set that file as editor's input, so that just 'save' instead of
            // 'save as' is possible from now on
            final DataBrowserModelEditorInput db_input = new DataBrowserModelEditorInput(new_input, model);
            setInput(db_input);
            setPartName(db_input.getName());
            setTitleToolTip(db_input.getToolTipText());
        }
        catch (Exception ex)
        {
            ExceptionDetailsErrorDialog.openError(getSite().getShell(), Messages.Error, ex);
        }
    }

    /** Save current model content, mark editor as clean.
     *
     *  @param monitor <code>IProgressMonitor</code>, may be null.
     *  @param stream The stream to use.
     *  @return Returns <code>true</code> when successful.
     */
    private void save(final IProgressMonitor monitor, final OutputStream stream) throws Exception
    {
        monitor.beginTask(Messages.Save, IProgressMonitor.UNKNOWN);
        try
        {
            new XMLPersistence().write(model, stream);
        }
        finally
        {
            monitor.done();
        }
        setDirty(false);
    }
    
	/**
	 * Adds mouse listener to the model based plot. On double click creates
	 * timestamp on the plot.
	 * 
	 * @param plot model based plot
	 */
    private void onDoubleClickCreateTimestamp(ModelBasedPlot plot) {
    	plot.getPlot().getPlotControl().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (model.getItems().iterator().hasNext()) {
					Instant position = calculatePosition(plot.getPlot().getXAxis().getValue(e.x));
					Timestamp<Instant> timestamp = new Timestamp<Instant>(position);
					if (plot.getPlot().getTimestamps().isEmpty()) {
						plot.getPlot().addTimestamp(timestamp);
					} else {
						plot.getPlot().updateTimestamp(plot.getPlot().getTimestamps().get(0), position);
					}
					getExecutorService().execute(() -> retrieveSample(position));
					setSliderRange();
					sampleIndexSlider.setSelection(plot.getPlot().getXAxis().getScreenCoord(position));
				}
			}
		});
    }
    
    /**
     * Calculates timestamp position.
     * 
     * @param x current timestamp
     * 
     * @return timestamp position.
     */
	private Instant calculatePosition(Instant x) {
		PlotSample less = null;
		PlotSample greater = null;
		for (ModelItem modelItem : model.getItems()) {
			PlotSamples samples = modelItem.getSamples();
			int lessIndex = plotSearch.findSampleLessOrEqual(samples, x);
			int greaterIndex = plotSearch.findSampleGreaterOrEqual(samples, x);
			if (lessIndex != -1) {
				less = getBetterSample(less, samples.get(lessIndex), x);
			}
			if (greaterIndex != -1) {
				greater = getBetterSample(greater, samples.get(greaterIndex), x);
			}
		}
		if (less == null && greater == null) {
			return x;
		} else if (less == null && greater != null) {
			return greater.getPosition();
		} else if (less != null && greater == null) {
			return less.getPosition();
		} else {
			long diff1 = less.getPosition().toEpochMilli() - x.toEpochMilli();
			long diff2 = greater.getPosition().toEpochMilli() - x.toEpochMilli();
			return diff1 < diff2 ? less.getPosition() : greater.getPosition();
		}
	}
	
	/**
	 * Returns sample which is closer to the current timestamp.
	 * 
	 * @param sample1 sample1
	 * @param sample2 sample2
	 * @param x timestamp
	 * 
	 * @return sample which is closer to the current timestamp.
	 */
	private PlotSample getBetterSample(PlotSample sample1, PlotSample sample2, Instant x) {
		if (sample1 == null) {
			return sample2;
		} else {
			long diff1 = sample1.getPosition().toEpochMilli() - x.toEpochMilli();
			long diff2 = sample2.getPosition().toEpochMilli() - x.toEpochMilli();
			if (diff1 > diff2) {
				return sample2;
			}
		}
		return sample1;
	}
    
	/**
	 * Adds selection listner to the slider.
	 * 
	 * @param slider slider
	 */
    private void onSelectionShowWaveform(Slider slider) {
        slider.addSelectionListener(new SelectionAdapter() {
        	
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!plot.getPlot().getTimestamps().isEmpty()) {
            		Instant x = plot.getPlot().getXAxis().getValue(slider.getSelection());
                	PlotSample item = null;
                	for (ModelItem modelItem : model.getItems()) {
                		PlotSamples samples = modelItem.getSamples();
                		int index = plotSearch.findSampleLessOrEqual(samples, x);
                		if (index != -1) {
                			item = getBetterSample(item, samples.get(index), x);
                		}
                	}
                	if (item != null) {
                		final Instant position = item.getPosition();
                		plot.getPlot().updateTimestamp(plot.getPlot().getTimestamps().get(0), position);
                		getExecutorService().execute(() -> retrieveSample(position));
                	}
                }
            }
        });
    }
    
    /**
     * Sets slider range.
     */
    private void setSliderRange() {
		Instant min = plot.getPlot().getXAxis().getValueRange().getLow();
		Instant max = plot.getPlot().getXAxis().getValueRange().getHigh();
		int x1 = plot.getPlot().getXAxis().getScreenCoord(min);
		int x2 = plot.getPlot().getXAxis().getScreenCoord(max);
		sampleIndexSlider.setMinimum(x1);
		sampleIndexSlider.setMaximum(x2);
		sampleIndexSlider.setPageIncrement(1);
    }
    
    /** 
     * Retrieves sample with waveform and shows it in waveform plot.
     * 
     * @param timestamp timestamp
     */
    private void retrieveSample(Instant timestamp) {
    	PVItem pv = null;
		try {
			pv = new PVItem(Preferences.getFastWaveformPVName(), 0.0);
	    	pv.setRequestType(RequestType.RAW);
	    	pv.useDefaultArchiveDataSources();
		} catch (Exception ex) {
            Activator.getLogger().log(Level.SEVERE, "Cannot create fast waveform PV item", ex); //$NON-NLS-1$
            return;
		}
		
    	ArchiveFetchJob fetchJob = new ArchiveFetchJob(pv, timestamp, timestamp, new ArchiveFetchJobListener() {
			
			@Override
			public void fetchCompleted(ArchiveFetchJob job) {
				PVItem item = job.getPVItem();
				if (item.getSamples().size() > 0) {
					PlotSample sample = item.getSamples().get(0);
					WaveformValueDataProvider dataProvider = new WaveformValueDataProvider();
					dataProvider.setValue(sample.getVType());
        		
					// remove waveform plot traces
					for (Trace<Double> trace : waveformPlot.getTraces()) {
						waveformPlot.removeTrace(trace);
					}
        		
					waveformPlot.addTrace("Waveform: " + Preferences.getFastWaveformPVName(), dataProvider, waveformColor, TraceType.NONE, 1, PointType.CIRCLES, 5, 0);
					waveformPlot.getXAxis().setValueRange(0.0, (double) dataProvider.size());
					waveformPlot.stagger();
					waveformPlot.requestUpdate();
				}
			}
			
			@Override
			public void archiveFetchFailed(ArchiveFetchJob job, ArchiveDataSource archive, Exception error) {
			}
		});
    	
    	fetchJob.schedule();
    }
    
    /**
     * @return created executor.
     */
	private ExecutorService getExecutorService() {
		if (executorService == null) {
			executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.NANOSECONDS, new DismissableBlockingQueue<Runnable>(2));
		}
		return executorService;
	}
}