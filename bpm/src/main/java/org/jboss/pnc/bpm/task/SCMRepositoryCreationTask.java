package org.jboss.pnc.bpm.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.ToString;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.dto.internal.bpm.RepositoryCreationProcess;
import org.jboss.pnc.spi.exception.CoreException;

import java.io.Serializable;
import java.util.HashMap;

@ToString(callSuper = true)
public class SCMRepositoryCreationTask extends BpmTask {

    private final RepositoryCreationProcess repositoryCreationProcess;

    public SCMRepositoryCreationTask(RepositoryCreationProcess repositoryCreationProcess, String accessToken) {
        setAccessToken(accessToken);
        this.repositoryCreationProcess = repositoryCreationProcess;
    }

    @Override
    protected Serializable getProcessParameters() throws CoreException {
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("pncBaseUrl", config.getPncBaseUrl());
            params.put("repourBaseUrl", config.getRepourBaseUrl());
            params.put("taskData", MAPPER.writeValueAsString(repositoryCreationProcess));
            return params;
        } catch (JsonProcessingException e) {
            throw new CoreException("Could not get the parameters.", e);
        }
    }

    @Override
    protected String getProcessId() {
        return config.getBcCreationProcessId();
    }

}
