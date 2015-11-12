package com.walmart.store.location.data;

import com.walmart.store.location.data.entities.Location;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public interface LocationDataRepository extends GraphRepository<Location> {
    // MATCH (p) WHERE p:Location AND p.name = {name}
    List<Location> findByName(String name);

    @Query(value = "MATCH (dt:Department)<-[r:HAS_DEPARTMENT]-(loc:Location) WHERE dt.departmentId = {deptId} RETURN loc" )
    Collection<Location> findByDepartmentId(@Param("deptId") String departmentId);

    // MATCH (p)-[:FRIEND]-(f) WHERE p:Person and id(f) = {0}
    //Set<Location> findByFriends(Location friend);
}

