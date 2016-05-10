Name: remoteDashboardAdmin
Access Key: eyJrIjoicXhQUnNQSDA0N0FFV2ZNZVJaaWZhNEcxNzBxWnJpeDUiLCJuIjoicmVtb3RlRGFzaGJvYXJkQWRtaW4iLCJpZCI6MX0=

You will only be able to view this key here once! It is not stored in this form. So be sure to copy it now. 

You can authenticate request using the Authorization HTTP header, example: 

curl -H "Authorization: Bearer your_key_above" http://your.grafana.com/api/dashboards/db/mydash

apiuser
eyJrIjoiSHlnb0JQaVlualBBMURHMzJPamFhNHhIUXN3MWt4RXAiLCJuIjoiYXBpdXNlciIsImlkIjoxfQ==

GET /api/dashboards/db/locationservice HTTP/1.1
Host: graphite.graphite-dev2.in-node-location.rmnim.qa.cloud.wal-mart.com:3000
Authorization: Bearer eyJrIjoiSHlnb0JQaVlualBBMURHMzJPamFhNHhIUXN3MWt4RXAiLCJuIjoiYXBpdXNlciIsImlkIjoxfQ==
Cache-Control: no-cache
Postman-Token: 0440c45e-7335-7f18-dd02-61d6fa812f7b

OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
  .url("http://graphite.graphite-dev2.in-node-location.rmnim.qa.cloud.wal-mart.com:3000/api/dashboards/db/locationservice")
  .get()
  .addHeader("authorization", "Bearer eyJrIjoiSHlnb0JQaVlualBBMURHMzJPamFhNHhIUXN3MWt4RXAiLCJuIjoiYXBpdXNlciIsImlkIjoxfQ==")
  .addHeader("cache-control", "no-cache")
  .addHeader("postman-token", "a86a2718-5f26-0aa1-07c9-49d98030201e")
  .build();

Response response = client.newCall(request).execute();

## Alerting as a code:

Every Application should create its own stream in graylog following the steps listed below:

  - Get The list of all streams 
  - Loop through the streams and find out if the stream for the current application already exists
  - if stream does not exist, create the stream with the routing rules
  - update the alert settings 
  - update alert receiver
  - activate stream
  
Information about the alerting system should come from a local config supplied by the target application and ccm.

  - graylog username and password or authorization token
  - streamName or title, should be unique per application
  - Routing rules (every rule either ORed or ANDed)
  - alert setup
  - alert receiver
  
POST /streams HTTP/1.1
Host: graylog.graylog-dev.cps-graylog.rmnim.qa.cloud.wal-mart.com:12900
Content-Type: application/json
Authorization: Basic YWRtaW46d2FsbWFydC1ncmF5bG9n
Cache-Control: no-cache
Postman-Token: 7a9eb8ae-7a1d-39e3-0d85-73e6ef915d56

{
  "title": "Abiy Api test",
  "description": "error for container",
  "rules": [
    {
          "field": "message",
          "type": 2,
          "inverted": false,
          "value": " ERROR "
        }
  ],
  "content_pack": null,
  "matching_type": "OR"
}

