## gatling-maven-plugin-demo
=========================
### Running as a java executable
    mvn clean package

Simple showcase of a maven project using the gatling-maven-plugin.
This will create an executable fat jar. Make sure that you have a the directory target/test-classes present from the location you are running the jar. Gatling seems to need this.

Then simply execute the following command:


    $mvn gatling:execute -DsimulationClass=com.walmart.store.gatling.simulation.BasicSimulation

If there is only one simulations file then do:

    $mvn gatling:execute

If you have  parameters then run:

mvn gatling:execute -DsimulationClass=com.walmart.store.gatling.simulation.BasicSimulation -Dusers=5 -Dramp=1

Note: Change then parameters accordingly
The properties in the file resources/application.properties can be overridden by passing it as system properties. 
The results of the test are located outside your target directory.