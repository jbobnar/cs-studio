/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.pv.simu;

/** Dynamic value that produces noise waveform
 *  @author Xihui Chen
 */
public class NoiseWaveformValue extends DynamicValue
{
    /** Initialize
     *  @param name
     */
    public NoiseWaveformValue(final String name)
    {
        super(name);
    }

    /** {@inheritDoc} */
    @Override
    protected void update()
    {    int arraySize = (int) step;
        double[] doubleArray = new double[arraySize];
        for(int i=0; i<arraySize; i++){
            doubleArray[i] = min + (Math.random() * (max - min));
        }
        setValue(doubleArray);
    }
}
