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

import com.cosylab.util.ListenerList;

import org.epics.css.dal.DataAccess;
import org.epics.css.dal.DataExchangeException;
import org.epics.css.dal.DynamicValueEvent;
import org.epics.css.dal.DynamicValueListener;
import org.epics.css.dal.EnumProperty;
import org.epics.css.dal.EnumSimpleProperty;
import org.epics.css.dal.PatternProperty;
import org.epics.css.dal.SequenceAccess;

import java.util.BitSet;


public abstract class AbstractDataAccessWrapper<T> implements DataAccess<T>
{
	protected static final int UNKNOWN = -1;
	protected DataAccess sourceDA;
	protected Class<T> valClass;
	protected ListenerList dvListeners = new ListenerList(DynamicValueListener.class);
	protected T lastValue;
	protected int conversion = UNKNOWN;

	public class DADynamicValueListener implements DynamicValueListener
	{
		private void fireEvent(DynamicValueEvent event, int id)
		{
			DynamicValueEvent newEvent = new DynamicValueEvent(AbstractDataAccessWrapper.this,
				    event.getProperty(),
				    convertFromOriginal(event.getValue(),
				        (DataAccess)event.getSource()), event.getCondition(),
				    event.getTimestamp(), event.getMessage(), event.getError(),
				    event.getEventID());

			DynamicValueListener[] listeners = (DynamicValueListener[])dvListeners
				.toArray();

			for (int i = 0; i < listeners.length; i++) {
				switch (id) {
				case 0:
					listeners[i].valueUpdated(newEvent);
					lastValue = (T)newEvent.getValue();

					break;

				case 1:
					listeners[i].valueChanged(newEvent);
					lastValue = (T)newEvent.getValue();

					break;

				case 2:
					listeners[i].timeoutStarts(newEvent);

					break;

				case 3:
					listeners[i].timeoutStops(newEvent);

					break;

				case 4:
					listeners[i].timelagStarts(newEvent);

					break;

				case 5:
					listeners[i].timelagStops(newEvent);

					break;

				case 6:
					listeners[i].errorResponse(newEvent);

					break;

				case 7:
					listeners[i].conditionChange(newEvent);

					break;
				}
			}
		}

		public void valueUpdated(DynamicValueEvent event)
		{
			fireEvent(event, 0);
		}

		public void valueChanged(DynamicValueEvent event)
		{
			fireEvent(event, 1);
		}

		public void timeoutStarts(DynamicValueEvent event)
		{
			fireEvent(event, 2);
		}

		public void timeoutStops(DynamicValueEvent event)
		{
			fireEvent(event, 3);
		}

		public void timelagStarts(DynamicValueEvent event)
		{
			fireEvent(event, 4);
		}

		public void timelagStops(DynamicValueEvent event)
		{
			fireEvent(event, 5);
		}

		public void errorResponse(DynamicValueEvent event)
		{
			fireEvent(event, 6);
		}

		public void conditionChange(DynamicValueEvent event)
		{
			fireEvent(event, 7);
		}
	}

	public AbstractDataAccessWrapper(Class<T> valClass, DataAccess sourceDA)
	{
		this.sourceDA = sourceDA;
		this.valClass = valClass;
		conversion = getConversion();

		if (conversion == UNKNOWN) {
			throw new IllegalArgumentException();
		}

		sourceDA.addDynamicValueListener(new DADynamicValueListener());
	}

	public void addDynamicValueListener(DynamicValueListener l)
	{
		dvListeners.add(l);
	}

	public void removeDynamicValueListener(DynamicValueListener l)
	{
		dvListeners.remove(l);
	}

	public DynamicValueListener[] getDynamicValueListeners()
	{
		return (DynamicValueListener[])dvListeners.toArray(new DynamicValueListener[dvListeners
		    .size()]);
	}

	public Class<T> getDataType()
	{
		return valClass;
	}

	public boolean isSettable()
	{
		return sourceDA.isSettable();
	}

	public void setValue(T value) throws DataExchangeException
	{
		Object newVal = convertToOriginal(value, sourceDA);

		if (newVal != null) {
			sourceDA.setValue(newVal);
		}
	}

	public T getValue() throws DataExchangeException
	{
		Object value = convertFromOriginal(sourceDA.getValue(), sourceDA);

		if (value != null) {
			return (T)value;
		}

		return null;
	}

	public T getLatestReceivedValue()
	{
		return lastValue;
	}

	protected abstract int getConversion();

	protected abstract Object convertToOriginal(Object value,
	    DataAccess dataAccess);

	protected abstract Object convertFromOriginal(Object value,
	    DataAccess dataAccess);
}

/* __oOo__ */
