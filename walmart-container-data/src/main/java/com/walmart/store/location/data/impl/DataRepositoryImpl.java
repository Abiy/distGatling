package com.walmart.store.location.data.impl;

import com.walmart.store.location.data.DataRepository;
import com.walmart.store.location.data.entities.Entity;

import org.springframework.stereotype.Repository;

@Repository
public class DataRepositoryImpl implements DataRepository {

	@Override
	public Entity getData(long id) {
		return new Entity(1L,"John",21);
	}
}
