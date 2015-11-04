package com.walmart.store.location.domain.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.walmart.store.location.data.LocationRepository;
import com.walmart.store.location.domain.LocationService;
import com.walmart.store.location.model.Location;

@Service
public class LocationServiceImpl implements LocationService {

	private LocationRepository locRep;
	
	@Autowired
	public LocationServiceImpl(LocationRepository locRep) {
		this.locRep = locRep;
	}
	
	@Override
	public Location getLocation(long id) {
		return locRep.getLocation(id);
	}
	
}
