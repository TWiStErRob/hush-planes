package dft.hushplanes.server;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;

import dft.hushplanes.db.DatabaseModule;
import dft.hushplanes.model.*;

@Path("/flights")
public class Service {
	private static final long MIN_15 = TimeUnit.MINUTES.toMillis(1);

	@GET
	@Path("/full")
	@Produces(MediaType.APPLICATION_JSON)
	// @Context UriInfo uriInfo
	public Flights getAircraftInJSON() {
		Session session = DatabaseModule.provideSession();
		@SuppressWarnings("unchecked")
		List<Flight> list = session.createQuery("from Flight").list();
		Flights flights = new Flights();
		flights.flights = list;
		return flights;
	}

	@GET
	@Path("/{time}")
	@Produces(MediaType.APPLICATION_JSON)
	// @Context UriInfo uriInfo
	public Flights getAircraftInJSON(@PathParam("time") long searchedTime) {
		Session session = DatabaseModule.provideSession();
		@SuppressWarnings("unchecked")
		List<Location> list = session
				.createQuery("select loc from Location as loc"
						+ " where loc.time between :start_time and :end_time")
				.setParameter("start_time", searchedTime)
				.setParameter("end_time", searchedTime + MIN_15)
				.list();
		Flights flights = new Flights();
		Collection<Flight> sum = new HashSet<>();
		for (Location loc : list) {
			sum.add(loc.flight);
		}
		flights.flights.addAll(sum);
		return flights;
	}
}
