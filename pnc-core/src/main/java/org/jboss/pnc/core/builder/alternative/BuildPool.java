package org.jboss.pnc.core.builder.alternative;


import org.jboss.pnc.model.BuildResult;
import org.jboss.pnc.model.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class BuildPool {

    final BlockingQueue<Future<BuildResult>> workLog = new LinkedBlockingQueue<>();
    final ExecutorService executorService;

    public BuildPool(int numberOfThreads) {
        executorService = Executors.newFixedThreadPool(numberOfThreads);
    }

    public BlockingQueue<Future<BuildResult>> submit(Function<Project, BuildResult> buildRecipe, Project... projects) {
        List<Project> projectsToBeBuilt = submitToBuild(Arrays.asList(projects), new ArrayList<>());
        projectsToBeBuilt.stream().forEach((project) -> {
            Future<BuildResult> buildResult = executorService.submit(() -> buildRecipe.apply(project));
            workLog.add(buildResult);
        });
        return workLog;
    }

    public void shutDown(long timeout, TimeUnit timeUnit){
        try {
            executorService.shutdown();
            executorService.awaitTermination(timeout, timeUnit);
        } catch (InterruptedException e) {
            // nothing we can do here - interrupting one thread above...
            Thread.currentThread().interrupt();
        }
    }

    private List<Project> submitToBuild(List<Project> projectsToBeScanned, List<Project> workingLog) {
        if(projectsToBeScanned.isEmpty()) {
            //eliminate duplicates
            return new ArrayList<>(new LinkedHashSet<>(workingLog));
        }
        projectsToBeScanned.stream()
                .forEach((project) -> workingLog.add(0, project));

        List<Project> newProjectToBeScanned = new ArrayList<>();
        for(Project project : projectsToBeScanned) {
            newProjectToBeScanned.addAll(project.getDependencies());
        }
        return submitToBuild(newProjectToBeScanned, workingLog);
    }
}
