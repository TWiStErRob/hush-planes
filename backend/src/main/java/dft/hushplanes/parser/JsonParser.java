package dft.hushplanes.parser;

import java.io.*;
import java.util.List;

import javax.annotation.*;

import org.hibernate.*;
import org.slf4j.*;

import com.google.gson.*;

import dft.hushplanes.db.DatabaseModule;
import dft.hushplanes.model.*;
import dft.hushplanes.model.Flight.*;
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
		Session session = DatabaseModule.provideSession();
		try {
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
		} finally {
			DatabaseModule.kill();
		}
	}

	private static void save(Session session, File file, AircraftListJsonResponse model) {
		for (Ac aircraft : model.acList) {
			Flight flight = (Flight)session.byId(Flight.class).load(aircraft.Id);
			if (flight == null) {
				flight = new Flight();
				flight.id = aircraft.Id;
				flight.operator = aircraft.Op;
				flight.origin = aircraft.From;
				flight.destination = aircraft.To;
				flight.name = aircraft.Call;
				flight.country = aircraft.Cou;
				flight.model = aircraft.Mdl;
				flight.engines = parseEngines(aircraft.Engines);
				flight.engineType = engineType(aircraft.EngType);
				flight.enginePlacement = enginePlacement(aircraft.EngMount);
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
			loc.altitude = aircraft.Alt;
			loc.speed = aircraft.Spd;
			loc.speed_vertical = aircraft.Vsi;
			loc.bearing = aircraft.Brng;
			flight.path.add(loc);
			try {
				session.saveOrUpdate(loc);
			} catch (NonUniqueObjectException ex) {
				LOG.warn("Duplicate: #{} @ {}", flight.id, loc.time);
				continue;
			}
		}
	}
	private static @Nullable Integer parseEngines(@Nullable String engines) {
		if (engines != null) {
			try {
				return Integer.parseInt(engines);
			} catch (NumberFormatException ex) {
				LOG.warn("Invalid Engines: {}", engines, ex);
				return null;
			}
		} else {
			return null;
		}
	}
	private static @Nonnull EnginePlacement enginePlacement(@Nullable Integer mount) {
		if (mount == null) {
			return EnginePlacement.Unknown;
		}
		switch (mount) {
			case 1:
				return EnginePlacement.AftMounted;
			case 2:
				return EnginePlacement.WingBuried;
			case 3:
				return EnginePlacement.FuselageBuried;
			case 4:
				return EnginePlacement.NoseMounted;
			case 5:
				return EnginePlacement.WingMounted;
			default:
				return EnginePlacement.Unknown;
		}
	}
	private static @Nonnull EngineType engineType(@Nullable Integer type) {
		if (type == null) {
			return EngineType.None;
		}
		switch (type) {
			case 1:
				return EngineType.Piston;
			case 2:
				return EngineType.Turbo;
			case 3:
				return EngineType.Jet;
			case 4:
				return EngineType.Electric;
			default:
				return EngineType.None;
		}
	}
}
