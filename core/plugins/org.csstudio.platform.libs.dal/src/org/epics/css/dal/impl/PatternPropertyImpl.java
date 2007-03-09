/*
 * Copyright (c) 2006 Stiftung Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */

package org.epics.css.dal.impl;

import com.cosylab.util.BitCondition;

import org.epics.css.dal.DataExchangeException;
import org.epics.css.dal.PatternProperty;
import org.epics.css.dal.PatternPropertyCharacteristics;
import org.epics.css.dal.context.PropertyContext;

import java.util.BitSet;


/**
 * Default implementation of PatternProperty
 *
 * @author $Author$
 * @version $Revision$
 */
public class PatternPropertyImpl extends DynamicValuePropertyImpl<BitSet>
	implements PatternProperty
{
	/**
	 * Creates a new PatternPropertyImpl object.
	 *
	 * @param name property name
	 * @param propertyContext property context
	 */
	public PatternPropertyImpl(String name, PropertyContext propertyContext)
	{
		super(BitSet.class, name, propertyContext);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.epics.css.dal.PatternSimpleProperty#getBitDescriptions()
	 */
	public String[] getBitDescriptions() throws DataExchangeException
	{
		return (String[])directoryProxy.getCharacteristic(PatternPropertyCharacteristics.C_BIT_DESCRIPTIONS);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.epics.css.dal.PatternSimpleProperty#getConditionWhenCleared()
	 */
	public BitCondition[] getConditionWhenCleared()
		throws DataExchangeException
	{
		return (BitCondition[])directoryProxy.getCharacteristic(PatternPropertyCharacteristics.C_CONDITION_WHEN_CLEARED);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.epics.css.dal.PatternSimpleProperty#getConditionWhenSet()
	 */
	public BitCondition[] getConditionWhenSet() throws DataExchangeException
	{
		return (BitCondition[])directoryProxy.getCharacteristic(PatternPropertyCharacteristics.C_CONDITION_WHEN_SET);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.epics.css.dal.PatternSimpleProperty#getBitMask()
	 */
	public BitSet getBitMask() throws DataExchangeException
	{
		return (BitSet)directoryProxy.getCharacteristic(PatternPropertyCharacteristics.C_BIT_MASK);
	}
} /* __oOo__ */


/* __oOo__ */
