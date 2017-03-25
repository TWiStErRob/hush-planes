package dft.hushplanes.server;

import com.google.gson.Gson;

import java.util.Date;

import dft.hushplanes.model.Flights;
import dft.hushplanes.parser.AircraftListJsonResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/json/aircraft")
public class Service {
	Flights get() {
		return null;
	}

	@GET
	@Path("/get/{time}")
	@Produces(MediaType.APPLICATION_JSON)
	public AircraftListJsonResponse getAircraftInJSON(@Context UriInfo uriInfo, @QueryParam("time") long searchedTime) {

		AircraftListJsonResponse aircraftAll = new AircraftListJsonResponse();
		return aircraftAll;
	}
	@POST
	@Path("/post")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createAircraftInJSON(AircraftListJsonResponse flight) {

		String result = "" + flight;
		return Response.status(201).entity(result).build();
	}
}