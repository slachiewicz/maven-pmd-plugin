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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.api.plugin.MojoException;
import org.apache.maven.api.plugin.annotations.Mojo;
import org.apache.maven.api.plugin.annotations.Parameter;
import org.apache.maven.plugins.pmd.model.PmdErrorDetail;
import org.apache.maven.plugins.pmd.model.PmdFile;
import org.apache.maven.plugins.pmd.model.Violation;
import org.apache.maven.plugins.pmd.model.io.xpp3.PmdXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Fails the build if there were any PMD violations in the source code.
 *
 * <p>This goal only reads the {@code pmd.xml} result file in {@code targetDirectory}. It no longer forks
 * the {@code pmd} report goal, so that file must have been produced earlier in the build.
 *
 * @since 2.0
 */
@Mojo(name = "check", defaultPhase = "verify")
public class PmdViolationCheckMojo extends AbstractPmdViolationCheckMojo<Violation> {
    /**
     * Default constructor. Initializes with the correct {@link ExcludeViolationsFromFile}.
     */
    public PmdViolationCheckMojo() {
        super(new ExcludeViolationsFromFile());
    }

    /**
     * What priority level to fail the build on.
     * PMD violations are assigned a priority from 1 (most severe) to 5 (least severe) according
     * to the rule's priority.
     * Violations at or less than this priority level are considered failures and will fail
     * the build if {@code failOnViolation=true} and the count exceeds {@code maxAllowedViolations}.
     * The other violations will be regarded as warnings and will be displayed in the build output
     * if {@code verbose=true}.
     * Setting a value of 5 will treat all violations as failures, which may cause the build to fail.
     * Setting a value of 1 will treat all violations as warnings.
     * Only values from 1 to 5 are valid.
     */
    @Parameter(property = "pmd.failurePriority", defaultValue = "5", required = true)
    private int failurePriority = 5;

    /**
     * Skip the PMD checks. Most useful on the command line via "-Dpmd.skip=true".
     */
    @Parameter(property = "pmd.skip", defaultValue = "false")
    private boolean skip;

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoException {
        if (skip) {
            getLog().info("Skipping PMD execution");
            return;
        }

        executeCheck("pmd.xml", "PMD", "violation", failurePriority);
    }

    /**
     * {@inheritDoc}
     */
    protected void printError(Violation item, String severity) {

        StringBuilder buff = new StringBuilder(100);
        buff.append("PMD ").append(severity).append(": ");
        if (item.getViolationClass() != null) {
            if (item.getViolationPackage() != null) {
                buff.append(item.getViolationPackage());
                buff.append(".");
            }
            buff.append(item.getViolationClass());
        } else {
            buff.append(item.getFileName());
        }
        buff.append(":");
        buff.append(item.getBeginline());
        buff.append(" Rule:").append(item.getRule());
        buff.append(" Priority:").append(item.getPriority());
        buff.append(" ").append(item.getText()).append(".");

        this.getLog().warn(buff.toString());
    }

    @Override
    protected List<Violation> getErrorDetails(File pmdFile) throws XmlPullParserException, IOException {
        try (InputStream in = new FileInputStream(pmdFile)) {
            PmdXpp3Reader reader = new PmdXpp3Reader();
            PmdErrorDetail details = reader.read(in, false);
            List<Violation> violations = new ArrayList<>();
            for (PmdFile file : details.getFiles()) {
                String fullPath = file.getName();

                for (Violation violation : file.getViolations()) {
                    violation.setFileName(getFilename(fullPath, violation.getViolationPackage()));
                    violations.add(violation);
                }
            }
            return violations;
        }
    }

    @Override
    protected int getPriority(Violation errorDetail) {
        return errorDetail.getPriority();
    }

    @Override
    protected ViolationDetails<Violation> newViolationDetailsInstance() {
        return new ViolationDetails<>();
    }

    private String getFilename(String fullpath, String pkg) {
        int index = fullpath.lastIndexOf(File.separatorChar);

        while (pkg != null && !pkg.isEmpty()) {
            index = fullpath.substring(0, index).lastIndexOf(File.separatorChar);

            int dot = pkg.indexOf('.');

            if (dot < 0) {
                break;
            }
            pkg = pkg.substring(dot + 1);
        }

        return fullpath.substring(index + 1);
    }
}
