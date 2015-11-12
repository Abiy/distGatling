package com.walmart.store.location.data.entities;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;
import java.util.UUID;

/**
 * A generic location that can be used to store items or serve as a parent for other locations
 */
@NodeEntity()
public class Location extends Entity {

    @Relationship(type="BELONGS_TO", direction= Relationship.INCOMING)
    private Set<Location> Locations;

    private UUID locationId;
    private String gln;
    private String sgln;
    private String name;
    private float temperatureFrom;
    private float temperatureTo;
    private String usageType;

    @Relationship(type="HAS_DEPARTMENT", direction= Relationship.OUTGOING)
    private  Department department;

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getUsageType() {
        return usageType;
    }

    public void setUsageType(String usageType) {
        this.usageType = usageType;
    }

    public float getTemperatureTo() {
        return temperatureTo;
    }

    public void setTemperatureTo(float temperatureTo) {
        this.temperatureTo = temperatureTo;
    }

    public float getTemperatureFrom() {
        return temperatureFrom;
    }

    public void setTemperatureFrom(float temperatureFrom) {
        this.temperatureFrom = temperatureFrom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSgln() {
        return sgln;
    }

    public void setSgln(String sgln) {
        this.sgln = sgln;
    }

    public String getGln() {
        return gln;
    }

    public void setGln(String gln) {
        this.gln = gln;
    }
    //Level Type (Zone, Bin, Type)
    //Level Label (Zone, Slot, Size)

    public UUID getLocationId() {
        return locationId;
    }

    public void setLocationId(UUID locationId) {
        this.locationId = locationId;
    }

    public Set<Location> getLocations() {
        return Locations;
    }

    public void setLocations(Set<Location> locations) {
        Locations = locations;
    }

}
