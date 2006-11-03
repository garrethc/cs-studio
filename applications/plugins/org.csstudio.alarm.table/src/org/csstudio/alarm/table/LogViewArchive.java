package org.csstudio.alarm.table;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.csstudio.alarm.dbaccess.ArchiveDBAccess;
import org.csstudio.alarm.dbaccess.archivedb.ILogMessageArchiveAccess;
import org.csstudio.alarm.table.dataModel.JMSMessageList;
import org.csstudio.alarm.table.expertSearch.ExpertSearchDialog;
import org.csstudio.alarm.table.logTable.JMSLogTableViewer;
import org.csstudio.alarm.table.preferences.LogArchiveViewerPreferenceConstants;
import org.csstudio.alarm.table.timeSelection.StartEndDialog;
import org.csstudio.data.Timestamp;

import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;


/**
 * Simple view more like console, used to write log messages
 */
public class LogViewArchive extends ViewPart {

	public static final String ID = LogViewArchive.class.getName();

	private Shell parentShell = null;

	private JMSMessageList jmsml = null;

	private JMSLogTableViewer jlv = null;

	private String[] columnNames;

	private Text timeFrom;

	private Text timeTo;

	public void createPartControl(Composite parent) {

		columnNames = JmsLogsPlugin.getDefault().getPluginPreferences()
				.getString(LogArchiveViewerPreferenceConstants.P_STRINGArch)
				.split(";");
		jmsml = new JMSMessageList(columnNames);

		parentShell = parent.getShell();

		GridLayout grid = new GridLayout();
		grid.numColumns = 1;
		parent.setLayout(grid);
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
		comp.setLayout(new GridLayout(4, true));

		Group buttons = new Group(comp, SWT.LINE_SOLID);
		buttons.setText("Period");
		buttons.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1,
				1));
		buttons.setLayout(new GridLayout(5, true));

		create24hButton(buttons);
		create72hButton(buttons);
		createWeekButton(buttons);
		createFlexButton(buttons);
		createSearchButton(buttons);


		Group from = new Group(comp, SWT.LINE_SOLID);
		from.setText("from");
		from.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		from.setLayout(new GridLayout(1, true));

		timeFrom = new Text(from, SWT.SINGLE);
		timeFrom.setEditable(false);
		timeFrom.setText("                            ");
		Group to = new Group(comp, SWT.LINE_SOLID);
		to.setText("to");
		to.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		to.setLayout(new GridLayout(1, true));

		timeTo = new Text(to, SWT.SINGLE);
		timeTo.setEditable(false);
		timeTo.setText("                              ");

		jlv = new JMSLogTableViewer(parent, getSite(), columnNames, jmsml);
		jlv.setAlarmSorting(false);
		parent.pack();
		JmsLogsPlugin.getDefault().getPluginPreferences()
				.addPropertyChangeListener(propertyChangeListener);

	}

	private void create72hButton(Composite comp) {
		Button b72hSearch = new Button(comp, SWT.PUSH);
		b72hSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		b72hSearch.setText("3 DAYS");

		b72hSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ILogMessageArchiveAccess adba = new ArchiveDBAccess();
				GregorianCalendar to = new GregorianCalendar();
				GregorianCalendar from = (GregorianCalendar) to.clone();
				from.add(GregorianCalendar.HOUR, -72);
				showNewTime(from, to);
				ArrayList<HashMap<String, String>> am = adba.getLogMessages(
						from, to);
				jmsml.clearList();
				jlv.refresh();
				jmsml.addJMSMessageList(am);

			}
		});

	}

	private void createWeekButton(Composite comp) {
		Button b168hSearch = new Button(comp, SWT.PUSH);
		b168hSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		b168hSearch.setText("WEEK");

		b168hSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ILogMessageArchiveAccess adba = new ArchiveDBAccess();
				GregorianCalendar to = new GregorianCalendar();
				GregorianCalendar from = (GregorianCalendar) to.clone();
				from.add(GregorianCalendar.HOUR, -168);
				showNewTime(from, to);
				ArrayList<HashMap<String, String>> am = adba.getLogMessages(
						from, to);
				jmsml.clearList();
				jlv.refresh();
				jmsml.addJMSMessageList(am);
			}
		});

	}

	private void createFlexButton(Composite comp) {
		Button bFlexSearch = new Button(comp, SWT.PUSH);
		bFlexSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		bFlexSearch.setText("USER");

		bFlexSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Timestamp start = new Timestamp((new Date().getTime()) / 1000);
				Timestamp end = new Timestamp((new Date().getTime()) / 1000);
				StartEndDialog dlg = new StartEndDialog(parentShell, start, end);
				if (dlg.open() == StartEndDialog.OK) {
					double low = dlg.getStart().toDouble();
					double high = dlg.getEnd().toDouble();
					ILogMessageArchiveAccess adba = new ArchiveDBAccess();
					GregorianCalendar from = new GregorianCalendar();
					GregorianCalendar to = new GregorianCalendar();
					if (low < high) {
						from.setTimeInMillis((long) low * 1000);
						to.setTimeInMillis((long) high * 1000);
					} else {
						from.setTimeInMillis((long) high * 1000);
						to.setTimeInMillis((long) low * 1000);
					}
					showNewTime(from, to);
					ArrayList<HashMap<String, String>> am = adba
							.getLogMessages(from, to);
					jmsml.clearList();
					jlv.refresh();
					jmsml.addJMSMessageList(am);
				}
			}
		});

	}

	private void createSearchButton(Composite comp) {
		Button bSearch = new Button(comp, SWT.PUSH);
		bSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		bSearch.setText("Expert");

		bSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				System.out.println("expert pushed");
				Timestamp start = new Timestamp((new Date().getTime()) / 1000);
				Timestamp end = new Timestamp((new Date().getTime()) / 1000);
				System.out.println("expert 2");
				ExpertSearchDialog dlg = new ExpertSearchDialog(parentShell, start, end);
				if (dlg.open() == ExpertSearchDialog.OK) {
					System.out.println("expert open");
				}
//				ILogMessageArchiveAccess adba = new ArchiveDBAccess();
//				GregorianCalendar to = new GregorianCalendar();
//				GregorianCalendar from = (GregorianCalendar) to.clone();
//				from.add(GregorianCalendar.HOUR, -72);
//				showNewTime(from, to);
//				ArrayList<HashMap<String, String>> am = adba.getLogMessages(
//						from, to);
//				jmsml.clearList();
//				jlv.refresh();
//				jmsml.addJMSMessageList(am);

			}
		});
	}


	private void create24hButton(Composite comp) {
		Button b24hSearch = new Button(comp, SWT.PUSH);
		b24hSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
				1, 1));
		b24hSearch.setText("DAY");

		b24hSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ILogMessageArchiveAccess adba = new ArchiveDBAccess();
				GregorianCalendar to = new GregorianCalendar();
				GregorianCalendar from = (GregorianCalendar) to.clone();
				from.add(GregorianCalendar.HOUR, -24);
				showNewTime(from, to);
				ArrayList<HashMap<String, String>> am = adba.getLogMessages(
						from, to);
				jmsml.clearList();
				jlv.refresh();
				jmsml.addJMSMessageList(am);

			}
		});

	}

	private void showNewTime(GregorianCalendar from, GregorianCalendar to) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		timeFrom.setText(sdf.format(from.getTime()));
		timeTo.setText(sdf.format(to.getTime()));
		timeFrom.getParent().getParent().redraw();
	}

	public void setFocus() {
	}

	public void dispose() {
		super.dispose();
		JmsLogsPlugin.getDefault().getPluginPreferences()
				.removePropertyChangeListener(propertyChangeListener);
	}

	private final IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			columnNames = JmsLogsPlugin
					.getDefault()
					.getPluginPreferences()
					.getString(LogArchiveViewerPreferenceConstants.P_STRINGArch)
					.split(";");
			jlv.setColumnNames(columnNames);

			Table t = jlv.getTable();
			TableColumn[] tc = t.getColumns();

			int diff = tc.length - columnNames.length;

			if (diff < 0) {
				for (int i = 0; i < diff; i++) {
					TableColumn tableColumn = new TableColumn(t, SWT.CENTER);
					tableColumn.setText(new Integer(i).toString());
					tableColumn.setWidth(100);
				}
			} else if (diff > 0) {
				diff = (-1) * diff;
				for (int i = 0; i < diff; i++) {
					tc[i].dispose();
				}
			}

			for (int i = 0; i < tc.length; i++) {
				tc[i].setText(columnNames[i]);
			}
			jlv.refresh();
		}
	};
}
