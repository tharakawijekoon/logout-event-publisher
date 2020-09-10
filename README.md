# logout-event-publisher
Event handler for publishing logout events of Identity server 5.10.0 to external parties. This Event handler would publish the logout events in the following format similar to the DASLoginDataPublisherImpl.

```
{
  "event": {
    "metaData": {
      "tenantId": "-1234"
    },
    "payloadData": {
      "contextId": "c2b97cdf6d12eb99776f24aaa921eecdf5264c599e57a1a63d83678449c2a444",
      "eventId": "c2b97cdf6d12eb99776f24aaa921eecdf5264c599e57a1a63d83678449c2a444",
      "eventType": "Logout",
      "authenticationSuccess": "false",
      "username": "tharaka",
      "localUserName": "tharaka",
      "userStoreDomain": "PRIMARY",
      "tenantDomain": "carbon.super",
      "remoteIp": "127.0.0.1",
      "region": "NOT_AVAILABLE",
      "inboundAuthType": "NOT_AVAILABLE",
      "serviceProvider": "oidcdebugger",
      "rememberMeEnabled": "false",
      "forceAuthEnabled": "NOT_AVAILABLE",
      "passiveAuthEnabled": "NOT_AVAILABLE",
      "rolesCommaSeparated": "NOT_AVAILABLE",
      "authenticationStep": "NOT_AVAILABLE",
      "identityProvider": "LOCAL",
      "authStepSuccess": "NOT_AVAILABLE",
      "stepAuthenticator": "NOT_AVAILABLE",
      "isFirstLogin": "NOT_AVAILABLE",
      "identityProviderType": "NOT_AVAILABLE",
      "_timestamp": "1599708689918"
    }
  }
}
```

# Build, Deploy & Run

## Build
Execute the following command to build the project

```mvn clean install```

## Deploy

Copy and place the built JAR artifact from the /target/org.wso2.custom.event.publishers.logouteventpublisher-1.0.0.jar to the <IS_HOME>/repository/components/dropins directory. Navigate to <IS_HOME>/repository/conf/deployment.toml and add the following configurations to register the new handler.
```
[[event_handler]]
name= "customSessionTerminateDataPublisher"
subscriptions =["SESSION_TERMINATE"]
[event_handler.properties]
enable = true
 ```
Setup the custom event stream. We will be using "org.wso2.is.custom.stream.SessionTerminateData:1.0.0" as the event stream name, so create a [org.wso2.is.custom.stream.SessionTerminateData_1.0.0.json](https://github.com/tharakawijekoon/logout-event-publisher/blob/master/src/main/resources/org.wso2.is.custom.stream.SessionTerminateData_1.0.0.json) file in the <IS_HOME>/repository/deployment/server/eventstreams/ directory. This file should include the stream definition,
```
{
  "name": "org.wso2.is.custom.stream.SessionTerminateData",
  "version": "1.0.0",
  "nickName": "",
  "description": "",
  "metaData": [
    {
      "name": "tenantId",
      "type": "INT"
    }
  ],
  "payloadData": [
    {
      "name": "contextId",
      "type": "STRING"
    },
    {
      "name": "eventId",
      "type": "STRING"
    },
    {
      "name": "eventType",
      "type": "STRING"
    },
    {
      "name": "authenticationSuccess",
      "type": "BOOL"
    },
    {
      "name": "username",
      "type": "STRING"
    },
    {
      "name": "localUserName",
      "type": "STRING"
    },
    {
      "name": "userStoreDomain",
      "type": "STRING"
    },
    {
      "name": "tenantDomain",
      "type": "STRING"
    },
    {
      "name": "remoteIp",
      "type": "STRING"
    },
    {
      "name": "region",
      "type": "STRING"
    },
    {
      "name": "inboundAuthType",
      "type": "STRING"
    },
    {
      "name": "serviceProvider",
      "type": "STRING"
    },
    {
      "name": "rememberMeEnabled",
      "type": "BOOL"
    },
    {
      "name": "forceAuthEnabled",
      "type": "BOOL"
    },
    {
      "name": "passiveAuthEnabled",
      "type": "BOOL"
    },
    {
      "name": "rolesCommaSeparated",
      "type": "STRING"
    },
    {
      "name": "authenticationStep",
      "type": "STRING"
    },
    {
      "name": "identityProvider",
      "type": "STRING"
    },
    {
      "name": "authStepSuccess",
      "type": "BOOL"
    },
    {
      "name": "stepAuthenticator",
      "type": "STRING"
    },
    {
      "name": "isFirstLogin",
      "type": "BOOL"
    },
    {
      "name": "identityProviderType",
      "type": "STRING"
    },
    {
      "name": "_timestamp",
      "type": "LONG"
    }
  ]
}
```
In the code, the [createPayload()](https://github.com/tharakawijekoon/logout-event-publisher/blob/master/src/main/java/org/wso2/custom/event/publishers/CustomEventPublisher.java#L100) method populates and returns an array of objects with the required attributes. The payloadData in the above definition will be populated from this array. you can customize the payload by changing the values to suit your requirements.

Add the HTTP event adaptor to publish events to the external party using the new custom event stream definition. create a file [External-Publisher-wso2event-Logout.xml](https://github.com/tharakawijekoon/logout-event-publisher/blob/master/src/main/resources/External-publisher-wso2event-Logout.xml) in <IS_HOME>/repository/deployment/server/eventpublishers/ directory and add the following content. you can change the http.url to the service which consumes these events published.

```
<?xml version="1.0" encoding="UTF-8"?>
<eventPublisher name="External-Publisher-wso2event-Logout"
  statistics="disable" trace="disable" xmlns="http://wso2.org/carbon/eventpublisher">
  <from streamName="org.wso2.is.custom.stream.SessionTerminateData" version="1.0.0"/>
  <mapping customMapping="disable" type="json"/>
  <to eventAdapterType="http">
        <property name="http.client.method">HttpPost</property>
        <property name="http.url">http://localhost:8281/services/wso2logout</property>
  </to>
</eventPublisher>
```


Restart the server
