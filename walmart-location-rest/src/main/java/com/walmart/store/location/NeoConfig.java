package com.walmart.store.location;

import com.walmart.store.location.data.LocationDataRepository;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.data.neo4j.server.RemoteServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * Neo Config
 */

@Configuration
@EnableNeo4jRepositories(basePackageClasses = {LocationDataRepository.class})
@EnableTransactionManagement
public class NeoConfig extends Neo4jConfiguration {

    @Autowired
    private Environment env;

    @Bean
    public Neo4jServer neo4jServer() {
        return new RemoteServer("http://localhost:7474","neo4j","admin123");
    }

    @Bean
    public SessionFactory getSessionFactory() {
        // with domain entity base package(s)
        return new SessionFactory("com.walmart.store.location.data.entities");
    }

    // needed for session in view in web-applications
    @Bean
    @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Session getSession() throws Exception {
        return super.getSession();
    }

}