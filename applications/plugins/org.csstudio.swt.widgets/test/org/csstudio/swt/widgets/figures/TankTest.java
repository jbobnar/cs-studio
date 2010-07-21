package org.csstudio.swt.widgets.figures;
import org.eclipse.draw2d.Figure;


public class TankTest extends AbstractMarkedWidgetTest{

	@Override
	public Figure createTestWidget() {		
		return new TankFigure();
	}
	
	
	@Override
	public String[] getPropertyNames() {
		String[] superProps =  super.getPropertyNames();
		String[] myProps = new String[]{
				"fillColor",
				"fillBackgroundColor",
				"effect3D"
		};
		
		return concatenateStringArrays(superProps, myProps);
	}
	
	@Override
	public boolean isAutoTest() {
		return true;
	}

	
		
}