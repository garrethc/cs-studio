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
package org.csstudio.alarm.treeView.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test
 * 
 * @author jpenning
 */
public class UrlValidatorTest {
    
    @Test
    public void testValidStringsWithProtocol() {
        Assert.assertEquals(UrlValidator.Result.URL_IS_OK, UrlValidator.checkUrl("file://bla"));
        Assert.assertEquals(UrlValidator.Result.URL_IS_OK, UrlValidator.checkUrl("file:bla"));
        Assert.assertEquals(UrlValidator.Result.URL_IS_OK, UrlValidator.checkUrl("http://www.desy.de"));
        Assert.assertEquals(UrlValidator.Result.URL_IS_OK, UrlValidator.checkUrl("http:desy"));
    }
    @Test
    public void testValidStringsWithoutProtocol() {
        Assert.assertEquals(UrlValidator.Result.URL_IS_OK, UrlValidator.checkUrl(""));
        Assert.assertEquals(UrlValidator.Result.URL_HAS_NO_PROTOCOL, UrlValidator.checkUrl("bla"));
    }
    
    @Test
    public void testInvalidStrings() {
        Assert.assertEquals(UrlValidator.Result.URL_INVALID, UrlValidator.checkUrl(null));
        Assert.assertEquals(UrlValidator.Result.URL_INVALID, UrlValidator.checkUrl("xttp:desy"));
    }
    
}