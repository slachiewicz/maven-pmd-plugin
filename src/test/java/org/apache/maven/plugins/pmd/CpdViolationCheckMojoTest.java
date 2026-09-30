/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.plugins.pmd;

import net.sourceforge.pmd.PMDVersion;
import org.apache.maven.api.plugin.MojoException;
import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version $Id$
 */
@MojoTest
public class CpdViolationCheckMojoTest {

    @Basedir("${basedir}/target/test-classes/unit/default-configuration")
    @InjectMojo(goal = "cpd-check", pom = "cpd-check-default-configuration-plugin-config.xml")
    @MojoParameter(name = "siteDirectory", value = "src/site")
    @MojoParameter(name = "targetDirectory", value = "cpd-report")
    @Test
    public void testDefaultConfiguration(CpdViolationCheckMojo mojo) throws Exception {
        try {
            mojo.execute();

            fail("MojoException should be thrown.");
        } catch (final MojoException e) {
            assertTrue(e.getMessage().startsWith("CPD " + PMDVersion.VERSION + " has found 1 duplicat"));
        }
    }

    @Basedir("${basedir}/target/test-classes/unit/default-configuration")
    @InjectMojo(goal = "cpd-check", pom = "cpd-check-notfailonviolation-plugin-config.xml")
    @MojoParameter(name = "siteDirectory", value = "src/site")
    @MojoParameter(name = "targetDirectory", value = "cpd-report")
    @Test
    public void testNotFailOnViolation(CpdViolationCheckMojo mojo) throws Exception {
        mojo.execute();
    }

    @Basedir("${basedir}/target/test-classes/unit/custom-configuration")
    @InjectMojo(goal = "cpd-check", pom = "cpd-check-exception-test-plugin-config.xml")
    @MojoParameter(name = "siteDirectory", value = "src/site")
    @MojoParameter(name = "targetDirectory", value = "cpd-report")
    @Test
    public void testException(CpdViolationCheckMojo mojo) throws Exception {
        try {
            mojo.execute();

            fail("MojoException should be thrown.");
        } catch (MojoException e) {
            assertTrue(e.getMessage().contains("Unable to perform check"));
        }
    }

    @Basedir("${basedir}/target/test-classes/unit/default-configuration")
    @InjectMojo(goal = "cpd-check", pom = "cpd-check-cpd-exclusions-configuration-plugin-config.xml")
    @MojoParameter(name = "siteDirectory", value = "src/site")
    @MojoParameter(name = "targetDirectory", value = "cpd-report")
    @Test
    public void testExclusionsConfiguration(CpdViolationCheckMojo mojo) throws Exception {
        // this call shouldn't throw an exception, as the classes with duplications have been excluded
        mojo.execute();
    }
}
