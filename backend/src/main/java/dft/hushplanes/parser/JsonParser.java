package dft.hushplanes.parser;

import java.io.*;
import java.util.List;

import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.*;

import com.google.gson.*;

import dft.hushplanes.model.*;
import dft.hushplanes.parser.AircraftListJsonResponse.Ac;

public class JsonParser {
	static {
		System.setProperty("org.jboss.logging.provider", "slf4j");
	}

	private static final Logger LOG = LoggerFactory.getLogger(JsonParser.class);

	public static void main(String... args) throws Throwable {
		File dbFile = new File("h:\\temp\\db.sqlite");
		if (!dbFile.delete() && dbFile.exists()) {
			throw new IllegalStateException("Cannot delete DB");
		}
		Configuration configuration = new Configuration().configure();
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties())
				.build();
		SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			String folder = "h:\\temp\\sample";
			Gson gson = new GsonBuilder().create();
			for (File file : new File(folder).listFiles()) {
				LOG.trace("Loading {}", file);
				try (Reader reader = new FileReader(file)) {
					AircraftListJsonResponse model =
							gson.fromJson(reader, AircraftListJsonResponse.class);
					save(session, file, model);
				}
			}
			transaction.commit();
		} catch (Exception ex) {
			transaction.rollback();
			throw ex;
		}
		@SuppressWarnings("unchecked")
		List<Flight> list = session.createQuery("from Flight").list();
		for (Flight flight : list) {
			LOG.trace("Flight #{} {}: {} -> {}",
					flight.id, flight.name, flight.origin, flight.destination);
		}
		session.close();
		sessionFactory.close();
	}

	private static void save(Session session, File file, AircraftListJsonResponse model) {
		for (Ac aircraft : model.acList) {
			Flight flight =
					(Flight)session.byId(Flight.class).load(aircraft.Id);
			if (flight == null) {
				flight = new Flight();
				flight.id = aircraft.Id;
				flight.operator = aircraft.Op;
				flight.origin = aircraft.From;
				flight.destination = aircraft.To;
				flight.name = aircraft.Call;
				flight.country = aircraft.Cou;
				flight.model = aircraft.Mdl;
				session.saveOrUpdate(flight);
			}

			if (aircraft.PosStale) {
				continue;
			}
			Location loc = new Location();
			loc.file = file.getName();
			loc.flight = flight;
			loc.time = aircraft.TSecs;
			loc.latitude = aircraft.Lat;
			loc.longitude = aircraft.Long;
			loc.speed = aircraft.Spd;
			loc.speed_vertical = aircraft.Vsi;
			loc.bearing = aircraft.Brng;
			
			session.saveOrUpdate(loc);
		}
	}
}
