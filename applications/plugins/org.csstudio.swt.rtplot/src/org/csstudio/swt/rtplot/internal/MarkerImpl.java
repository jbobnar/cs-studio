package org.csstudio.swt.rtplot.internal;

import org.csstudio.swt.rtplot.SWTMediaPool;
import org.csstudio.swt.rtplot.Marker;
import org.csstudio.swt.rtplot.data.PlotDataItem;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/** 
 *  Marker that's displayed on a xAxis.
 *  
 *  @param <XTYPE> Data type used for the {@link PlotDataItem}
 *  
 *  @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class MarkerImpl<XTYPE extends Comparable<XTYPE>> extends Marker<XTYPE> {
	
	/**
	 * Constructor.
	 * 
	 * @param position marker position
	 */
	public MarkerImpl(final XTYPE position) {
		super(position);
	}

	/**
	 * Paint the marker on given gc and axes.
	 *
	 * @param gc gc
	 * @param media swt media pool
	 * @param bounds bounds
	 * @param xaxis x axis
	 */
	public void paint(final GC gc, final SWTMediaPool media, final Rectangle bounds, final AxisPart<XTYPE> xaxis) {
        int x = xaxis.getScreenCoord(position);
        if (x >= bounds.x && x <= bounds.x + bounds.width) {
            gc.setForeground(media.get(getColor()));
            gc.setLineWidth(getLineWidth());
        	gc.drawLine(x, bounds.y, x, bounds.y + bounds.height);
        }
	}
}