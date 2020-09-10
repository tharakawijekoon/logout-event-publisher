package org.wso2.custom.event.publishers.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.custom.event.publishers.CustomEventPublisher;

@Component(
        name = "custom.session.termination.event.publisher",
        immediate = true
)
public class CustomEventPublisherServiceComponent {
    private static final Log log = LogFactory.getLog(CustomEventPublisherServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(AbstractEventHandler.class,
                    new CustomEventPublisher(), null);

            if (log.isDebugEnabled()) {
                log.debug("org.wso2.custom.event.publishers bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error while activating org.wso2.custom.event.publishers", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("org.wso2.custom.event.publishers bundle is deactivated");
        }
    }

    @Reference(
            name = "EventStreamService",
            service = EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService"
    )
    protected void setEventStreamService(EventStreamService eventStreamService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting the Event Stream Service");
        }
        CustomEventPublisherServiceHolder.getInstance().setPublisherService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {

        if (log.isDebugEnabled()) {
            log.debug("Un-setting the Event Stream Service");
        }
        CustomEventPublisherServiceHolder.getInstance().setPublisherService(null);
    }
}
