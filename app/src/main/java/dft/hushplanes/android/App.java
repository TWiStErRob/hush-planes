package dft.hushplanes.android;

import android.app.Application;

public class App extends Application {
	private static App instance;
	private AppComponent component;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		component = DaggerAppComponent.create();
	}

	public static AppComponent getAppComponent() {
		return instance.component;
	}
}
