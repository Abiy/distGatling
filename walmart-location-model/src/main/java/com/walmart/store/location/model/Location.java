package com.walmart.store.location.model;

import java.util.Map;

public class Location {
	private long id;
	private String name;
	private Map<String, Object> attributes;
	
	
	public Location() {
		//For Jackson
	}
	
	public Location(long id, String name, Map<String, Object> attributes) {
		super();
		this.id = id;
		this.name = name;
		this.attributes = attributes;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
