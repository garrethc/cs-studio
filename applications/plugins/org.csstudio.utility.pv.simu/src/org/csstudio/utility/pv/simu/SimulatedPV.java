package org.csstudio.utility.pv.simu;


/** Simulated PV.
 *  <p>
 *  Provides updates based on changes of the underlying DynamicValue.
 *  
 *  @author Kay Kasemir
 */
public class SimulatedPV extends BasicPV<DynamicValue>
{
    /** Initialize
     *  @param prefix PV type prefix
     *  @param name PV name
     */
    public SimulatedPV(final String prefix, final DynamicValue value)
    {
        super(prefix, value);
    }
    
    /** {@inheritDoc} */
    public synchronized void start() throws Exception
    {
        running = true;
        value.addListener(this);
        value.start();
    }

    /** {@inheritDoc} */
    public void stop()
    {
        value.removeListener(this);
        value.stop();
        running = false;
    }
}
