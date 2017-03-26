package dft.hushplanes.android;

import android.app.Application;

import dft.hushplanes.android.AppComponent.AppModule;

public class App extends Application {
	private static App instance;
	private AppComponent component;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		component = DaggerAppComponent
				.builder()
				.appModule(new AppModule(this))
				.build()
		;
	}

	public static AppComponent getAppComponent() {
		return instance.component;
	}
}
