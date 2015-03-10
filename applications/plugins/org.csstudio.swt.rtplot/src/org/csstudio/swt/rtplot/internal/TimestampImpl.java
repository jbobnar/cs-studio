package org.csstudio.swt.rtplot.internal;

import java.util.Random;

import org.csstudio.swt.rtplot.SWTMediaPool;
import org.csstudio.swt.rtplot.Timestamp;
import org.csstudio.swt.rtplot.data.PlotDataItem;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

/** 
 *  Timestamp that's displayed on a xAxis.
 *  
 *  @param <XTYPE> Data type used for the {@link PlotDataItem}
 *  
 *  @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class TimestampImpl<XTYPE extends Comparable<XTYPE>> extends Timestamp<XTYPE> {

	private RGB color;
	private int lineWidth;
	
	/**
	 * Constructor.
	 * 
	 * @param position timestamp position
	 */
	public TimestampImpl(final XTYPE position) {
		super(position);
	}

	/**
	 * Paint the timestamp on given gc and axes.
	 *
	 * @param gc gc
	 * @param media swt media pool
	 * @param bounds bounds
	 * @param xaxis x axis
	 */
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
	
	/**
	 * Sets timestamp position.
	 * 
	 * @param position timestamp position
	 */
	public void setPosition(final XTYPE position) {
		this.position = position;
	}
	
	/**
	 * Sets timestamp color.
	 * 
	 * @param color timestamp color
	 */
	public void setColor(final RGB color) {
		this.color = color;
	}
	
	/**
	 * @return timestamp color.
	 */
	public RGB getColor() {
		return color;
	}
	
	/**
	 * Sets timestamp line width.
	 * 
	 * @param lineWidth timestamp line width
	 */
	public void setLineWidth(final int lineWidth) {
		this.lineWidth = lineWidth;
	}

	/**
	 * @return timestamp line width.
	 */
	public int getLineWidth() {
		return lineWidth;
	}
}