package org.csstudio.swt.rtplot;

import java.util.Objects;

public class Timestamp <XTYPE extends Comparable<XTYPE>> {
	
	protected volatile XTYPE position;
	
	public Timestamp(final XTYPE position) {
		this.position = Objects.requireNonNull(position);
	}
	
	public XTYPE getPosition() {
		return position;
	}
	
    @Override
    public String toString() {
        return "Timestamp on " + position;
    }
}