/*
 * Copyright (c) 2011 Stiftung Deutsches Elektronen-Synchrotron,
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
package org.csstudio.archive.common.service.mysqlimpl;

import javax.annotation.Nonnull;

import org.csstudio.archive.common.service.IArchiveRequestType;

/**
 * Archive request abstraction for optimized MySQL implementation.
 *
 * @author bknerr
 * @since 05.01.2011
 */
public enum ArchiveRequestType implements IArchiveRequestType {
    RAW("Raw values."),
    AVG_PER_MINUTE("Averaged over the time period of one minute."),
    AVG_PER_HOUR("Averaged over the time period of one hour.");

    private final String _desc;

    /**
     * Constructor.
     */
    private ArchiveRequestType(@Nonnull final String desc) {
        _desc = desc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getDescription() {
        return _desc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getTypeIdentifier() {
        return name();
    }

    /**
     * Returns the default/natural request type.
     * @return the default/natural request type for this implementation.
     */
    @Nonnull
    public static ArchiveRequestType getDefault() {
        return RAW;
    }
}
