package com.walmart.gatling.domain.impl;

import com.walmart.gatling.data.DataRepository;
import com.walmart.gatling.domain.DomainService;
import com.walmart.gatling.data.Entity;

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
