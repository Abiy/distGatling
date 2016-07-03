## Gatling

Gatling is a highly capable load testing tool. It is designed for ease of use, maintainability and high performance.

Out of the box, Gatling comes with excellent support of the HTTP protocol that makes it a tool of choice for load testing any HTTP server. As the core engine is actually 
protocol agnostic, it is perfectly possible to implement support for other protocols. For example, Gatling currently also ships JMS support.

The Quickstart has an overview of the most important concepts, walking you through the setup of a simple scenario for load testing an HTTP server.

Having scenarios that are defined in code and are resource efficient are the two requirements that motivated the development of Gatling. Based on an expressive DSL, the scenarios
are self explanatory. They are easy to maintain and can be kept in a version control system.

Gatling’s architecture is asynchronous as long as the underlying protocol, such as HTTP, can be implemented in a non blocking way. 
This kind of architecture lets us implement virtual users as messages instead of dedicated threads, making them very resource cheap. Thus, running thousands of concurrent 
virtual users is not an issue tool. 

## Scaling Out
Sometimes, generating some very heavy load from a single machine might lead to saturating the OS or the network interface controller.

In this case, you might want to use several Gatling instances hosted on multiple machines.

Gatling doesn’t have a cluster mode yet, but you can achieve similar results manually:

## Distributed Gatling

Distributed Gatling is a walmart Technology in house solution that was created to enable developers and QA engineers to run gatling simulation tests in a distributed/cluster environment.  
The solution is cloud native and has two components, Cluster Master and Cluster Worker.

# Cluster Master (CM)

The Cluster Master  provides users interfaces and REST API's for basic operation related to running , tracking and consolidating performance reports,  Master is also responsible for
    - Handling user request
    - Tracking and monitoring the health of Cluster Workers 
    - Submitting Gatling simulation tasks to distributed workers
    - Tracking the progress of distributed tasks
    - Collecting and Aggregating performance reports
    - Providing a system of records for  multiple simulation history
    - Maintaining repository for simulation and data files
   
Different projects could share the same cluster and run side by side on the same cluster with  complete isolation

# Cluster Worker(CW)

After joining the cluster, CW workers are responsible for 
    - Running performance tests
    - Pulling Simulation and data  files from the master
    - Proving a streaming REST end points for error logs , std logs and simulation logs
    
   
    
## Usage

After unzipping the download bundle 

    Run master.sh to start the Cluster Master, take a note of the master ip and port
        /bin/bash master.sh -Dmaster.port=<2551> -Dserver.port=<8080>
        

After starting the master using the above command, point your browser to GET http://localhost:8080/ to access the web page.

        
     Run agent.sh to start the Cluster worker for each node you intend to include to the cluster, each worker should be assigned the correct master contact point
        /bin/bash agent.sh -Dakka.contact-points=akka.tcp://PerformanceSystem@<MASTER_HOST>:<MASTER_PORT>/system/receptionist


##  Goal

The design of distributed gatling is based Derek Wyatt's blog on Work pulling pattern and has the following goals:
 
    - Provide a mechanism for the master to detect death of workers and reassign work if need be
    - Dynamic workers to allow for auto scaling, up/down
    - Message that is delivered to Master is durable

<img src="/images/pull_work.png" width="400" height="400" alt="Pull work model"/> 

Workers pull instructions from the master, the master keeps track of which instruction is consumed by which worker. The instructions could assume one of the following forms:
   
    - FileRequest
    - WorkRequest
    - AbortRequest
    - RegisterRequest
    
The master does not actively assign instructions to any worker but instead workers pull the next task when they are idle. If a worker is busy it will not be issued a new task.
Once the master assigns simulation task to a worker, workers are required to report task progress to master every predefined time interval. 
If the master does not receive a progress heart beat for simulation task, it assumes the worker is dead and reassigns the task to a different worker. 
In addition to marking the worker as dead and the job as failed, the master also removes the worker from the active list of workers. 
If the worker become alive after recovery it should re-join the cluster and re-register with the master.  


---

## Screen Shots 

---

# Thread metrics for the master
<img src="/images/landing.png" width="700" height="400" alt="Thread metrics for the master"/>

---

# Upload plugins, simulations files, data and conf files
<img src="/images/upload.png" width="700" height="400" alt="Upload plugins, simulations files, data and conf files"/>

---

# Start a distributed simulation task, provide worker pool to use, simulation to run and count of parallel tasks
<img src="/images/submit_simulation_job.png" width="700" height="400" alt="Start a distributed simulation task"/>

---

# Track the progress of a distributed simulation task on the cluster
<img src="/images/running_simulation_job.png" width="700" height="400" alt="Track progress"/>

---

# Cluster information - shows current state of all the workers in the cluster
<img src="/images/cluster_info.png" width="700" height="400" alt="Cluster info"/>