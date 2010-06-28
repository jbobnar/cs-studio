package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.ColorProperty;
import org.csstudio.opibuilder.properties.IntegerProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;
import org.eclipse.swt.graphics.RGB;

/**
 * @author hammonds
 *
 */
public class ByteMonitorModel extends AbstractPVWidgetModel {

	/**
	 * The ID of this widget model.
	 */
	public static final String ID = "org.csstudio.opibuilder.widgets.bytemonitor"; //$NON-NLS-1$

	/** The number of bits to display */
	public static final String PROP_NUM_BITS = "numBits";

	/** The bit number to start displaying */
	public static final String PROP_START_BIT = "startBit";

	/** The ID of the horizontal property. */
	public static final String PROP_HORIZONTAL = "horizontal"; //$NON-NLS-1$

	/** Reverse the direction that bytes are displayed normal display is start bit on right or bottom*/
	public static final String PROP_BIT_REVERSE = "bitReverse";
	
	/** Default color if the bit is on */
	public static final String PROP_ON_COLOR = "on_color";

	/** Default color if the bit is off */
	public static final String PROP_OFF_COLOR = "off_color";

	/** The ID of the square LED property. */
	public static final String PROP_SQUARE_LED = "square_led"; //$NON-NLS-1$
	
	/** The default color of the on color property. */
	private static final RGB DEFAULT_ON_COLOR = new RGB(0,255,0);
	/** The default color of the off color property. */
	private static final RGB DEFAULT_OFF_COLOR = new RGB(0, 100 ,0);

	/** The ID of the effect 3D property. */
	public static final String PROP_EFFECT3D = "effect_3d"; //$NON-NLS-1$
	

	public ByteMonitorModel() {
		setSize(292, 20);
	}
	
	/* (non-Javadoc)
	 * @see org.csstudio.opibuilder.model.AbstractWidgetModel#configureProperties()
	 */
	@Override
	protected void configureProperties() {
		addProperty(new IntegerProperty(PROP_NUM_BITS, "Number of bits", 
				WidgetPropertyCategory.Display, 16, 0, 64));
		addProperty(new IntegerProperty(PROP_START_BIT, "Start Bit", 
				WidgetPropertyCategory.Display, 0, 0, 64));
		addProperty(new BooleanProperty(PROP_HORIZONTAL, "Horizontal", 
				WidgetPropertyCategory.Display, true));	
		addProperty(new BooleanProperty(PROP_BIT_REVERSE, "Reverse Bits", 
		WidgetPropertyCategory.Display, false));
		addProperty(new ColorProperty(PROP_ON_COLOR, "On Color",
				WidgetPropertyCategory.Display, DEFAULT_ON_COLOR));
		addProperty(new ColorProperty(PROP_OFF_COLOR, "Off Color",
				WidgetPropertyCategory.Display, DEFAULT_OFF_COLOR));		
		addProperty(new BooleanProperty(PROP_SQUARE_LED, "Square LED", 
				WidgetPropertyCategory.Display, false));
		addProperty(new BooleanProperty(PROP_EFFECT3D, "3D Effect", 
				WidgetPropertyCategory.Display, true));

	}

	/* (non-Javadoc)
	 * @see org.csstudio.opibuilder.model.AbstractWidgetModel#getTypeID()
	 */
	@Override
	public String getTypeID() {
		return ID;
	}

}