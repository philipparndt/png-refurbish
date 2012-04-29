package de.rnd7.pngrefurbish;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import external.org.eclipse.compare.internal.BufferedCanvas;

public class ImageEditor extends BufferedCanvas {

	public static final int SCALE = 10;
	
	private Image image = null;
	private Point size = new Point(200, 200);
	private boolean transparency = false;
	
	private Point selectionStart = null;
	private Point selectionEnd = null;

	private final Image raster;
	
	private final Collection<IPixelSelectionListener> listeners = new ArrayList<IPixelSelectionListener>();

	private Image originalImage;
	
	public ImageEditor(final Composite parent, final int style) {
		super(parent, style);
		
		this.raster = this.createUnavailableImageRaster();

		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				if (ImageEditor.this.selectionStart != null) {
					ImageEditor.this.firePixelSelection();
					ImageEditor.this.selectionStart = null;
					ImageEditor.this.selectionEnd = null;
					ImageEditor.this.redraw();
				}
			}
			
			@Override
			public void mouseDown(final MouseEvent e) {
				ImageEditor.this.selectionStart = new Point(e.x, e.y);
				ImageEditor.this.selectionEnd = new Point(e.x, e.y);
				ImageEditor.this.redraw();
			}
			
			@Override
			public void mouseDoubleClick(final MouseEvent e) {
				
			}
		});
		this.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(final MouseEvent e) {
				if (ImageEditor.this.selectionStart != null) {
					ImageEditor.this.selectionEnd = new Point(e.x, e.y);
					ImageEditor.this.redraw();
				}
			}
		});
		
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					ImageEditor.this.selectionStart = null;
					ImageEditor.this.repaint();
				}
			}
		});
	}
	
	public Rectangle getSelectionRectangle() {
		final int x1 = (int) Math.ceil(Math.min(this.selectionStart.x, this.selectionEnd.x) / SCALE) * SCALE;
		final int x2 = (int) (Math.floor(Math.max(this.selectionStart.x, this.selectionEnd.x) / SCALE) + 1) * SCALE;
		final int y1 = (int) Math.ceil(Math.min(this.selectionStart.y, this.selectionEnd.y) / SCALE) * SCALE;
		final int y2 = (int) (Math.floor(Math.max(this.selectionStart.y, this.selectionEnd.y) / SCALE) + 1) * SCALE;
		return new Rectangle(x1, y1, x2 - x1, y2 - y1);
	}
	
	@Override
	public void doPaint(final GC gc) {
		if (ImageEditor.this.image != null) {
			final Rectangle bounds = ImageEditor.this.image.getBounds();
			if (ImageEditor.this.transparency) {
				this.drawRaster(new Point(0, 0), new Point(bounds.width * SCALE, bounds.height * SCALE), gc, this.raster);
			}
			gc.drawImage(ImageEditor.this.image, 0, 0, bounds.width, bounds.height, 0, 0, bounds.width * SCALE, bounds.height * SCALE);

			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
			
			for (int x = 0; x <= bounds.width; x++) {
				gc.drawLine(x * SCALE, 0, x * SCALE, bounds.height * SCALE);
			}
			
			for (int y = 0; y <= bounds.height; y++) {
				gc.drawLine(0, y * SCALE, bounds.width * SCALE, y * SCALE);
			}
		}
		
		this.drawSelection(gc);
	}
	
	private void drawRaster(final Point offset, final Point size, final GC gc, final Image raster) {
		gc.setClipping(new Rectangle(offset.x, offset.y, ImageEditor.this.size.x, ImageEditor.this.size.y));
		final int width = raster.getImageData().width;
		final int height = raster.getImageData().height;
		for (int x = 0; x < size.x; x += width) {
			for (int y = 0; y < size.y; y += height) {
				gc.drawImage(raster, offset.x + x, offset.y + y);
			}
		}
		gc.setClipping((Rectangle) null);
	}
	
	private void drawSelection(final GC gc) {
		if (ImageEditor.this.selectionStart != null) {
			gc.setAlpha(80);
			
			final Rectangle rectangle = this.getSelectionRectangle();
			gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_BLUE));
			gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLUE));
			gc.fillRectangle(rectangle);
			gc.setAlpha(255);
			gc.drawRectangle(rectangle);
		}
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
			}
			colorIndex = colorIndex == 0 ? 1 : 0;
		}

		return new Image(this.getDisplay(), imageData);
	}

	
	public void setImage(final Image image) {
		this.image = image;
		this.update();
		this.redraw();
	}
	
	public void freezeImage() {
		this.originalImage = new Image(this.image.getDevice(), this.image.getImageData());
	}
	
	public Image getImage() {
		return this.image;
	}

	@Override
	public Point computeSize(final int wHint, final int hHint) {
		return this.size;
	}
	
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		return this.size;
	}
	
	@Override
	public void setSize(final Point size) {
		this.size = size;
		super.setSize(size);
	}
	
	public void setTransparency(final boolean transparency) {
		this.transparency = transparency;
	}
	
	public void addListener(final IPixelSelectionListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(final IPixelSelectionListener listener) {
		this.listeners.remove(listener);
	}
	
	private void firePixelSelection() {
		if (this.image != null) {
			final Rectangle selectionRectangle = this.getSelectionRectangle();
			final ImageData imageData = this.originalImage.getImageData();
			
			final int x1 = Math.max(selectionRectangle.x / SCALE, 0);
			final int x2 = Math.max((selectionRectangle.x + selectionRectangle.width) / SCALE, 0);
			final int y1 = Math.max(selectionRectangle.y / SCALE, 0);
			final int y2 = Math.max((selectionRectangle.y + selectionRectangle.height) / SCALE, 0);

			final Rectangle bounds = this.originalImage.getBounds();

			final Collection<PixelInfo> pixelInfos = new ArrayList<PixelInfo>();
			
			for (int x = x1; x < x2; x++) {
				for (int y = y1; y < y2; y++) {
					if (x < bounds.width && y < bounds.height) {
						final int pixelValue = imageData.getPixel(x, y);
						final RGB rgb = imageData.palette.getRGB(pixelValue);
						final int alpha = imageData.getAlpha(x, y);
						
						final PixelInfo pixelInfo = new PixelInfo(rgb, pixelValue, alpha, new Point(x, y));
						pixelInfos.add(pixelInfo);
					}
				}
			}
			
			if (!pixelInfos.isEmpty()) {
				for (final IPixelSelectionListener listener : this.listeners) {
					listener.pixelSelected(pixelInfos);
				}
			}
		}
	}
}
