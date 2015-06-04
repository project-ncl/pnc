package org.jboss.pnc.rest.endpoint;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.provider.BuildRecordProvider;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class BuildRecordEndpointTest {

    @Test
    public void getLogsNoContentTest() {
        int logId = 1;
        BuildRecordEndpoint buildRecordEndpoint = getLogsPrepareEndpoint(logId, "");

        // then
        assertEquals(204, buildRecordEndpoint.getLogs(logId).getStatus());
    }

    @Test
    public void getLogsWithContentTest() {
        int logId = 1;
        String logContent = "LOG CONTENT";
        BuildRecordEndpoint buildRecordEndpoint = getLogsPrepareEndpoint(logId, logContent);

        // then
        assertEquals(200, buildRecordEndpoint.getLogs(logId).getStatus());
    }

    private BuildRecordEndpoint getLogsPrepareEndpoint(int logId, String logContent) {
        // given
        BuildRecordRepository buildRecordRepository = mock(BuildRecordRepository.class);
        BuildRecordProvider buildRecordProvider = new BuildRecordProvider(buildRecordRepository, null);
        BuildRecordEndpoint buildRecordEndpoint = new BuildRecordEndpoint(buildRecordProvider, null);
        BuildRecord buildRecord = mock(BuildRecord.class);

        // when
        Mockito.when(buildRecord.getBuildLog()).thenReturn(logContent);
        Mockito.when(buildRecordRepository.findOne(logId)).thenReturn(buildRecord);
        return buildRecordEndpoint;
    }
}
