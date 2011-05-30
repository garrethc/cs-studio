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
package org.csstudio.domain.desy.softioc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.csstudio.domain.desy.junit.ConditionalClassRunner;
import org.csstudio.domain.desy.junit.OsCondition;
import org.csstudio.domain.desy.junit.RunIf;
import org.eclipse.core.runtime.FileLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link SoftIoc}.
 * 
 * @author bknerr
 * @since 27.05.2011
 */
@RunWith(ConditionalClassRunner.class)
@RunIf(conditionClass = OsCondition.class, arguments = {OsCondition.LINUX})
public class SoftIocHeadlessTest {

    private SoftIoc _softIoc;
    
    @Before
    public void setup() throws IOException, URISyntaxException {
        
        URL dbBundleResourceUrl = SoftIocHeadlessTest.class.getClassLoader().getResource("db/myTestDbFile.db");
        URL dbFileUrl = FileLocator.toFileURL(dbBundleResourceUrl);
        ISoftIocConfigurator cfg = new BasicSoftIocConfigurator().with(new File(dbFileUrl.getFile()));
        
        _softIoc = new SoftIoc(cfg);
        _softIoc.start();
    }
    
    @Test
    public void testMonitorSoftIoc() throws IOException, URISyntaxException {
        URL camExeUrl = FileLocator.toFileURL(SoftIocHeadlessTest.class.getClassLoader().getResource("win/camonitor.exe"));
        
        boolean killCaRepeater = !isCaRepeaterAlreadyRunning();
        
        Process cam = null;
        int noOfRuns = 0;
        try {
            cam = new ProcessBuilder(new File(camExeUrl.toURI()).toString(), "SoftIocTest:calc").start();
            BufferedReader input = new BufferedReader(new InputStreamReader(cam.getInputStream()));
            String line = null;
            while((line=input.readLine()) != null && noOfRuns < 5) {
                Assert.assertTrue(line.startsWith("SoftIocTest:calc"));
                noOfRuns++;
            }
            input.close();
        } finally {
            if (cam != null) {
                cam.destroy();
                Assert.assertEquals(Integer.valueOf(5), Integer.valueOf(noOfRuns));
            } else {
                Assert.fail("Monitor process could not be spawned.");
            }
            if (killCaRepeater) {
                new ProcessBuilder("taskkill.exe", "/f", "/im", "caRepeater.exe").start();
            }
        }
    }
    
    private boolean isCaRepeaterAlreadyRunning() throws IOException {
        Process taskList = new ProcessBuilder("tasklist.exe", "/fi", "IMAGENAME eq caRepeater.exe").start();
        
        BufferedReader input = new BufferedReader(new InputStreamReader(taskList.getInputStream()));
        boolean taskExists = false;
        String line;
        while((line=input.readLine()) != null) {
            if ("INFO: No tasks running with the specified criteria.".equals(line)) {
                break;
            } else if (line.startsWith("caRepeater.exe")) {
                taskExists = true;
                break;
            }
        }
        input.close();
        taskList.destroy();
        return taskExists;
    }

    @After
    public void teardown() throws IOException {
        _softIoc.stop();
    }
}
