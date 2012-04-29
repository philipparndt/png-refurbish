package de.rnd7.pngrefurbish;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ColorPicker extends Composite {

	private RGB RGB = null;
	private Color color = null;
	private int pixelData = -1;
	
	private Canvas canvas;
	
	private boolean selected = false;
	
	public ColorPicker(final Composite parent, final String title) {
		super(parent, SWT.NONE);
		
		this.setLayout(new GridLayout(2, false));
		
		final Image raster = this.createUnavailableImageRaster();
		
		final Label label = new Label(this, SWT.NONE);
		label.setText(title + ":");
		
		final Point size = new Point(25, 25);
		
		this.canvas = new Canvas(this, SWT.BORDER) {
			@Override
			public Point computeSize(final int wHint, final int hHint) {
				return size;
			}
			
			@Override
			public Point computeSize(final int wHint, final int hHint, final boolean changed) {
				return size;
			}
		};
		
		this.canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e) {
				ColorPicker.this.selected = !ColorPicker.this.selected;
				
				ColorPicker.this.canvas.redraw();
			}
		});
		
		this.canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(final PaintEvent e) {
				if (ColorPicker.this.selected || !ColorPicker.this.hasColor()) {
					this.drawRaster(new Point(0, 0), size, e.gc, raster);
				}
				else {
					e.gc.setBackground(ColorPicker.this.getColor());
					e.gc.fillRectangle(0, 0, size.x, size.y);
				}
				if (ColorPicker.this.selected) {
					e.gc.setBackground(e.gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
					e.gc.setAlpha(80);
					e.gc.fillRectangle(0, 0, size.x, size.y);
					e.gc.setAlpha(255);
				}
			}
			
			private void drawRaster(final Point offset, final Point size, final GC gc, final Image raster) {
				gc.setClipping(new Rectangle(offset.x, offset.y, size.x, size.y));
				final int width = raster.getImageData().width;
				final int height = raster.getImageData().height;
				for (int x = 0; x < size.x; x += width) {
					for (int y = 0; y < size.y; y += height) {
						gc.drawImage(raster, offset.x + x, offset.y + y);
					}
				}
				gc.setClipping((Rectangle) null);
			}

		});
		
		this.canvas.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_BLACK));
	}
	
	private Image createUnavailableImageRaster() {
		final PaletteData paletteData = new PaletteData(
				new RGB[] {
						new RGB(255, 255, 255), new RGB(180, 180, 180)
				});

		final int size = 10;

		final ImageData imageData = new ImageData(size, size, 1, paletteData);
		int colorIndex = 0;
		for (int x = 0; x < size; x += 5) {
			for (int y = 0; y < size; y += 5) {
				for (int dx = 0; dx < 5; dx++) {
					for (int dy = 0; dy < 5; dy++) {
						imageData.setPixel(x+dx, y+dy, colorIndex);
					}
				}
				colorIndex = colorIndex == 0 ? 1 : 0;
			};
			colorIndex = colorIndex == 0 ? 1 : 0;
		}

		return new Image(this.getDisplay(), imageData);
	}
	
	public boolean hasColor() {
		return this.color != null;
	}
	
	public RGB getRGB() {
		return this.RGB;
	}
	
	public int getPixelData() {
		return this.pixelData;
	}
	
	public Color getColor() {
		return this.color;
	}

	public void select(final RGB rgb, final int pixelData) {
		this.RGB = rgb;
		this.pixelData = pixelData;
		
		if (this.color != null && !this.color.isDisposed()) {
			this.color.dispose();
		}
		
		this.color = new Color(this.canvas.getDisplay(), rgb);
		
		this.selected = false;
		this.canvas.setBackground(this.color);
	}
	
	public boolean isSelected() {
		return this.selected;
	}

	public void setSelection(final boolean selected) {
		this.selected = selected;
		this.canvas.redraw();
	}
	
}
