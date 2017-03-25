package dft.hushplanes.server;

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;

import dft.hushplanes.db.DatabaseModule;
import dft.hushplanes.model.*;

@Path("/flights")
public class Service {
	@GET
	@Path("/{time}")
	@Produces(MediaType.APPLICATION_JSON)
	// @Context UriInfo uriInfo
	public Flights getAircraftInJSON(@PathParam("time") long searchedTime) {
		Session session = DatabaseModule.provideSession();
		@SuppressWarnings("unchecked")
		List<Flight> list = session.createQuery("from Flight").list();
		Flights flights = new Flights();
		flights.flights = list;
		return flights;
	}
}
