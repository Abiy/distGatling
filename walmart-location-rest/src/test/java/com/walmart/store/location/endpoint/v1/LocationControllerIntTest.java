package com.walmart.store.location.endpoint.v1;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.walmart.store.location.AbstractRestIntTest;
import com.walmart.store.location.domain.LocationService;
import com.walmart.store.location.model.Location;

/**
 * Trivial integration test class that exercises the Junit spring runner and in container testing.
 * 
 * @author jevans
 *
 */
public class LocationControllerIntTest extends AbstractRestIntTest {
	
	@Autowired
	private LocationService locSvc;
	
	@Test
	public void testLocationGetByIdMarshalsCorrectly() {
		long location = 0;
		Location wireLoc = template.getForObject("http://localhost:8080/api/location/" + location, Location.class);
		assertEqualsServiceValue(wireLoc);
	}

	private void assertEqualsServiceValue(Location wireLoc) {
		assertNotNull("Expected supplied wire Location to be non-null.", wireLoc);
		
		long location = wireLoc.getId();
		Location svcLoc = locSvc.getLocation(location);
		assertNotNull("Expected the location service to have a location with id '" + location + "'.", svcLoc);
		assertJsonEquals(svcLoc, wireLoc);
	}
	
}
