package com.walmart.store.container.data.impl;

import com.walmart.store.container.data.DataRepository;
import com.walmart.store.container.data.Entity;

import org.springframework.stereotype.Repository;

@Repository
public class DataRepositoryImpl implements DataRepository {

	@Override
	public Entity getData(long id) {
		return new Entity(1L,"John",21);
	}
}
