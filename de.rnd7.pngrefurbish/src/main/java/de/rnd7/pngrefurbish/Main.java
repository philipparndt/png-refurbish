package de.rnd7.pngrefurbish;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class Main implements IPixelSelectionListener {

	private Image image;

	private final Button smoothPaint;
	private final Button undoPaint;
	
	private final Display display;
	private final Collection<ImageEditor> editors = new ArrayList<ImageEditor>();

	private ColorPicker baseColorPicker;
	private ColorPicker backgroundColorPicker;
	
	public Main() throws Exception {
		this.display = new Display();
		final Shell shell = new Shell(this.display);
		shell.setLayout(new GridLayout());

		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		
		this.createEditors(composite, new int[]{SWT.COLOR_BLACK, SWT.COLOR_WHITE, SWT.COLOR_GRAY, SWT.COLOR_DARK_GRAY,
										   		SWT.COLOR_RED, SWT.COLOR_GREEN, SWT.COLOR_CYAN, -1});
		
		this.baseColorPicker = new ColorPicker(shell, "Base color");
		this.backgroundColorPicker = new ColorPicker(shell, "Background color");
		
		this.smoothPaint = new Button(shell, SWT.TOGGLE);
		this.smoothPaint.setText("Paint smooth pixels");

		this.undoPaint = new Button(shell, SWT.TOGGLE);
		this.undoPaint.setText("Undo paint pixels");
		
		this.smoothPaint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Main.this.baseColorPicker.setSelection(false);
				Main.this.backgroundColorPicker.setSelection(false);
				Main.this.undoPaint.setSelection(false);
				
				if (!Main.this.baseColorPicker.hasColor()) {
					MessageDialog.openError(shell, "Select base color first!", "Select the base color first!");
					Main.this.smoothPaint.setSelection(false);
					return;
				}
				if (!Main.this.backgroundColorPicker.hasColor()) {
					MessageDialog.openError(shell, "Select background color first!", "Select the background color first!");
					Main.this.smoothPaint.setSelection(false);
					return;
				}
			}
		});
		
		this.undoPaint.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				Main.this.baseColorPicker.setSelection(false);
				Main.this.backgroundColorPicker.setSelection(false);
				Main.this.smoothPaint.setSelection(false);
			}
		});
		
		this.createMenu(shell);
		
		shell.setText("Smoothie!");
		shell.setSize(new Point(850, 650));
		shell.open();
		
		while (!shell.isDisposed ()) {
			if (!this.display.readAndDispatch()) {
				this.display.sleep();
			}
		}
		this.display.dispose();
	}
	
	private void createEditors(final Composite composite, final int[] backgroundColors) {
		for (final int colorIndex : backgroundColors) {
			final ImageEditor editor = new ImageEditor(composite, SWT.BORDER);
			if (colorIndex != -1) {
				editor.setBackground(this.display.getSystemColor(colorIndex));
			}
			editor.setTransparency(colorIndex == -1);
			editor.addListener(this);
			this.editors.add(editor);
		}
	}
	
	private void createMenu(final Shell shell) {
		final Menu menu = new Menu(shell, SWT.BAR);
		final Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		final MenuItem fileMenuHeader = new MenuItem(menu, SWT.CASCADE);
		fileMenuHeader.setMenu(fileMenu);
		fileMenuHeader.setText("&File");
		
		final MenuItem open = new MenuItem(fileMenu, SWT.PUSH);
		open.setText("Open...");
		open.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				
				dialog.setFilterExtensions(new String [] {"*.png", "*.*"});
				dialog.setFilterNames(new String [] {"*.png", "All files (*.*)"});
				final String filename = dialog.open();
				
				if (filename != null) {
					Main.this.image = new Image(Main.this.display, filename);
					
					for (final ImageEditor ed : Main.this.editors) {
						ed.setImage(Main.this.image);
						ed.freezeImage();
					}
				}
			}
		});
		
		final MenuItem save = new MenuItem(fileMenu, SWT.PUSH);
		save.setText("Save as...");
		save.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				
				dialog.setFilterExtensions(new String [] {"*.png", "*.*"});
				dialog.setFilterNames(new String [] {"*.png", "All files (*.*)"});
				final String filename = dialog.open();
				
				if (filename != null) {
					final ImageLoader loader = new ImageLoader();
					loader.data = new ImageData[] {Main.this.image.getImageData()};
					loader.save(filename, SWT.IMAGE_PNG);
				}
			}
		});
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		final MenuItem about = new MenuItem(fileMenu, SWT.PUSH);
		about.setText("About...");
		about.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				MessageDialog.openInformation(shell, "About", "Smoothie!\n\n(c) 2012 Vector Informatik GmbH\nPES 2.1");
			}
		});
		shell.setMenuBar(menu);
	}
	
	@Override
	public void pixelSelected(final Collection<PixelInfo> pixelInfos) {
		if (this.baseColorPicker.isSelected()) {
			final PixelInfo pixelInfo = pixelInfos.iterator().next();
			this.baseColorPicker.select(pixelInfo.getRgb(), pixelInfo.getPixelValue());
		}
		
		if (this.backgroundColorPicker.isSelected()) {
			final PixelInfo pixelInfo = pixelInfos.iterator().next();
			this.backgroundColorPicker.select(pixelInfo.getRgb(), pixelInfo.getPixelValue());
		}

		if (this.smoothPaint.getSelection()) {
			this.selectColorToChange(pixelInfos);
		}
		
		if (this.undoPaint.getSelection()) {
			this.undoPaint(pixelInfos);
		}
	}
	
	private void undoPaint(final Collection<PixelInfo> pixelInfos) {	
		final ImageData imageData = Main.this.image.getImageData();
		

		for (final PixelInfo pixelInfo : pixelInfos) {
			final Point point = pixelInfo.getPoint();
			imageData.setPixel(point.x, point.y, pixelInfo.getPixelValue());
			imageData.setAlpha(point.x, point.y, pixelInfo.getAlpha());
		}
		
		Main.this.image.dispose();
		Main.this.image = new Image(this.display, imageData);
		
		for (final ImageEditor editor : this.editors) {
			editor.setImage(Main.this.image);
		}
	}

	private void selectColorToChange(final Collection<PixelInfo> pixelInfos) {							
		final ImageData imageData = Main.this.image.getImageData();
		
		if (!this.baseColorPicker.hasColor() || !this.backgroundColorPicker.hasColor()) {
			return;
		}
		
		final Color cbase = this.baseColorPicker.getColor();
		final RGB base = cbase.getRGB();
		
		final Color cbackground = this.backgroundColorPicker.getColor();
		final RGB background = cbackground.getRGB();

		for (final PixelInfo pixelInfo : pixelInfos) {
			final int alpha = this.calcOpacity(base, background, pixelInfo.getRgb());
	
			final Point point = pixelInfo.getPoint();
			imageData.setPixel(point.x, point.y, Main.this.baseColorPicker.getPixelData());
			imageData.setAlpha(point.x, point.y, alpha);
		}
		
		Main.this.image.dispose();
		
		Main.this.image = new Image(this.display, imageData);
		
		for (final ImageEditor editor : this.editors) {
			editor.setImage(Main.this.image);
		}
	}
	
	public int calcOpacity(final RGB base, final RGB background, final RGB resulting) {
		final Image image = new Image(this.display, 1, 1);
		
//		target.red = alpha * new + (1 - alpha) * old
		
//		original R = (resulting R - ((1-opacity)*background R)) / opacity.
//		original G = (resulting G - ((1-opacity)*background G)) / opacity.
//		original B = (resulting B - ((1-opacity)*background B)) / opacity.

//		a = (b - ((1-x) * c)) / x
//		a * x = b - (c - c * x)
		
		final float opacityRed = ((float)resulting.red - (float)background.red) / ((float)base.red - (float)background.red);
		final float opacityGreen = ((float)resulting.green - (float)background.green) / ((float)base.green - (float)background.green);
		final float opacityBlue = ((float)resulting.blue - (float)background.blue) / ((float)base.blue - (float)background.blue);
	
		final double opacitySummary = (0.299*opacityRed + 0.587*opacityGreen + 0.114*opacityBlue);
		image.dispose();
		
		return Math.max(Math.min(new Double(255 * opacitySummary).intValue(), 255), 0);
	}
	
	public static void main (final String [] args) {
		try  {
			new Main();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
