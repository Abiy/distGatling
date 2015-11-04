package com.walmart.store.location.domain.impl;

import com.walmart.store.location.data.LocationRepository;
import com.walmart.store.location.domain.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LocationServiceImpl implements LocationService {

	private LocationRepository locRep;
	
	@Autowired
	public LocationServiceImpl(LocationRepository locRep) {
		this.locRep = locRep;
	}
	
	@Override
	public com.walmart.store.location.model.Location getLocation(long id) {
		return locRep.getLocation(id);
	}
	
}
