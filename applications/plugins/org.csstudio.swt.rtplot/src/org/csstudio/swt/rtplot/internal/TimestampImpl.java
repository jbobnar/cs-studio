package org.csstudio.swt.rtplot.internal;

import java.util.Random;

import org.csstudio.swt.rtplot.SWTMediaPool;
import org.csstudio.swt.rtplot.Timestamp;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

public class TimestampImpl<XTYPE extends Comparable<XTYPE>> extends Timestamp<XTYPE> {

	private RGB color;
	private int lineWidth;
	
	public TimestampImpl(final XTYPE position) {
		super(position);
	}

	public void paint(final GC gc, final SWTMediaPool media, final Rectangle bounds, final AxisPart<XTYPE> xaxis) {
        int x = xaxis.getScreenCoord(position);
        if (x >= bounds.x && x <= bounds.x + bounds.width) {
        	if (color == null) {
        		Random rand = new Random();
        		color = new RGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
        	}
            gc.setForeground(media.get(color));
            gc.setLineWidth(lineWidth == 0 ? 2 : lineWidth);
        	gc.drawLine(x, bounds.y, x, bounds.y + bounds.height);
        }
	}
	
	public void setPosition(final XTYPE position) {
		this.position = position;
	}
	
	public void setColor(final RGB color) {
		this.color = color;
	}
	
	public RGB getColor() {
		return color;
	}
	
	public void setLineWidth(final int lineWidth) {
		this.lineWidth = lineWidth;
	}
	
	public int getLineWidth() {
		return lineWidth;
	}
}
