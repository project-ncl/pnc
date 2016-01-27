/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.processes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.logging.Logger;
import org.jboss.pnc.processes.util.TestUserTaskWorkItemHandler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RunWith(Parameterized.class)
public class ProcessTest {

	private static final Logger LOG = Logger.getLogger(ProcessTest.class);

	private KieSession ksession = null;

	private boolean patchRelease;
	private boolean buildInstaller;
	private boolean buildQuickstarts;
	private boolean buildMavenRepo;
	private boolean buildDockerImage;
	private boolean produceRPMs;
	private int expectedWorkItems;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				// Build all
				{Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, 17},
				// Build nothing
				{Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 11},
				// Build 2nd half
				{Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, 14},
				// Build 1st half
				{Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, 14},
		});
	}

	public ProcessTest(boolean patchRelease, boolean buildInstaller, boolean buildQuickstarts, boolean buildMavenRepo,
			boolean buildDockerImage, boolean produceRPMs, int expectedWorkItems) {

		this.patchRelease = patchRelease;
		this.buildInstaller = buildInstaller;
		this.buildQuickstarts = buildQuickstarts;
		this.buildMavenRepo = buildMavenRepo;
		this.buildDockerImage = buildDockerImage;
		this.produceRPMs = produceRPMs;
		this.expectedWorkItems = expectedWorkItems;
	}

	@Before
	public void setUpProcessTest() {
		ksession = KieServices.Factory.get().getKieClasspathContainer().newKieSession("JBossSession");
	}

	@Test
	public void shouldCompleteProcess() throws Exception {
		LOG.info(String
				.format("Executing process test with inputs: \n "
						+ "[Patch Release: %s] [Build Maven Repo: %s] [Build Installer: %s] [Build Quickstarts: %s] [Build Docker Image: %s] [Produce RPMs: %s]",
						patchRelease, buildMavenRepo, buildInstaller, buildQuickstarts, buildDockerImage, produceRPMs));

		// We use a dummy workitem handler for Human Tasks
		TestUserTaskWorkItemHandler workitemHandler = new TestUserTaskWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workitemHandler);

		// Prepare process inputs
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("patchRelease", this.patchRelease);
		params.put("buildInstaller", this.buildInstaller);
		params.put("buildQuickstarts", this.buildQuickstarts);
		params.put("buildMavenRepo", this.buildMavenRepo);
		params.put("buildDockerImage", this.buildDockerImage);
		params.put("produceRPMs", this.produceRPMs);

		// Start process
		ProcessInstance instance = ksession.startProcess("productReleaseQEHandoffProcess", params);
		assertNotNull(instance);
		assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());

		// Process all the tasks
		WorkItem item = workitemHandler.getWorkItem();
		while (item != null) {
			assertEquals(ProcessInstance.STATE_ACTIVE, instance.getState());
			ksession.getWorkItemManager().completeWorkItem(item.getId(), null);
			item = workitemHandler.getWorkItem();
		}

		// Process successfully completed
		assertEquals(ProcessInstance.STATE_COMPLETED, instance.getState());
		assertEquals("Number of executed work items does not match!", expectedWorkItems,
				workitemHandler.getExecutedWorkItems());
		LOG.info("Process successfully completed.");
	}

	@After
	public void tearDown() {
		ksession.dispose();
	}
}
