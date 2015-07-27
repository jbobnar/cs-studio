/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.opibuilder.converter.writer;

import org.csstudio.opibuilder.converter.EdmConverterTest;
import org.csstudio.opibuilder.converter.model.EdmException;
import org.csstudio.opibuilder.converter.model.EdmModel;

import junit.framework.TestCase;

public class OpiColorDefTest extends TestCase {

    public void testOpiColorDef() throws EdmException {

        System.setProperty("edm2xml.robustParsing", "false");
        System.setProperty("edm2xml.colorsFile", EdmConverterTest.COLOR_LIST_FILE);
        EdmModel.getInstance();

        OpiColorDef.writeDefFile(EdmModel.getColorsList(), EdmConverterTest.RESOURCES_LOCATION + "color.def");
    }
}
