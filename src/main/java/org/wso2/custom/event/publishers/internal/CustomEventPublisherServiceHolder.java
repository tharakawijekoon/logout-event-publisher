package org.wso2.custom.event.publishers.internal;

import org.wso2.carbon.event.stream.core.EventStreamService;

public class CustomEventPublisherServiceHolder {
    private static CustomEventPublisherServiceHolder customEventPublisherServiceHolder =
            new CustomEventPublisherServiceHolder();
    private EventStreamService publisherService;

    private CustomEventPublisherServiceHolder() {

    }

    public static CustomEventPublisherServiceHolder getInstance() {

        return customEventPublisherServiceHolder;
    }

    public EventStreamService getPublisherService() {

        return publisherService;
    }

    public void setPublisherService(EventStreamService publisherService) {

        this.publisherService = publisherService;
    }
}
