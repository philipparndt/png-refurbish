package de.rnd7.test;

import junit.framework.Assert;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.Test;

public class MyTest {

	@Test
	public void testSwt() {
		

		Shell parent = new Shell();
		MyControl control = new MyControl(parent, SWT.NONE);
		
//		Text firstname = findText(control, "firstname");
//		firstname.setText("foo");
//		
//		Text lastname = findText(control, "lastname");
//		lastname.setText("bar");

		SWTBot bot = new SWTBot(control);

		SWTBotText firstname = bot.textWithId("firstname");
//		firstname.typeText("foo", 500);
		firstname.setText("foo");
		
		SWTBotText lastname = bot.textWithId("lastname");
		lastname.setText("bar");
		
		Assert.assertEquals("foo", control.getFirstname());
		Assert.assertEquals("bar", control.getLastname());

		SWTBotButton clear = bot.buttonWithId("clear");
		clear.click();
				
		Assert.assertEquals("", control.getFirstname());
		Assert.assertEquals("", control.getLastname());

	}
	
	private Text findText(Composite parent, String key) {
		for (Widget widget : parent.getChildren()) {
			if (widget instanceof Text) {
				if (key.equals(widget.getData("org.eclipse.swtbot.widget.key"))) {
					return (Text) widget;
				}
			}
		}

		throw new RuntimeException("Cannot find widget");
	}
}
