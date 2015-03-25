/*******************************************************************************
 * Copyright (c) 2014 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.swt.rtplot.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.csstudio.swt.rtplot.Axis;
import org.csstudio.swt.rtplot.SWTMediaPool;
import org.csstudio.swt.rtplot.Trace;
import org.csstudio.swt.rtplot.data.PlotDataItem;
import org.csstudio.swt.rtplot.data.PlotDataProvider;
import org.csstudio.swt.rtplot.internal.util.ScreenTransform;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/** 
 * 	Helper for painting a {@link Trace}.
 * 
 *  @param <XTYPE> Data type of horizontal {@link Axis}
 *  
 *  @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class SmartTracePainter<XTYPE extends Comparable<XTYPE>> extends TracePainter<XTYPE>
{
    private static final int REDUCTION_DISTANCE = 2;    

    /** @param gc GC
     *  @param media
     *  @param bounds Clipping bounds within which to paint
     *  @param x_transform Coordinate transform used by the x axis
     *  @param trace Trace, has reference to its value axis
     */
    @Override
    public void paint(final GC gc, final SWTMediaPool media, final Rectangle bounds, final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis, final Trace<XTYPE> trace, final PlotDataProvider<XTYPE> data)
    {
    	data.getLock().lock();
    	try {
    		super.paint(gc, media, bounds, x_transform, y_axis, trace, getReducedDataProvider(data, x_transform, y_axis));
    	} finally {
    		data.getLock().unlock();
    	}
    }
    
    /**
     * Reduce data provider data.
     * 
     * @param data data provider
     * @param x_transform x transform
     * @param y_axis y axis
     * 
     * @return reduced data list.
     */
	private List<PlotDataItem<XTYPE>> reduction(final PlotDataProvider<XTYPE> data, final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis) {
		List<PlotDataItem<XTYPE>> reducedList = new ArrayList<PlotDataItem<XTYPE>>();
		int N = data.size();
		int x0 = -1;
		int position = 0;
		double val0;
		if (N == 0) {
			return reducedList;
		}
		do {
			PlotDataItem<XTYPE> item = data.get(position);
			reducedList.add(item);
			x0 = (int) x_transform.transform(item.getPosition());
			val0 = item.getValue();
			position ++;
		} while (Double.isNaN(val0));
		int y0 = (int) y_axis.getScreenCoord(val0);
		for (int i = position; i < N; i++) {
			PlotDataItem<XTYPE> nextItem = data.get(i);
			int x1 = (int) x_transform.transform(nextItem.getPosition());
			double val1 = nextItem.getValue();
			if (Double.isNaN(val1)) {
				reducedList.add(nextItem);
			} else {
				int y1 = (int) y_axis.getScreenCoord(val1);
				if ((x0 - x1 > REDUCTION_DISTANCE || x1 - x0 > REDUCTION_DISTANCE)
						&& (y0 - y1 > REDUCTION_DISTANCE || y1 - y0 > REDUCTION_DISTANCE)) {
					reducedList.add(nextItem);
					x0 = x1;
					y0 = y1;
				}
			}
		}
		return reducedList;
	}
	
	/**
	 * Returns reduced data provider.
	 * 
	 * @param data data provider
	 * @param x_transform x transform
	 * @param y_axis y axis
	 * 
	 * @return reduced data provider.
	 */
	private PlotDataProvider<XTYPE> getReducedDataProvider(final PlotDataProvider<XTYPE> data, final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis) {
		List<PlotDataItem<XTYPE>> reducedDataList = reduction(data, x_transform, y_axis);
        return new PlotDataProvider<XTYPE>() {

			@Override
			public Lock getLock() {
				return data.getLock();
			}

			@Override
			public int size() {
				return reducedDataList.size();
			}

			@Override
			public PlotDataItem<XTYPE> get(int index) {
				return reducedDataList.get(index);
			}
		};
	}
}