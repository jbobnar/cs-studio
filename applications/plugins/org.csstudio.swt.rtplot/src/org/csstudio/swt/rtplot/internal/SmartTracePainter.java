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
import org.csstudio.swt.rtplot.PointType;
import org.csstudio.swt.rtplot.SWTMediaPool;
import org.csstudio.swt.rtplot.Trace;
import org.csstudio.swt.rtplot.TraceType;
import org.csstudio.swt.rtplot.data.PlotDataItem;
import org.csstudio.swt.rtplot.data.PlotDataProvider;
import org.csstudio.swt.rtplot.internal.util.ScreenTransform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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
    public void paint(final GC gc, final SWTMediaPool media, final Rectangle bounds, final ScreenTransform<XTYPE> x_transform, final YAxisImpl<XTYPE> y_axis, final Trace<XTYPE> trace)
    {
        x_min = bounds.x - OUTSIDE;
        x_max = bounds.x + bounds.width + OUTSIDE;
        y_min = bounds.y - OUTSIDE;
        y_max = bounds.y + bounds.height + OUTSIDE;

        final Color old_color = gc.getForeground();
        final Color old_bg = gc.getBackground();
        final int old_width = gc.getLineWidth();

        final Color color = media.get(trace.getColor());
        gc.setBackground(color);
        gc.setForeground(color);

        // TODO Use anti-alias?
        gc.setAdvanced(true);
        gc.setAntialias(SWT.ON);
        
        final PlotDataProvider<XTYPE> data = trace.getData();
        data.getLock().lock();
        final PlotDataProvider<XTYPE> reducedDataProvider = getReducedDataProvider(data, x_transform, y_axis);
        reducedDataProvider.getLock().lock();
        try
        {
            final TraceType type = trace.getType();
            switch (type)
            {
            case NONE:
                break;
            case AREA:
                gc.setAlpha(50);
            	drawMinMaxArea(gc, x_transform, y_axis, reducedDataProvider);
                gc.setAlpha(255);
                drawStdDevLines(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                drawValueStaircase(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                break;
            case AREA_DIRECT:
                gc.setAlpha(50);
                drawMinMaxArea(gc, x_transform, y_axis, reducedDataProvider);
                gc.setAlpha(255);
                drawStdDevLines(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                drawValueLines(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                break;
            case LINES:
                drawMinMaxLines(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                gc.setAlpha(50);
                drawStdDevLines(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                gc.setAlpha(255);
                drawValueStaircase(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                break;
            case LINES_DIRECT:
                drawMinMaxLines(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                gc.setAlpha(50);
                drawStdDevLines(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                gc.setAlpha(255);
                drawValueLines(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                break;
            case SINGLE_LINE:
                drawValueStaircase(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                break;
            case SINGLE_LINE_DIRECT:
                drawValueLines(gc, x_transform, y_axis, reducedDataProvider, trace.getWidth());
                break;
            }

            final PointType point_type = trace.getPointType();
            switch (point_type)
            {
            case NONE:
                break;
            case SQUARES:
            case CIRCLES:
            case DIAMONDS:
            case XMARKS:
            case TRIANGLES:
                drawPoints(gc, x_transform, y_axis, reducedDataProvider, point_type, trace.getPointSize());
                break;
            }
        }
        finally
        {
        	reducedDataProvider.getLock().unlock();
            data.getLock().unlock();
        }
        gc.setLineWidth(old_width);
        gc.setBackground(old_bg);
        gc.setForeground(old_color);
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
				if (Math.abs(x0 - x1) < REDUCTION_DISTANCE || Math.abs(y0 - y1) < REDUCTION_DISTANCE) {
					continue;
				}
				reducedList.add(nextItem);
				x0 = x1;
				y0 = y1;
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