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

package org.epics.css.dal.simulation;

import org.epics.css.dal.RemoteException;
import org.epics.css.dal.context.AbstractApplicationContext;
import org.epics.css.dal.context.Connectable;
import org.epics.css.dal.context.ConnectionException;
import org.epics.css.dal.context.ConnectionState;
import org.epics.css.dal.context.DeviceFamily;
import org.epics.css.dal.context.LinkListener;
import org.epics.css.dal.context.PlugContext;
import org.epics.css.dal.context.RemoteInfo;
import org.epics.css.dal.device.AbstractDevice;
import org.epics.css.dal.impl.AbstractDeviceImpl;
import org.epics.css.dal.impl.DeviceBean;
import org.epics.css.dal.impl.DeviceFamilyImpl;
import org.epics.css.dal.proxy.AbstractPlug;
import org.epics.css.dal.proxy.DeviceProxy;
import org.epics.css.dal.proxy.DirectoryProxy;
import org.epics.css.dal.spi.AbstractDeviceFactory;
import org.epics.css.dal.spi.DeviceFactory;
import org.epics.css.dal.spi.LinkPolicy;

import javax.naming.directory.DirContext;


/**
 * Device factory implementation
 *
 * @author $Author$
 * @version $Revision$
  */
public class DeviceFactoryImpl extends AbstractDeviceFactory
{
	/**
	 * Creates a new DeviceFactoryImpl object.
	 */
	public DeviceFactoryImpl()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.epics.css.dal.spi.AbstractFactorySupport#getPlugClass()
	 */
	@Override
	protected Class<?extends AbstractPlug> getPlugClass()
	{
		return SimulatorPlug.class;
	}
}

/* __oOo__ */
