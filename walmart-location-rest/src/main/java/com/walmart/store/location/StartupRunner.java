package com.walmart.store.location;

import com.walmart.store.location.data.LocationDataRepository;
import com.walmart.store.location.data.entities.Department;
import com.walmart.store.location.data.entities.Location;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.neo4j.template.Neo4jOperations;

import java.util.UUID;

public class StartupRunner implements CommandLineRunner {
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private Neo4jOperations neo4jOperations;
    @Autowired
    private LocationDataRepository locationDataRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Application Initializing...");


        //try (Transaction tx = neo4jOperations..beginTx()){
        //locationDataRepository.save(location1, 1);
        // tx.success();
        //}
        //run basic check to make sure the app is starting in a healthy state
    }
}