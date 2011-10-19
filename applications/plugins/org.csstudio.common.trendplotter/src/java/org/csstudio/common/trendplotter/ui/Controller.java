/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.common.trendplotter.ui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.csstudio.apputil.ui.dialog.ErrorDetailDialog;
import org.csstudio.common.trendplotter.Activator;
import org.csstudio.common.trendplotter.Messages;
import org.csstudio.common.trendplotter.archive.ArchiveFetchJob;
import org.csstudio.common.trendplotter.archive.ArchiveFetchJobListener;
import org.csstudio.common.trendplotter.model.ArchiveDataSource;
import org.csstudio.common.trendplotter.model.ArchiveRescale;
import org.csstudio.common.trendplotter.model.AxisConfig;
import org.csstudio.common.trendplotter.model.Model;
import org.csstudio.common.trendplotter.model.ModelItem;
import org.csstudio.common.trendplotter.model.ModelListener;
import org.csstudio.common.trendplotter.model.PVItem;
import org.csstudio.common.trendplotter.preferences.Preferences;
import org.csstudio.common.trendplotter.propsheet.AddArchiveCommand;
import org.csstudio.common.trendplotter.propsheet.AddAxisCommand;
import org.csstudio.csdata.ProcessVariable;
import org.csstudio.data.values.ITimestamp;
import org.csstudio.data.values.TimestampFactory;
import org.csstudio.swt.xygraph.undo.OperationsManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/** Controller that interfaces the {@link Model} with the {@link Plot}:
 *  <ul>
 *  <li>For each item in the Model, create a trace in the plot.
 *  <li>Perform scrolling of the time axis.
 *  <li>When the plot is interactively zoomed, update the Model's time range.
 *  <li>Get archived data whenever the time axis changes.
 *  </ul>
 *  @author Kay Kasemir
 */
public class Controller implements ArchiveFetchJobListener
{
    /** Optional shell used to track shell state */
    final private Shell shell;

    /** Display used for dialog boxes etc. */
    final private Display display;

    /** Model with data to display */
    final Model model;

    /** GUI for displaying the data */
    final Plot plot;

    /** Timer that triggers scrolling or trace redraws */
    final Timer update_timer = new Timer("Update Timer", true); //$NON-NLS-1$

    /** Task executed by update_timer */
    private TimerTask update_task = null;

    /** Was scrolling off, i.e. we have not scrolled for a while? */
    private boolean scrolling_was_off = true;

    /** Delay to avoid flurry of archive requests
     *  @see #scheduleArchiveRetrieval(ITimestamp, ITimestamp)
     */
    final private long archive_fetch_delay = Preferences.getArchiveFetchDelay();

    /** Delayed task to avoid flurry of archive requests
     *  @see #scheduleArchiveRetrieval(ITimestamp, ITimestamp)
     */
    private TimerTask archive_fetch_delay_task = null;

    /** Currently active archive jobs, used to prevent multiple requests
     *  for the same model item.
     */
    final private ArrayList<ArchiveFetchJob> archive_fetch_jobs =
        new ArrayList<ArchiveFetchJob>();

    /** Is the window (shell) iconized? */
    private volatile boolean window_is_iconized = false;

    /** Should we perform redraws, or is the window hidden and we should suppress them? */
    private boolean suppress_redraws = false;

    /** Is there any Y axis that's auto-scaled? */
    private volatile boolean have_autoscale_axis = false;

