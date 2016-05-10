Auto configuration library for grafana dashboard and graylog alert.

If you have questions or are working on a pull request or just
curious, please feel welcome to join the chat room:

## Overview

 - implemented in Java
 - monitor configuration file could be provided in three formats: Java properties, JSON, and a
   human-friendly HOCON (Human-Optimized Config Object Notation)
 - users can override the config with Java system properties,
    `-Dgrafana.port=10`
 - parses duration and size settings, "512k" or "10 seconds"
 - converts types, so if you ask for a boolean and the value
   is the string "yes", or you ask for a float and the value is
   an int, it will figure it out.

This library limits itself to grafana dashboard and graylog alert configuration. 
If you you are interested in extending it to support other platforms, pull requests are encouraged.

**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Essential Information](#essential-information)
  - [License](#license)
  - [Snapshot Releases](#snapshot-releases)
  - [Getting started](#getting-started)


## Essential Information

### License

The license is Apache 2.0, see LICENSE-2.0.txt.

### Snapshot Releases

Snapshot releases are available [Here](http://repo.wal-mart.com/content/repositories/snapshots/com/walmart/store/walmart-iasc/1.0-SNAPSHOT/)

You can find published releases on Nexus repository.

      <dependency>
          <groupId>com.walmart.store</groupId>
          <artifactId>walmart-iasc</artifactId>
          <version>1.0-SNAPSHOT</version>
      </dependency>


### Getting Started

After including the above dependency, you can start the auto dashboard creation and alert configuration using:
    
    LiquiMonitor.configure(true,true); //enable dashboard and start alert
    
    Assuming you have provided a monitor.conf file and a dashboard.json file as a resource the above code 
    will create and configure you grafana dashboard and also setups your application alerting based 
    on your graylog collected log data.
    
Sample Files: 
    [monitor.conf](src/main/resources/monitor.conf)
    [dashboard.json](src/main/resources/dashboard.json)