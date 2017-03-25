package dft.hushplanes.server;

import java.util.*;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
public class App extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		return new HashSet<>(Arrays.<Class<?>>asList(Service.class));
	}
}
