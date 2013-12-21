package de.rnd7.test;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class MyControl extends Composite {

	protected String firstname;
	protected String lastname; 
	
	private ArrayList<Listener> listeners = new ArrayList<Listener>();
	
	public MyControl(Composite parent, int style) {
		super(parent, style);
		
		
		final Text firstname = new Text(this, SWT.BORDER);
		firstname.setData("org.eclipse.swtbot.widget.key", "firstname");
		firstname.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				MyControl.this.firstname = firstname.getText();
				fire();
			}
		});
		
		final Text lastname = new Text(this, SWT.BORDER);
		lastname.setData("org.eclipse.swtbot.widget.key", "lastname");
		lastname.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				MyControl.this.lastname = lastname.getText();
				fire();
			}
		});
		
		final Button button = new Button(this, SWT.NONE);
		button.setData("org.eclipse.swtbot.widget.key", "clear");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				firstname.setText("");
				lastname.setText("");
			}
		});
    }

	public String getFirstname() {
		return firstname;
	}
	
	public String getLastname() {
		return lastname;
	}
	
	private void fire() {
		for (Listener listener : listeners) {
			listener.handleEvent(new Event());
		}
	}
	
	public void addListener(Listener listener) {
		listeners.add(listener);
	}
}
