package com.onpositive.prefeditor.dialogs;

import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple input dialog for soliciting an input string from the user.
 * <p>
 * This concrete dialog class can be instantiated as is, or further subclassed as
 * required.
 * </p>
 */
public class NewPreferenceDialog extends Dialog {
	
    private static final String ID_PATTERN = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final Pattern FQCN = Pattern.compile(ID_PATTERN + "(\\." + ID_PATTERN + ")*");
	
    /**
     * The title of the dialog.
     */
    private String title;

    /**
     * The message to display, or <code>null</code> if none.
     */
    private String message;
    
    /**
     * Preference parent
     */
    protected String parent = "";//$NON-NLS-1$
    
    /**
     * Preference name
     */
    protected String name = "";//$NON-NLS-1$

    /**
     * Preference value
     */
    protected String value = "";//$NON-NLS-1$

    /**
     * Ok button widget.
     */
    private Button okButton;

    /**
     * Name text widget.
     */
    protected Text nameText;

    
    /**
     * Value text widget.
     */
    protected Text valueText;

    /**
     * Error message label widget.
     */
    private Text errorMessageText;
    
    /**
     * Error message string.
     */
    private String errorMessage;

    /**
     * Possible parents for preference
     */
	private String[] possibleParents;

	/**
	 * Parent selection combo
	 */
	protected ComboViewer viewer;
    
    public NewPreferenceDialog(Shell parentShell, String initialParent, String[] possibleParents) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		this.possibleParents = possibleParents;
        this.title = "New preference";
        message = "Enter new preference details";
        parent = initialParent;
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	name = nameText.getText().trim();
            value = valueText.getText().trim();
            parent = getParentTxt();
        } else {
        	name = null;
            value = null;
            parent = null;
        }
        super.buttonPressed(buttonId);
    }

	protected String getParentTxt() {
		return viewer.getCombo().getText().trim();
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
			shell.setText(title);
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Cancel buttons by default
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        //do this here because setting the text will set enablement on the ok
        // button
        nameText.setFocus();
        if (name != null) {
            nameText.setText(name);
        }
        if (value != null) {
            valueText.setText(value);
        }
        viewer.setInput(possibleParents);
        if (this.parent != null) {
        	viewer.setSelection(new StructuredSelection(this.parent));
        }
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(
					SelectionChangedEvent event) {
				parentSelected(valueFromSelection(event.getSelection()));
				validateInput();
			}
		});
        viewer.getControl().addListener(SWT.Modify, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				validateInput();
			}
        	
        });
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        // create composite
        Composite composite = (Composite) super.createDialogArea(parent);
        ((GridLayout) composite.getLayout()).numColumns = 2;
        // create message
        if (message != null) {
            Label label = new Label(composite, SWT.WRAP);
            label.setText(message);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_CENTER);
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            data.horizontalSpan = 2;
            label.setLayoutData(data);
            label.setFont(parent.getFont());
        }
        
		createParentControls(composite);
        
        Label label = new Label(composite, SWT.WRAP);
        label.setText("Preference name:");
        
        nameText = new Text(composite, getInputTextStyle());
        nameText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        
        label = new Label(composite, SWT.WRAP);
        label.setText("Preference value:");
        
        valueText = new Text(composite, getInputTextStyle());
        valueText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        valueText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });
        errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
        GridDataFactory.fillDefaults().grab(true,false).span(2,1).applyTo(errorMessageText);
        errorMessageText.setBackground(errorMessageText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        // Set the error message text
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
        setErrorMessage(errorMessage);

        applyDialogFont(composite);
        return composite;
    }

	protected void createParentControls(Composite composite) {
		Label label = new Label(composite, SWT.WRAP);
        label.setText("Preference parent:");
        
        viewer = new ComboViewer(composite, SWT.DROP_DOWN);
        viewer.setContentProvider(new ArrayContentProvider());
        
        viewer.getControl().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
	}

    protected void parentSelected(String parent) {
		this.parent = parent;
	}

    /**
     * Returns the ok button.
     * 
     * @return the ok button
     */
    protected Button getOkButton() {
        return okButton;
    }

    /**
     * Returns the text area.
     * 
     * @return the text area
     */
    protected Text getText() {
        return valueText;
    }

    /**
     * Returns the string typed into this input dialog.
     * 
     * @return the input string
     */
    public String getValue() {
        return value;
    }

    /**
     * Validates the input.
     * <p>
     * The default implementation of this framework method delegates the request
     * to the supplied input validator object; if it finds the input invalid,
     * the error message is displayed in the dialog's message line. This hook
     * method is called whenever the text changes in the input field.
     * </p>
     */
    protected void validateInput() {
        String errorMessage = null;
        String parent = getParentTxt();
        int idx = parent.lastIndexOf('/');
		if (idx > -1) {
			parent = parent.substring(idx + 1);
		}
        String name = nameText.getText().trim();
        if (parent.isEmpty()) {
        	errorMessage = "Parent node/file name should be specified";
        } 
        else if (!FQCN.matcher(parent).matches()) {
        	errorMessage = "Parent should be valid plugin name, e.g. ab.cd.ef";
        } 
        else if (name.isEmpty()) {
        	errorMessage = "Preference name should be specified";
        }
        // Bug 16256: important not to treat "" (blank error) the same as null
        // (no error)
        setErrorMessage(errorMessage);
    }

    /**
     * Sets or clears the error message.
     * If not <code>null</code>, the OK button is disabled.
     * 
     * @param errorMessage
     *            the error message, or <code>null</code> to clear
     * @since 3.0
     */
    public void setErrorMessage(String errorMessage) {
    	this.errorMessage = errorMessage;
    	if (errorMessageText != null && !errorMessageText.isDisposed()) {
    		errorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
    		// Disable the error message text control if there is no error, or
    		// no error text (empty or whitespace only).  Hide it also to avoid
    		// color change.
    		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
    		boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
    		errorMessageText.setEnabled(hasError);
    		errorMessageText.setVisible(hasError);
    		errorMessageText.getParent().update();
    		// Access the ok button by id, in case clients have overridden button creation.
    		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
    		Control button = getButton(IDialogConstants.OK_ID);
    		if (button != null) {
    			button.setEnabled(errorMessage == null);
    		}
    	}
    }
    
	/**
	 * Returns the style bits that should be used for the input text field.
	 * Defaults to a single line entry. Subclasses may override.
	 * 
	 * @return the integer style bits that should be used when creating the
	 *         input text
	 * 
	 * @since 3.4
	 */
	protected int getInputTextStyle() {
		return SWT.SINGLE | SWT.BORDER;
	}

	public String getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}
	
    protected String valueFromSelection(ISelection selection) {
    	if (selection instanceof StructuredSelection) {
	    	Object firstElement = ((IStructuredSelection) selection).getFirstElement();
	        if (firstElement != null)
	        	return firstElement.toString();
    	}
		return null;
    }
}