    /** Initialize
     *  @param shell Shell
     *  @param model Model that has the data
     *  @param plot Plot for displaying the Model
     *  @throws Error when called from non-UI thread
     */
    public Controller(final Shell shell, final Model model, final Plot plot)
    {
        this.shell = shell;
        this.model = model;
        this.plot = plot;

        if (shell == null)
        {
            display = Display.getCurrent();
            if (display == null)
             {
                throw new Error("Must be called from UI thread"); //$NON-NLS-1$
            }
        }
        else
        {
            display = shell.getDisplay();
            // Update 'iconized' state from shell
            shell.addShellListener(new ShellListener()
            {
                @Override
                public void shellIconified(final ShellEvent e)
                {
                    window_is_iconized = true;
                }

                @Override
                public void shellDeiconified(final ShellEvent e)
                {
                    window_is_iconized = false;
                }

                @Override
                public void shellDeactivated(final ShellEvent e) { /* Ignore */  }
                @Override
                public void shellClosed(final ShellEvent e)      { /* Ignore */  }
                @Override
                public void shellActivated(final ShellEvent e)   { /* Ignore */  }
            });
            window_is_iconized = shell.getMinimized();
        }
        checkAutoscaleAxes();
        createPlotTraces();

        // Listen to user input from Plot UI, update model
        plot.addListener(new PlotListener()
        {
            @Override
            public void scrollRequested(final boolean enable_scrolling)
            {
                model.enableScrolling(enable_scrolling);
            }

            @Override
            public void timeConfigRequested()
            {
                StartEndTimeAction.run(shell, model, plot.getOperationsManager());
            }

            @Override
            public void timeAxisChanged(final long start_ms, final long end_ms)
            {
                if (model.isScrollEnabled())
                {
                    final long dist = Math.abs(end_ms - System.currentTimeMillis());
                    final long range = end_ms - start_ms;
                    // Iffy range?
                    if (range <= 0) {
                        return;
                    }
                    // In scroll mode, if the end time selected by the user via
                    // the GUI is close enough to 'now', scrolling remains 'on'
                    // and we'll continue to scroll with the new time range.
                    if (dist * 100 / range > 10)
                    {   // Time range 10% away from 'now', disable scrolling
                        model.enableScrolling(false);
                    }
                    else if (Math.abs(100*(range - (long)(model.getTimespan()*1000))/range) <= 1)
                    {
                        // We're still scrolling, and the time span didn't really
                        // change, i.e. it's within 1% of the model's span: Ignore.
                        // This happens when scrolling moved the time axis around,
                        // the user zoomed vertically, and the plot now tells
                        // us about a new time range that resulted from scrolling.
                        return;
                    }
                }
                final ITimestamp start_time = TimestampFactory.fromMillisecs(start_ms);
                final ITimestamp end_time = TimestampFactory.fromMillisecs(end_ms);
                // Update model's time range
                model.setTimerange(start_time, end_time);
                // Controller's ModelListener will fetch new archived data
            }

            @Override
            public void valueAxisChanged(final int index, final double lower, final double upper)
            {   // Update axis range in model
                final AxisConfig axis = model.getAxis(index);
                axis.setRange(lower, upper);
            }

            @Override
            public void droppedName(final String name)
            {
                // Offer potential PV name in dialog so user can edit/cancel
                final AddPVAction add = new AddPVAction(plot.getOperationsManager(), shell, model, false);
                // Allow passing in many names, assuming that white space separates them
                final String[] names = name.split("[\\r\\n\\t ]+"); //$NON-NLS-1$
                for (final String n : names) {
                    if (! add.runWithSuggestedName(n, null)) {
                        break;
                    }
                }
            }

            @Override
            public void droppedPVName(final ProcessVariable name, final ArchiveDataSource archive)
            {
                if (name == null)
                {
                    if (archive == null) {
                        return;
                    }
                    // Received only an archive. Add to all PVs
                    for (int i=0; i<model.getItemCount(); ++i)
                    {
                        if (! (model.getItem(i) instanceof PVItem)) {
                            continue;
                        }
                        final PVItem pv = (PVItem) model.getItem(i);
                        if (pv.hasArchiveDataSource(archive)) {
                            continue;
                        }
                        new AddArchiveCommand(plot.getOperationsManager(), pv, archive);
                    }
                }
                else
                {   // Received PV name
                    final ModelItem item = model.getItem(name.getName());
                    if (item == null)
                    {
                        final OperationsManager operations_manager = plot.getOperationsManager();

                        // Add to first empty axis, or create new axis
                        AxisConfig axis = model.getEmptyAxis();
                        if (axis == null) {
                            axis = new AddAxisCommand(operations_manager, model).getAxis();
                        }

                        // Add new PV
                        AddModelItemCommand.forPV(shell, operations_manager,
                                model, name.getName(), Preferences.getScanPeriod(),
                                axis, archive);
                        return;
                    }
                    if (archive == null  ||   ! (item instanceof PVItem))
                    {   // Duplicate PV, or a formula to which we cannot add archives
                        MessageDialog.openError(shell, Messages.Error,
                                NLS.bind(Messages.DuplicateItemFmt, name));
                        return;
                    }
                    // Add archive to existing PV
                    if (item instanceof PVItem) {
                        new AddArchiveCommand(plot.getOperationsManager(),
                                (PVItem) item, archive);
                    }
                }
            }
        });

        // Listen to Model changes, update Plot
        model.addListener(new ModelListener()
        {
            @Override
            public void changedUpdatePeriod()
            {
                if (update_task != null) {
                    createUpdateTask();
                }
            }

            @Override
            public void changedArchiveRescale()
            {
                // NOP
            }

            @Override
            public void changedColors()
            {
                plot.setBackgroundColor(model.getPlotBackground());
            }

            @Override
            public void changedTimerange()
            {
                // Get matching archived data
                scheduleArchiveRetrieval();
                // Show new time range on plot?
                if (model.isScrollEnabled())
                 {
                    return; // no, scrolling will handle that
                }
                // Yes, since the time axis is currently 'fixed'
                final long start_ms = (long) (model.getStartTime().toDouble()*1000);
                final long end_ms = (long) (model.getEndTime().toDouble()*1000);
                plot.setTimeRange(start_ms, end_ms);
            }

            @Override
            public void changedAxis(final AxisConfig axis)
            {
                checkAutoscaleAxes();
                if (axis == null)
                {
                    // New or removed axis: Recreate the whole plot
                    createPlotTraces();
                    return;
                }
                // Else: Update specific axis
                for (int i=0; i<model.getAxisCount(); ++i)
                {
                    if (model.getAxis(i) == axis)
                    {
                        plot.updateAxis(i, axis);
                        return;
                    }
                }
            }

            @Override
            public void itemAdded(final ModelItem item)
            {
                if (item.isVisible()) {
                    plot.addTrace(item);
                }
                // Get archived data for new item (NOP for non-PVs)
                getArchivedData(item, model.getStartTime(), model.getEndTime());
            }

            @Override
            public void itemRemoved(final ModelItem item)
            {
                if (item.isVisible()) {
                    plot.removeTrace(item);
                }
            }

            @Override
            public void changedItemVisibility(final ModelItem item)
            {   // Add/remove from plot, but don't need to get archived data
                if (item.isVisible()) {
                    // itemAdded(item) would also get archived data
                    plot.addTrace(item);
                } else {
                    plot.removeTrace(item);
                }
            }

            @Override
            public void changedItemLook(final ModelItem item)
            {
                plot.updateTrace(item);
            }

            @Override
            public void changedItemDataConfig(final PVItem item)
            {
                getArchivedData(item, model.getStartTime(), model.getEndTime());
            }

            @Override
            public void scrollEnabled(final boolean scroll_enabled)
            {
                plot.updateScrollButton(scroll_enabled);
            }
        });
    }

