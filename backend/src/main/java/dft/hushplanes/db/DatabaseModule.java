package dft.hushplanes.db;

import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class DatabaseModule {
	private static Session session;

	public static Session provideSession() {
		if (session == null) {
			Configuration configuration = new Configuration().configure();
			ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.applySettings(configuration.getProperties())
					.build();
			SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);
			session = sessionFactory.openSession();
		}
		return session;
	}

	public static synchronized void kill() {
		session.close();
		session.getSessionFactory().close();
		session = null;
	}
}
