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
package org.jboss.pnc.processes.util;

import org.jboss.logging.Logger;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import java.util.ArrayList;
import java.util.List;

public class TestUserTaskWorkItemHandler implements WorkItemHandler {

	private static final Logger LOG = Logger.getLogger(TestUserTaskWorkItemHandler.class);
	private List<WorkItem> workItems = new ArrayList<WorkItem>();
	private int workItemcounter = 0;
	
	public void abortWorkItem(WorkItem item, WorkItemManager manager) {
		LOG.info("Aborting workitem: " + item);
		manager.abortWorkItem(item.getId());
		
	}

	public void executeWorkItem(WorkItem item, WorkItemManager manager) {
		LOG.info("Executing workitem: " + item);
		workItems.add(item);
		workItemcounter++;
	}

	public WorkItem getWorkItem() {
		if (workItems.size() == 0) {
			return null;
		}
		if (workItems.size() == 1) {
			WorkItem result = workItems.get(0);
			this.workItems.clear();
			return result;
		} else {
			throw new IllegalArgumentException("More than one work item active");
		}
	}

	public int getExecutedWorkItems() {
		return this.workItemcounter;
	}
}
