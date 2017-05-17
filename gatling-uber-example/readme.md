gatling-maven-plugin-demo
=========================
# Running as a java executable
    mvn clean package

Simple showcase of a maven project using the gatling-maven-plugin.
This will create an executable fat jar. Make sure that you have a the directory target/test-classes present from the location you are running the jar. Gatling seems to need this.

If there are muliple simulation file thensimply execute the following command:
java -jar -Dusers=5 -Dramp=1 -Dnodes=100 -Dlocs=100 -DsuccRate=100 -Durl=http://locations-toçmcat.dev.in-node-location.rmnim.dev.cloud.wal-mart.com:8080/ target/performance.gatling-2.1.7.jar

    $mvn gatling:execute -Dgatling.simulationClass=com.walmart.store.location.ci.PerformanceCi

If there is only one simulations file then do simply:

    $mvn gatling:execute

If passing all parameters then simply run

mvn gatling:execute -Dgatling.simulationClass=com.walmart.store.location.ci.PerformanceCi -Dusers=5 -Dramp=1 -Dnodes=100 -Dlocs=100 -DsuccRate=100 -Durl=http://locations-tomcat.dev.in-node-location.rmnim.dev.cloud.wal-mart.com:8080/

Note: Change then parameters accordingly
The properties in the file resources/application.properties can be overridden by passing it as system properties. The results of the test are located outside your target directory