package org.csstudio.swt.rtplot;

import java.util.Objects;

import org.csstudio.swt.rtplot.data.PlotDataItem;

/** 
 * 	Annotation that's displayed on a xAxis.
 * 
 *  @param <XTYPE> Data type used for the {@link PlotDataItem}
 * 
 * 	@author <a href="mailto:miha.novak@cosylab.com">Miha Novak</a>
 */
public class Timestamp <XTYPE extends Comparable<XTYPE>> {
	
	protected volatile XTYPE position;
	
	/**
	 * Constructor.
	 * 
	 * @param position timestamp position
	 */
	public Timestamp(final XTYPE position) {
		this.position = Objects.requireNonNull(position);
	}
	
	/**
	 * @return timestamp position.
	 */
	public XTYPE getPosition() {
		return position;
	}
	
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Timestamp on " + position;
    }
}