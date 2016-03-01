package com.walmart.store.container.domain.impl;

import com.walmart.store.container.data.DataRepository;
import com.walmart.store.container.data.Entity;
import com.walmart.store.container.domain.DomainService;

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
