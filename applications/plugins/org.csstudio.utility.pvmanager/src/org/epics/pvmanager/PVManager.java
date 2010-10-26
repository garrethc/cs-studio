/*
 * Copyright 2010 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.pvmanager;

/**
 * Manages the PV creation and scanning.
 *
 * @author carcassi
 */
public class PVManager {

    private static volatile ThreadSwitch defaultOnThread = ThreadSwitch.onTimerThread();
    private static volatile DataSource defaultDataSource = null;

    /**
     * Changes the default thread on which notifications are going to be posted.
     *
     * @param threadSwitch the new target thread
     */
    public static void setDefaultThread(ThreadSwitch threadSwitch) {
        defaultOnThread = threadSwitch;
    }

    /**
     * Changes the default source for data.
     *
     * @param dataSource the data source
     */
    public static void setDefaultDataSource(DataSource dataSource) {
        PVManager.defaultDataSource = dataSource;
    }

    /**
     * Reads the given expression. Will return the average of the values collected
     * at the scan rate.
     *
     * @param <T> type of the pv value
     * @param pvExpression the expression to read
     * @return a pv manager expression
     */
    public static <T> PVManagerExpression<T> read(SourceRateExpression<T> pvExpression) {
        return new PVManagerExpression<T>(ExpressionLanguage.latestValueOf(pvExpression));
    }

    /**
     * Reads the given expression.
     *
     * @param <T> type of the pv value
     * @param pvExpression the expression to read
     * @return a pv manager expression
     */
    public static <T> PVManagerExpression<T> read(DesiredRateExpression<T> pvExpression) {
        return new PVManagerExpression<T>(pvExpression);
    }

    /**
     * An expression used to set the final parameters on how the pv expression
     * should be monitored.
     * @param <T> the type of the expression
     */
    public static class PVManagerExpression<T>  {

        private DesiredRateExpression<T> aggregatedPVExpression;
        // Initialize to defaults
        private ThreadSwitch onThread = defaultOnThread;
        private DataSource source = defaultDataSource;

        private PVManagerExpression(DesiredRateExpression<T> aggregatedPVExpression) {
            this.aggregatedPVExpression = aggregatedPVExpression;
        }

        /**
         * Defines which DataSource should be used to read the data.
         *
         * @param dataSource a connection manager
         * @return this
         */
        public PVManagerExpression<T> from(DataSource dataSource) {
            if (dataSource == null)
                throw new IllegalArgumentException("dataSource can't be null");
            source = dataSource;
            return this;
        }

        /**
         * Defines on which thread the PVManager should notify the client.
         *
         * @param onThread the thread on which to notify
         * @return this
         */
        public PVManagerExpression<T> andNotify(ThreadSwitch onThread) {
            if (this.onThread == null)  {
                this.onThread = onThread;
            } else {
                throw new IllegalStateException("Already set what thread to notify");
            }
            return this;
        }

        /**
         * Sets the rate of scan of the expression and creates the actual {@link PV}
         * object that can be monitored through listeners.
         * @param rate rate in Hz; should be between 1 and 50
         * @return the PV
         */
        public PV<T> atHz(double rate) {
            long scanPeriodMs = (long) (1000.0 * (1.0 / rate));

            // Check that a data source has been specified
            if (source == null) {
                throw new RuntimeException("You need to specify a source either " +
                        "using PVManager.setDefaultDataSource or by using " +
                        "read(...).from(dataSource).");
            }

            // Create PV and connect
            PV<T> pv = PV.createPv(aggregatedPVExpression.getDefaultName());
            DataRecipe dataRecipe = aggregatedPVExpression.getDataRecipe();
            Function<T> aggregatedFunction = aggregatedPVExpression.getFunction();
            Notifier<T> notifier = new Notifier<T>(pv, aggregatedFunction, onThread);
            Scanner.scan(notifier, scanPeriodMs);
            source.connect(dataRecipe);
            PVRecipe recipe = new PVRecipe(dataRecipe, source, notifier);
            notifier.setPvRecipe(recipe);
            return pv;
        }
    }
}