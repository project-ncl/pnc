package org.jboss.pnc.rest.notifications.model;

import org.jboss.pnc.core.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.junit.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NotificationFactoryTest {

    @Test
    public void shouldhaveProperListOfExternalEvents() throws Exception {
        //given
        EnumSet<BuildStatus> statuses = EnumSet.of(BuildStatus.REPO_SETTING_UP, BuildStatus.BUILD_COMPLETED_SUCCESS, BuildStatus.BUILD_COMPLETED_WITH_ERROR);
        NotificationFactory notificationFactory = new NotificationFactory();

        for(BuildStatus status : statuses) {
            //when
            boolean isExternal = notificationFactory.isExternal(status);

            //then
            assertTrue(isExternal);
        }
    }

    @Test
    public void shouldHaveProperListOfInternalEvents() throws Exception {
        //given
        EnumSet<BuildStatus> excludedStatuses = EnumSet.of(BuildStatus.REPO_SETTING_UP, BuildStatus.BUILD_COMPLETED_SUCCESS, BuildStatus.BUILD_COMPLETED_WITH_ERROR);
        EnumSet<BuildStatus> statuses = EnumSet.complementOf(excludedStatuses);
        NotificationFactory notificationFactory = new NotificationFactory();

        for(BuildStatus status : statuses) {
            //when
            boolean isExternal = notificationFactory.isExternal(status);

            //then
            assertFalse(isExternal);
        }
    }

    @Test
    public void shouldConvertSuccessfulNotificationEvent() throws Exception {
        //given
        BuildStatusChangedEvent event = new DefaultBuildStatusChangedEvent(BuildStatus.NEW, BuildStatus.BUILD_COMPLETED_SUCCESS, 1, 1);
        NotificationFactory notificationFactory = new NotificationFactory();

        //when
        Notification notification = notificationFactory.createNotification(event);

        //then
        assertThat(notification.getExceptionMessage()).isNull();
        assertThat(notification.getPayload()).isNotNull();
        assertThat(notification.getPayload().getEventType()).isEqualTo(NotificationEventType.BUILD_COMPLETED);
        assertThat(notification.getPayload().getId()).isEqualTo(1);
        assertThat(notification.getPayload().getUserId()).isEqualTo(1);
    }
}