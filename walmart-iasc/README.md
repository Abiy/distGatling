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