    /** @param suppress_redraws <code>true</code> if controller should suppress
     *        redraws because window is hidden
     */
    public void suppressRedraws(final boolean suppress_redraws)
    {
        if (this.suppress_redraws == suppress_redraws) {
            return;
        }
        this.suppress_redraws = suppress_redraws;
        if (!suppress_redraws) {
            plot.redrawTraces();
        }
    }

    /** Check if there's any axis in 'auto scale' mode.
     *  @see #have_autoscale_axis
     */
    private void checkAutoscaleAxes()
    {
        have_autoscale_axis = false;
        for (int i=0;  i<model.getAxisCount(); ++i) {
            if (model.getAxis(i).isAutoScale())
            {
                have_autoscale_axis = true;
                break;
            }
        }
    }

    /** When the user moves the time axis around, archive requests for the
     *  new time range are delayed to avoid a flurry of archive
     *  requests while the user is still moving around:
     */
    protected void scheduleArchiveRetrieval()
    {
        if (archive_fetch_delay_task != null) {
            archive_fetch_delay_task.cancel();
        }
        archive_fetch_delay_task = new TimerTask()
        {
            @Override
            public void run()
            {
                getArchivedData();
            }
        };
        update_timer.schedule(archive_fetch_delay_task, archive_fetch_delay);
    }

