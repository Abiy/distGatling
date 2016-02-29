package com.walmart.store.location.domain.impl;

import com.walmart.store.location.data.DataRepository;
import com.walmart.store.location.data.entities.Entity;
import com.walmart.store.location.domain.DomainService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DomainServiceImpl implements DomainService {

	private DataRepository locRep;
	
	@Autowired
	public DomainServiceImpl(DataRepository locRep) {
		this.locRep = locRep;
	}
	
	@Override
	public Entity service(long id) {
		return locRep.getData(id);
	}
	
}
