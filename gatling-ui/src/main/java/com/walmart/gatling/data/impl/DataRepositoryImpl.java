package com.walmart.gatling.data.impl;

import com.walmart.gatling.data.DataRepository;
import com.walmart.gatling.data.Entity;

import org.springframework.stereotype.Repository;

@Repository
public class DataRepositoryImpl implements DataRepository {

	@Override
	public Entity getData(long id) {
		return new Entity(1L,"John",21);
	}
}
