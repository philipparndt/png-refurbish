package de.rnd7.pngrefurbish;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

public class PixelInfo {
	private final RGB rgb;
	private final int pixelValue;
	private final Point point;
	private final int alpha;
	
	public PixelInfo(final RGB rgb, final int pixelValue, final int alpha, final Point point) {
		super();
		this.rgb = rgb;
		this.pixelValue = pixelValue;
		this.point = point;
		this.alpha = alpha;
	}
	public RGB getRgb() {
		return this.rgb;
	}
	public int getPixelValue() {
		return this.pixelValue;
	}
	public Point getPoint() {
		return this.point;
	}
	
	@Override
	public String toString() {
		return String.format("<PixelInfo> %s %d %s", this.point.toString(), this.pixelValue, this.rgb.toString());
	}
	public int getAlpha() {
		return this.alpha;
	}
}
