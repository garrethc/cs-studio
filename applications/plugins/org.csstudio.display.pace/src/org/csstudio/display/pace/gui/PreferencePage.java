package org.csstudio.display.pace.gui;

import org.csstudio.display.pace.Activator;
import org.csstudio.display.pace.Messages;
import org.csstudio.display.pace.Preferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


/** Preference page (GUI) for PACE settings
 *  @author Kay Kasemir
 */
public class PreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage
{

    public PreferencePage()
    {
        // This way, preference changes in the GUI end up in a file under
        // {workspace}/.metadata/.plugins/org.eclipse.core.runtime/.settings/,
        // i.e. they are specific to the workspace instance.
        final IPreferenceStore store =
            new ScopedPreferenceStore(new InstanceScope(), Activator.ID);
        setPreferenceStore(store);
        setMessage(Messages.Preferences_Message);
    }

    /** {@inheritDoc */
    @Override
    public void init(IWorkbench workbench)
    { /* NOP */ }

    /** {@inheritDoc */
    @Override
    protected void createFieldEditors()
    {
        final Composite parent = getFieldEditorParent();

        // RDB
        addField(new StringFieldEditor(Preferences.DEFAULT_LOGBOOK, Messages.Preferences_DefaultLogbook, parent));
    }
}
