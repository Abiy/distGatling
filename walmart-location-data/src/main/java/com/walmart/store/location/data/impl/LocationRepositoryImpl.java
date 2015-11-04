package com.walmart.store.location.data.impl;

import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;
import com.walmart.store.location.data.LocationRepository;
import com.walmart.store.location.model.Location;

@Repository
public class LocationRepositoryImpl implements LocationRepository {

	public Location getLocation(long id) {
		return new Location(0L, "Journey To Awesome Shelf 1",
				ImmutableMap.of("Levels", (Object) new String[] { "BookShelf", "JTA1" }));
	} 

}
