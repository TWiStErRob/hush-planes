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
        System.setProperty("log4j.configurationFile", "D:\\HACK\\hush-planes\\backend\\src\\main\\resources\\log4j2.xml");
    }
    private static final Logger LOG = LoggerFactory.getLogger(RouteMatcher.class);

    public static void main(String... args) throws Throwable {
		Session session = DatabaseModule.provideSession();
		try {
			@SuppressWarnings("unchecked")
			List<Flight> list = session.createQuery("from Flight where origin = 'EGLL London Heathrow, United Kingdom'").list();
            System.out.println(list.size());
            List<Route> routes = getRoutes();
            for (Flight flight : list) {
                findExpectedRoute(flight, routes);
			}
		} finally {
			DatabaseModule.kill();
		}
	}

	// Sets the runway, route and deviation of the flight, if it's a flight from Heathrow
    static void findExpectedRoute(Flight flight, List<Route> routes) {
        if (flight.origin.startsWith("EGLL") && flight.path.size() > 1) {
            Location start = flight.path.get(0);
            Location end = flight.path.get(flight.path.size() - 1);
            Runway runway;
            String routeName = "";
            // Identify the runway using direction and latitude
            if (flight.path.get(1).longitude > start.longitude) {
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

            // Get routes with the right latitude direction, and choose the one with min average distance
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
            LOG.trace("Flight #{}, Runway {}, Route: {}, Deviation: {}", flight.id, flight.runway, flight.route, flight.deviation);
        }
    }

    // For each point on the ideal route, we calculate the dist from it to the actual route 
    // We average these distances to get the average distance of a point from the ideal route
    static double avgDist(Route idealRoute, List<Location> flightPath) {
    	Iterator<Point> idealIt = idealRoute.points.iterator();
    	Iterator<Location> actualIt = flightPath.iterator();
    	double diffSum = 0;
    	int idealTraversed = 0;
        boolean directionW = idealRoute.name.startsWith("27");  // true means moving West
        Location actual = actualIt.next();
    	Location nextActual = actualIt.next();
        // Find the two actual points that are on either side of the ideal point, longitudinal
    	while (idealIt.hasNext()) {
            Point idealPoint = idealIt.next();
            /*if (newDirection != routeDirection) {
                routeDirection = newDirection;
                if (routeDirection)
                    while (actualIt.hasNext() && (nextActual.longitude - actual.longitude < 0)) {
                        actual = nextActual;
                        nextActual = actualIt.next();
                    }
                else
                    while (actualIt.hasNext() && (nextActual.longitude - actual.longitude > 0)) {
                        actual = nextActual;
                        nextActual = actualIt.next();
                    }
			}*/
            //if (lastIdealPoint != null && )
            // Find the first flight point on the other side of the next ideal point to reach
            while ( (directionW && (nextActual.longitude > idealPoint.longi) ||
                     !directionW && (nextActual.longitude < idealPoint.longi))
                     && actualIt.hasNext()) {
                actual = nextActual;
                nextActual = actualIt.next();
            }

            // Stop if we've reached the end of the flight, without getting to the
            // longitude of the next ideal route point
            if (!actualIt.hasNext() &&
                    (directionW && (nextActual.longitude > idealPoint.longi)
                        || !directionW && (nextActual.longitude < idealPoint.longi))) {
                break;
            }
            else {
                // add the diff between the ideal point and our plotted flight path
                double diff = pointLineDiff(idealPoint.longi, idealPoint.lat,
                        actual.longitude, actual.latitude, nextActual.longitude, nextActual.latitude);
                diffSum += diff;
                idealTraversed++;
            }
    	}
    	// roughly return in km
    	return diffSum / idealTraversed * 111;
    }

    // shortest distance between point (x0,y0) and line defined by (x1,y1), (x2,y2)
    // https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line#Cartesian_coordinates
    private static double pointLineDiff(double x0, double y0, double x1, double y1, double x2, double y2) {
    	double res = (Math.abs(x0*(y2 - y1) - y0*(x2 - x1) + x2*y1 - y2*x1)) 
				/ (Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2)));
		return res;
    }

    // Reads the expected routes from CSV file and returns them
    static List<Route> getRoutes() {
    	String csvFile = "D:\\HACK\\hush-planes\\backend\\src\\main\\resources\\Heathrow Easterly and Westerly departure routes.csv";
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