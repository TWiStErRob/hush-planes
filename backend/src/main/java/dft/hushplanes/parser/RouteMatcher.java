package dft.hushplanes.parser;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.lang.Double;

import org.hibernate.*;
import org.slf4j.*;

import com.google.gson.*;

import dft.hushplanes.db.DatabaseModule;
import dft.hushplanes.model.*;
import dft.hushplanes.parser.AircraftListJsonResponse.Ac;

public class RouteMatcher {
	static {
		System.setProperty("org.jboss.logging.provider", "slf4j");
	}

	private static final Logger LOG = LoggerFactory.getLogger(JsonParser.class);

	public static void main(String... args) throws Throwable {
		File dbFile = new File("D:\\Temp\\db.sqlite");
		if (!dbFile.delete() && dbFile.exists()) {
			throw new IllegalStateException("Cannot delete DB");
		}
		Session session = DatabaseModule.provideSession();
		try {
			Transaction transaction = session.beginTransaction();
			try {
				String folder = "D:\\HACK\\Aviation Data\\data";
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
			List<Flight> list = session.createQuery("from Flight where origin = 'EGLL London Heathrow, United Kingdom'").list();
			for (Flight flight : list) {
				LOG.trace("Flight #{} {}: {} -> {}",
						flight.id, flight.name, flight.origin, flight.destination);
			}
			// Get the flights that are departing Heathrow (from Heathrow, or, ascending and <1000 altitude and around Heathrow)
            // Add runway, route and deviation to flight
		} finally {
			DatabaseModule.kill();
		}
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

	// Sets the runway, route and deviation of the flight, if it's a flight from Heathrow
    static void findExpectedRoute(Flight flight) {
        if (flight.origin.startsWith("EGLL")) {
            List<Route> routes = getRoutes();
            Location start = flight.path.get(0);
            Location end = flight.path.get(flight.path.size() - 1);
            Runway runway;
            String routeName = "";
            // Direction - assume no curving back for now
            // Identify the runway using direction and latitude
            if (end.longitude > start.longitude) {
                if (Math.abs(Runway.L09.latitude - start.latitude) < Math.abs(Runway.R09.latitude - start.latitude))
                    runway = Runway.L09;
                else
                    runway = Runway.R09;
            } else {
                if (Math.abs(Runway.L27.latitude - start.latitude) < Math.abs(Runway.R27.latitude - start.latitude))
                    runway = Runway.L27;
                else
                    runway = Runway.R27;
            }
            flight.runway = runway;

            // Latitude increases / decreases monotonically.
            // Get routes with the right latitude direction (sign)
            double latDiff = end.latitude - start.latitude;
            double minDist = Double.MAX_VALUE;
            for (Route r : routes)
                if (r.runway == runway && (latDiff * r.latDiff > 0)) {
                    double dist = avgDist(r, flight.path);
                    if (dist < minDist) {
                        minDist = dist;
                        routeName = r.name;
                    }
                }
            flight.route = routeName;
            flight.deviation = minDist;
            LOG.trace("Flight #{} {} from runway {}, route {}, deviation {}",
                    flight.id, flight.name, flight.runway, flight.route, flight.deviation);
        }
    }

    // For each point on the ideal route, we calculate the dist from it to the actual route 
    // (We don't treat the ideal route as continuous because it has large gaps)
    // We average these distances to get the average distance of a point from the ideal route
    static double avgDist(Route idealRoute, List<Location> flightPath) {
    	Iterator<Point> idealIt = idealRoute.points.iterator();
    	Iterator<Location> actualIt = flightPath.iterator();
    	double diffSum = 0;
    	int idealTraversed = 0;
    	Location actual = actualIt.next();
    	Location nextActual = actualIt.next();
    	while (idealIt.hasNext()) {
    		Point idealPoint = idealIt.next();
    		// if long is moving negative, find the first actual point < long.
    		// else find the first actual point > long. 
    		while (nextActual.longitude > idealPoint.longi && actualIt.hasNext()) {
    			actual = nextActual;
    			nextActual = actualIt.next();
    		}
    		if (!actualIt.hasNext() && nextActual.longitude > idealPoint.longi) {
    			// the actual route doesn't reach the next ideal point
    			break;
    		}
    		else {
    			double diff = pointLineDiff(idealPoint.longi, idealPoint.lat,
                        actual.longitude, actual.latitude, nextActual.longitude, nextActual.latitude);
    			diffSum += diff;
    			idealTraversed++;
    		}
    	}
    	return diffSum / idealTraversed;
    }

    // shortest distance between point (x0,y0) and line defined by (x1,y1), (x2,y2)
    // https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line#Cartesian_coordinates
    private static double pointLineDiff(double x0, double y0, double x1, double y1, double x2, double y2) {
    	if (Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2) == 0)
    		System.out.println("problem");
    	double res = (Math.abs(x0*(y2 - y1) - y0*(x2 - x1) + x2*y1 - y2*x1)) 
				/ (Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2)));
		return res;
    }

    // Reads the expected routes from CSV file and returns them
    static List<Route> getRoutes() {
    	String csvFile = "Heathrow Easterly and Westerly departure routes.csv";
        String line = "";
        String cvsSplitBy = ",";
        List<Route> routes = new ArrayList<Route>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	br.readLine();
        	List<Point> points = new ArrayList<Point>();
        	line = br.readLine();
        	String[] data = line.split(cvsSplitBy);
            String name = data[1];
            while ((line = br.readLine()) != null) {
            	while (data[1].equals(name)) {
                	Point p = new Point(Double.parseDouble(data[3]), Double.parseDouble(data[4]));
                	points.add(p);
                	if ((line = br.readLine()) == null)
                		break;
                	data = line.split(cvsSplitBy);
                }
                routes.add(new Route(name, points));
                points = new ArrayList<Point>();
                name = data[1];
                points.add(new Point(Double.parseDouble(data[3]), Double.parseDouble(data[4])));
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return routes;
    }

    static class Route {
    	String name;
    	List<Point> points;
    	Runway runway; // 27L, 27R, 09L, 09R
    	double latDiff;
    	double deviation;
    	
    	public Route(String n, List<Point> x) {
    		name = n;
    		points = x;
    		latDiff = x.get(x.size()-1).lat - x.get(0).lat;
    		for (Runway r: Runway.values()) {
    			if (name.startsWith(r.name)) {
    				runway = r;;
    				break;
    			}
    		}
    	}
    	public void printMe() {
    		System.out.print(name + " : ");
	        for (Point p: points) {
	        	System.out.print("(" + p.lat + ", " + p.longi + "), ");
	        }
    	}
    }

    static class Point {
    	double lat;
    	double longi;
    	double dist;
    	public Point(double x, double y) {
    		lat = x;
    		longi = y;
    	}
    	public Point(double x, double y, double d) {
    		lat = x;
    		longi = y;
    		dist = d;
    	}
    	public String toString() {
    		return "(" + lat + ", " + longi + ")";
    	}
    }

    // Uses Haversine method. Returns in metres. Copied from 
	// http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
	static double distance(Point x, Point y) {
		double lat1 = x.lat;
		double lat2 = y.lat;
		double lon1 = x.longi;
		double lon2 = y.longi;
	    final int R = 6371; // Radius of the earth

	    Double latDistance = Math.toRadians(lat2 - lat1);
	    Double lonDistance = Math.toRadians(lon2 - lon1);
	    Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
	            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
	            * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
	    Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double distance = R * c * 1000; // convert to meters

	    distance = Math.pow(distance, 2);

	    return Math.sqrt(distance);
	}
}