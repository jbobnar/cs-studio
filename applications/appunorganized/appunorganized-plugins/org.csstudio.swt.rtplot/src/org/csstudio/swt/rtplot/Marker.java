package org.csstudio.swt.rtplot;

import java.util.Objects;

import org.csstudio.swt.rtplot.data.PlotDataItem;
import org.eclipse.swt.graphics.RGB;

/**
 * Annotation that's displayed on a xAxis.
 * 
 * @param <XTYPE> Data type used for the {@link PlotDataItem}
 * 
 * @author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class Marker<XTYPE extends Comparable<XTYPE>> {

    private static final int DEFAULT_LINE_WIDTH = 2;
    private static final RGB DEFAULT_RGB_COLOR = new RGB(0, 255, 0);

    private volatile XTYPE position;

    private RGB color;
    private int lineWidth;

    /**
     * Constructor.
     * 
     * @param position marker position
     */
    public Marker(final XTYPE position) {
        this.position = Objects.requireNonNull(position);
        lineWidth = DEFAULT_LINE_WIDTH;
        color = DEFAULT_RGB_COLOR;
    }

    /**
     * Sets marker position.
     * 
     * @param position marker position
     */
    public void setPosition(final XTYPE position) {
        this.position = position;
    }

    /**
     * @return marker position.
     */
    public XTYPE getPosition() {
        return position;
    }

    /**
     * Sets marker color.
     * 
     * @param color marker color
     */
    public void setColor(final RGB color) {
        this.color = color;
    }

    /**
     * @return marker color.
     */
    public RGB getColor() {
        return color;
    }

    /**
     * Sets marker line width.
     * 
     * @param lineWidth marker line width
     */
    public void setLineWidth(final int lineWidth) {
        this.lineWidth = lineWidth < 1 ? DEFAULT_LINE_WIDTH : lineWidth;
    }

    /**
     * @return marker line width.
     */
    public int getLineWidth() {
        return lineWidth;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Marker on " + position;
    }
}
