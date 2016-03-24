## Gatling

Gatling is a highly capable load testing tool. It is designed for ease of use, maintainability and high performance.

Out of the box, Gatling comes with excellent support of the HTTP protocol that makes it a tool of choice for load testing any HTTP server. As the core engine is actually 
protocol agnostic, it is perfectly possible to implement support for other protocols. For example, Gatling currently also ships JMS support.

The Quickstart has an overview of the most important concepts, walking you through the setup of a simple scenario for load testing an HTTP server.

Having scenarios that are defined in code and are resource efficient are the two requirements that motivated us to create Gatling. Based on an expressive DSL, the scenarios
are self explanatory. They are easy to maintain and can be kept in a version control system.

Gatling’s architecture is asynchronous as long as the underlying protocol, such as HTTP, can be implemented in a non blocking way. 
This kind of architecture lets us implement virtual users as messages instead of dedicated threads, making them very resource cheap. Thus, running thousands of concurrent 
virtual users is not an issue tool. Development is currently focusing on HTTP support.

## Scaling Out
Sometimes, generating some very heavy load from a single machine might lead to saturating the OS or the network interface controller.

In this case, you might want to use several Gatling instances hosted on multiple machines.

Gatling doesn’t have a cluster mode yet, but you can achieve similar results manually:

## Distributed Gatling

Distributed Gatling is a walmart Technology in house solution to enable developers and QA engineers to run gatling performance tests in a distributed/cluster environment.  
The solution is cloud native and has two components, Cluster Master and Cluster Worker.

# Cluster Master (CM)

The Cluster Master  provides users interfaces and REST API's for basic operation related to running , tracking and consolidating performance reports,  CM is responsible for
    Handling user request
    Tracking and monitoring the health of Cluster Workers 
    Submitting Performance tasks to workers
    Tracking the progress of performance tasks
    Collecting and Aggregating performance reports
    Providing a system of records for  Performance Jobs history
    Maintaining repository for simulation and data files
   
Different projects could share the same cluster and run side by side on the same cluster with isolation

# Cluster Worker(CW)

After joining the cluster, CW workers are responsible for 
    Running performance tests
    Pulling Simulation and data  files from master
    Proving a streaming REST api's for error and std logs
    
   
    
## Usage

After unzipping the download bundle 
    Run master.sh to start the Cluster Master, take a note of the master ip and port
    Run worker.sh to start the Cluster worker for each node you intend to include to the cluster, each worker should be assigned the correct master contact point
    -Dakka.contact-points=akka.tcp://PerformanceSystem@10.165.150.249:2551/system/receptionist
