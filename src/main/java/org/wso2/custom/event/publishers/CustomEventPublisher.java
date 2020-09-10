package org.wso2.custom.event.publishers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.handler.AbstractIdentityMessageHandler;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.data.publisher.application.authentication.AuthPublisherConstants;
import org.wso2.carbon.identity.data.publisher.application.authentication.AuthnDataPublisherUtils;
import org.wso2.carbon.identity.data.publisher.authentication.analytics.session.model.SessionData;
import org.wso2.carbon.identity.data.publisher.authentication.analytics.session.SessionDataPublisherUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.custom.event.publishers.internal.CustomEventPublisherServiceHolder;

import java.util.Arrays;

public class CustomEventPublisher extends AbstractEventHandler {

    private static final String CUSTOM_STREAM_NAME = "org.wso2.is.custom.stream.SessionTerminateData:1.0.0";
    private static final String CUSTOM_DATA_PUBLISHER_ENABLED = "customSessionTerminateDataPublisher.enable";
    private static final String CUSTOM_EVENT_PUBLISHER = "customSessionTerminateDataPublisher";
    private static final int SESSION_TERMINATION_STATUS = 0;
    private static Log LOG = LogFactory.getLog(CustomEventPublisher.class);

    @Override
    public String getName() {

        return CUSTOM_EVENT_PUBLISHER;
    }

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        boolean isEnabled = isCustomSessionDataPublishingEnabled(event);

        if (!isEnabled) {
            return;
        }

        SessionData sessionData = SessionDataPublisherUtil.buildSessionData(event);
        //take session termination event as a logout event
        if (IdentityEventConstants.EventName.SESSION_TERMINATE.name().equals(event.getEventName())) {
            doPublishSessionTermination(sessionData);
        } else {
            LOG.error("Event " + event.getEventName() + " cannot be handled");
        }
    }

    protected void doPublishSessionTermination(SessionData sessionData) {

        publishSessionData(sessionData, SESSION_TERMINATION_STATUS);

    }

    protected void publishSessionData(SessionData sessionData, int actionId) {

        SessionDataPublisherUtil.updateTimeStamps(sessionData, actionId);
        try {
            Object[] payloadData = createPayload(sessionData);
            publishToExternal(sessionData, payloadData);

        } catch (IdentityRuntimeException e) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Error while publishing session information", e);
            }
        }

    }

    private void publishToExternal(SessionData sessionData, Object[] payloadData) {

        String[] publishingDomains = (String[]) sessionData.getParameter(AuthPublisherConstants.TENANT_ID);
        if (publishingDomains != null && publishingDomains.length > 0) {
            try {
                FrameworkUtils.startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                for (String publishingDomain : publishingDomains) {
                    Object[] metadataArray = AuthnDataPublisherUtils.getMetaDataArray(publishingDomain);
                    org.wso2.carbon.databridge.commons.Event event =
                            new org.wso2.carbon.databridge.commons.Event(CUSTOM_STREAM_NAME, System.currentTimeMillis(),
                                    metadataArray, null, payloadData);
                    CustomEventPublisherServiceHolder.getInstance().getPublisherService().publish(event);
                    if (LOG.isDebugEnabled() && event != null) {
                        LOG.debug("Sending out to publishing domain:" + publishingDomain + " \n event : "
                                + event.toString());
                    }
                }
            } finally {
                FrameworkUtils.endTenantFlow();
            }
        }
    }

    private Object[] createPayload(SessionData sessionData) {

        Object[] payloadData = new Object[23];
        payloadData[0] = sessionData.getSessionId();
        payloadData[1] = sessionData.getSessionId();
        payloadData[2] = "Logout";
        payloadData[3] = "false";
        payloadData[4] = AuthnDataPublisherUtils.replaceIfNotAvailable(AuthPublisherConstants.CONFIG_PREFIX +
                AuthPublisherConstants.USERNAME, sessionData.getUser());
        payloadData[5] = AuthnDataPublisherUtils.replaceIfNotAvailable(AuthPublisherConstants.CONFIG_PREFIX +
                AuthPublisherConstants.USERNAME, sessionData.getUser());
        payloadData[6] = AuthnDataPublisherUtils.replaceIfNotAvailable(AuthPublisherConstants.CONFIG_PREFIX +
                AuthPublisherConstants.USER_STORE_DOMAIN, sessionData.getUserStoreDomain());
        payloadData[7] = sessionData.getTenantDomain();
        payloadData[8] = sessionData.getRemoteIP();
        payloadData[9] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[10] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[11] = AuthnDataPublisherUtils.replaceIfNotAvailable(AuthPublisherConstants.CONFIG_PREFIX +
                AuthPublisherConstants.SERVICE_PROVIDER, sessionData.getServiceProvider());
        payloadData[12] = sessionData.isRememberMe();
        payloadData[13] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[14] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[15] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[16] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[17] = AuthnDataPublisherUtils.replaceIfNotAvailable(AuthPublisherConstants.CONFIG_PREFIX +
                AuthPublisherConstants.IDENTITY_PROVIDER, sessionData.getIdentityProviders());
        payloadData[18] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[19] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[20] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[21] = AuthPublisherConstants.NOT_AVAILABLE;
        payloadData[22] = System.currentTimeMillis();


        if (LOG.isDebugEnabled()) {
            LOG.debug("The created payload :" + Arrays.asList(payloadData));
        }
        return payloadData;
    }

    @Override
    public boolean isEnabled(MessageContext messageContext) {
        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (AbstractIdentityMessageHandler.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return false;
        }

        return Boolean.parseBoolean(identityEventListenerConfig.getEnable());
    }

    private boolean isCustomSessionDataPublishingEnabled(Event event) throws IdentityEventException {

        if (this.configs.getModuleProperties() != null) {
            String handlerEnabled = this.configs.getModuleProperties().getProperty(CUSTOM_DATA_PUBLISHER_ENABLED);
            return Boolean.parseBoolean(handlerEnabled);
        }

        return false;
    }

}
