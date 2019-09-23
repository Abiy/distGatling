## Usage on Kubernetes

Download Gatling bundle as a .zip file [here](http://gatling.io/#/resources/download). Unzip the file in a directory of your choosing. Add a cancel.sh command in the bin directory, suitable for your system, that stops the gatling processes on the workers (example:  `ps ax | grep "uploads$1" | grep -v grep | awk '{print $1}' | xargs kill -9` )

After cloning or downloading the repository of distGatling ,follow the following steps to start the cluster:

Existing names for every component are:

| Component                             | Name                                            |
|---------------------------------------|-------------------------------------------------|
| kubernetes namespace                  | dist-gatling                                    |
| gatling docker image name             | gatling:latest                                  |
| dist gatling master docker image name | gatling-master:v1                               |
| dist gatling master deployment name   | gatling-master                                  |
| dist gatling mater service name       | gatling-master                                  |
| dist gatling worker docker image name | gatling-worker:v1                               |
| graphite docker image name            | graphite:latest                                 |
| graphite deployment name              | graphite-service                                |
| graphite service host                 | graphite-service.dist-gatling.svc.cluster.local |
| grafana docker image name             | grafana/grafana:latest                          |
| grafana deployment name               | grafana-service                                 |
| role name                             | gatling-role                                    |
| rolebinding name                      | gatling-role-binding                            |
| service account name                  | gatling-service-account                         |

This name can be change according to the next steps:

1.Build a docker image for gatling
* for metrics, enable them from gatling.conf(file can be found in gatling bundle):
```
   data {
       writers = [console, file, graphite]      # The list of DataWriters to which Gatling write simulation data (currently supported : console, file, graphite, jdbc)
       console {
           #light = false                # When set to true, displays a light version without detailed request stats
           #writePeriod = 5              # Write interval, in seconds
       }
       file {
           #bufferSize = 8192            # FileDataWriter's internal data buffer size, in bytes
       }
       leak {
           #noActivityTimeout = 30  # Period, in seconds, for which Gatling may have no activity before considering a leak may be happening
       }
       graphite {
           light = false              # only send the all* stats
           host = "graphite-service.dist-gatling.svc.cluster.local"         # The host where the Carbon server is located
           port = 2003                # The port to which the Carbon server listens to (2003 is default for plaintext, 2004 is default for pickle)
           protocol = "tcp"           # The protocol used to send data to Carbon (currently supported : "tcp", "udp")
           rootPathPrefix = "gatling" # The common prefix of all metrics sent to Graphite
           bufferSize = 8192          # Internal data buffer size, in bytes
           writePeriod = 1            # Write period, in seconds
       }
   }
```

* change host according to the location of your graphite service

* compress gatling bundle folder and name it : gatling-charts-highcharts-bundle.zip

* run Dockerfile-gatling to build the image:
```
   docker buid -t <GATLING_IMAGE_NAME>:<TAG> -f Dockerfile-gatling .
```

2.(OPTIONAL - JUST FOR METRICS) Build and deploy a Graphite image
   * get a graphite docker image
     * option A: get it from [here](https://hub.docker.com/r/graphiteapp/docker-graphite-statsd/)
     * option B: build it from official git repo [here](https://github.com/graphite-project/docker-graphite-statsd)
   * create a deployment with graphite-pod.yaml:
       - change graphite image name according to your built images
       - run: ` kubectl create -f graphite-pod.yaml `
   * expose it as a service

3.(OPTIONAL - JUST FOR METRICS) Build and deploy a Grafana image
   * if you already have a grafana service, this step is not necessary
   * get a grafana docker image [here](https://hub.docker.com/r/grafana/grafana/)
   * create a deployment with grafana-pod.yaml:
       * change grafana image name according to your built images
       * run: `kubectl create -f grafana-pod.yaml`
   * expose it as a service

4.Build dist-gatling
   * before building, go to gatling-rest/src/main/resources/worker-pod.yaml and change image name with 
   ```
   <GATLING_WORKER_DOCKER_IMAGE_NAME>:<TAG>
   ```
   * run: ` mvn clean package `

5.Build docker images for master and workers
* for master :
   * if you build the gatling image with other name, change it on the first line in Dockerfile-master
   * set the following environment variables:

   | Environment variable | Description                                     |
   |----------------------|-------------------------------------------------|
   | GRAPHITE_ENABLE      | true -> enable metrics false -> disable metrics |
   | GRAPHITE_NAME        | name to for graphite deployment                 |

   * change external host in dockerfile after this template : 
   ```
   -DEXTERNAL_HOST=<GATLING_MASTER_SERVICE_MANE>.<KUBERNETES_NAMESPACE>.svc.cluster.local
   ```

* for worker :
   * if you build the gatling image with other name, change it on the first line in Dockerfile-worker
   * set the following environment variables(must have the same values as in Dockerfile-master):

   | Environment variable | Description                                     |
   |----------------------|-------------------------------------------------|
   | GRAPHITE_ENABLE      | true -> enable metrics false -> disable metrics |
   | GRAPHITE_NAME        | name to for graphite deployment                 |

   * set ARG MASTER_SERVER with corresponding value for your master(same as in Dockerfile-master for -DEXTERNAL_HOST)

* For building the images run:

```
   docker build -t <GATLING_MASTER_DOCKER_IMAGE_NAME>:<TAG> -f Dockerfile-master .
   docker build -t <GATLING_WORKER_DOCKER_IMAGE_NAME>:<TAG> -f Dockerfile-worker .
```

6.Set Kubernetes environment:
   * create a namespace with the same name as the environment variables set in Dockerfiles(namespace.yaml can be used):
   
       `kubectl create -f namespace.yaml`
   * create a service account (serviceAccount.yaml can be used):
   
       `kubectl create -f serviceAccount.yaml`
   * create a role and a rolebinding (role.yaml and roleBinding.yaml can be used):
       ```
       kubectl create -f role.yaml
       kubectl create -f roleBinding.yaml
       ```
       
7.Create pod for master:
   * in master-pod.yaml change:
       * image name should be <GATLING_MASTER_DOCKER_IMAGE_NAME>:<TAG>
   * for creating the deployment, run:
       `kubectl create -f master-pod.yaml`
       
8.Expose deployment as service

9.Access gatling-master service from your browser