    /** Start model items and initiate scrolling/updates
     *  @throws Exception on error: Already running, problem starting threads, ...
     *  @see #isRunning()
     */
    public void start() throws Exception
    {
        if (isRunning())
         {
            throw new IllegalStateException("Already started"); //$NON-NLS-1$
        }
        createUpdateTask();
        model.start();

        // In scroll mode, the first scroll will update the plot and get data
        if (model.isScrollEnabled()) {
            return;
        }
        // In non-scroll mode, initialize plot's time range and get data
        plot.setTimeRange(model.getStartTime(), model.getEndTime());
        getArchivedData();
    }

    /** @return <code>true</code> while running
     *  @see #stop()
     */
    public boolean isRunning()
    {
        return update_task != null;
    }

    /** Create or re-schedule update task
     *  @see #start()
     */
    private void createUpdateTask()
    {
        // Can't actually re-schedule, so stop one that might already be running
        if (update_task != null)
        {
            update_task.cancel();
            update_task = null;
        }
        update_task = new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    // Skip updates while nobody is watching
                    if (window_is_iconized || suppress_redraws) {
                        return;
                    }
                    // Check if anything changed, which also updates formulas
                    final boolean anything_new = model.updateItemsAndCheckForNewSamples();

                    if (anything_new  &&   have_autoscale_axis ) {
                        plot.updateAutoscale();
                    }

                    if (model.isScrollEnabled()) {
                        performScroll();
                    } else
                    {
                        scrolling_was_off = true;
                        // Only redraw when needed
                        if (anything_new) {
                            plot.redrawTraces();
                        }
                    }
                }
                catch (final Throwable ex)
                {
                    Activator.getLogger().log(Level.WARNING, "Error in Plot refresh timer", ex); //$NON-NLS-1$
                }
            }
        };
        final long update_delay = (long) (model.getUpdatePeriod() * 1000);
        update_timer.schedule(update_task, update_delay, update_delay);
    }

    /** Stop scrolling and model items
     *  @throws IllegalStateException when not running
     */
    public void stop()
    {
        if (! isRunning())
         {
            throw new IllegalStateException("Not started"); //$NON-NLS-1$
        }
        // Stop ongoing archive access
        synchronized (archive_fetch_jobs)
        {
            for (final ArchiveFetchJob job : archive_fetch_jobs) {
                job.cancel();
            }
            archive_fetch_jobs.clear();
        }
        // Stop update task
        model.stop();
        update_task.cancel();
        update_task = null;
    }

    /** (Re-) create traces in plot for each item in the model */
    public void createPlotTraces()
    {
        plot.setBackgroundColor(model.getPlotBackground());
        plot.updateScrollButton(model.isScrollEnabled());
        plot.removeAll();
        for (int i=0; i<model.getAxisCount(); ++i) {
            plot.updateAxis(i, model.getAxis(i));
        }
        for (int i=0; i<model.getItemCount(); ++i)
        {
            final ModelItem item = model.getItem(i);
            if (item.isVisible()) {
                plot.addTrace(item);
            }
        }
    }

    /** Scroll the plot to 'now' */
    protected void performScroll()
    {
        if (! model.isScrollEnabled()) {
            return;
        }
        final long end_ms = System.currentTimeMillis();
        final long start_ms = end_ms - (long) (model.getTimespan()*1000);
        plot.setTimeRange(start_ms, end_ms);
        if (scrolling_was_off)
        {   // Scrolling was just turned on.
            // Get new archived data since the new time scale
            // could be way off what's in the previous time range.
            scrolling_was_off = false;
            getArchivedData();
        }
    }

    /** Initiate archive data retrieval for all model items
     *  @param start Start time
     *  @param end End time
     */
    private void getArchivedData()
    {
        final ITimestamp start = model.getStartTime();
        final ITimestamp end = model.getEndTime();
        for (int i=0; i<model.getItemCount(); ++i) {
            getArchivedData(model.getItem(i), start, end);
        }
    }

    /** Initiate archive data retrieval for a specific model item
     *  @param item Model item. NOP for non-PVItem
     *  @param start Start time
     *  @param end End time
     */
    private void getArchivedData(final ModelItem item,
            final ITimestamp start, final ITimestamp end)
    {
        // Only useful for PVItems with archive data source
        if (!(item instanceof PVItem)) {
            return;
        }
        final PVItem pv_item = (PVItem) item;
        if (pv_item.getArchiveDataSources().length <= 0) {
            return;
        }

        ArchiveFetchJob job;

        // Stop ongoing jobs for this item
        synchronized (archive_fetch_jobs)
        {
            for (int i=0; i<archive_fetch_jobs.size(); ++i)
            {
                job = archive_fetch_jobs.get(i);
                if (job.getPVItem() != pv_item) {
                    continue;
                }
                // System.out.println("Request for " + item.getName() + " cancels " + job);
                job.cancel();
                archive_fetch_jobs.remove(job);
            }
            // Start new job
            job = new ArchiveFetchJob(pv_item, start, end, this);
            archive_fetch_jobs.add(job);
        }
        job.schedule();
    }

    /** @see ArchiveFetchJobListener */
    @Override
    public void fetchCompleted(final ArchiveFetchJob job)
    {
        synchronized (archive_fetch_jobs)
        {
            archive_fetch_jobs.remove(job);
            // System.out.println("Completed " + job + ", " + archive_fetch_jobs.size() + " left");
            if (!archive_fetch_jobs.isEmpty()) {
                return;
            }
        }
        // All completed. Do something to the plot?
        final ArchiveRescale rescale = model.getArchiveRescale();
        if (rescale == ArchiveRescale.NONE) {
            return;
        }
        if (display == null  ||  display.isDisposed()) {
            return;
        }
        display.asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                if (display.isDisposed()) {
                    return;
                }
                switch (rescale)
                {
                case AUTOZOOM:
                    plot.getXYGraph().performAutoScale();
                    break;
                case STAGGER:
                    plot.getXYGraph().performStagger();
                    break;
                default:
                    break;
                }
            }
        });
    }

    /** @see ArchiveFetchJobListener */
    @Override
    public void archiveFetchFailed(final ArchiveFetchJob job,
            final ArchiveDataSource archive, final Exception error)
    {
        if (display == null  ||  display.isDisposed()) {
            return;
        }
        display.asyncExec(new Runnable()
        {
            @Override
            public void run()
            {
                if (display.isDisposed()) {
                    return;
                }
                final String message = NLS.bind(Messages.ArchiveAccessMessageFmt,
                            job.getPVItem().getDisplayName(), archive.getDescription());
                final String detail = NLS.bind(Messages.ArchiveAccessDetailFmt,
                        error.getMessage(), archive.getUrl());
                new ErrorDetailDialog(shell, Messages.Information, message, detail).open();
                job.getPVItem().removeArchiveDataSource(archive);
            }
        });
    }
}