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
