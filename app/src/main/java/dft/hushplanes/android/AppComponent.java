package dft.hushplanes.android;

import java.io.*;
import java.util.concurrent.Callable;

import javax.inject.*;

import org.slf4j.*;

import android.content.Context;

import com.google.gson.Gson;

import dagger.*;
import dft.hushplanes.android.AR_Tests.AugmentedRealityActivity;
import dft.hushplanes.android.AppComponent.*;
import dft.hushplanes.android.data.BackendService;
import dft.hushplanes.model.Flights;
import io.reactivex.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Singleton
@Component(modules = {
		AppModule.class,
		BackendModule.class
})
public interface AppComponent {
	void inject(AugmentedRealityActivity activity);

	@Module class AppModule {
		private final Context context;
		AppModule(Context context) {
			this.context = context;
		}
		@Provides Context provideAppContext() {
			return context.getApplicationContext();
		}
	}
	@Module class BackendModule {
		private static final Logger LOG = LoggerFactory.getLogger(BackendModule.class);

		@Provides
		BackendService provideRealService() {
			Retrofit retrofit = new Retrofit.Builder()
					.baseUrl("http://192.168.16.59:8180/backend/")
					.addConverterFactory(GsonConverterFactory.create())
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
					.build();
			return retrofit.create(BackendService.class);
		}

		@Provides @Named("mock")
		BackendService provideMockService(final Context context) {
			return new BackendService() {
				@Override
				public Single<Flights> flights(final long time) {
					LOG.info("Requesting flights for {}", time);
					return Single.defer(new Callable<SingleSource<Flights>>() {
						@Override
						public SingleSource<Flights> call() throws Exception {
							LOG.info("Really requesting times for {}", time);
							InputStream open = context.getAssets().open("flights.json");
							Flights flights = new Gson()
									.fromJson(new InputStreamReader(open), Flights.class);
							return Single.just(flights);
						}
					});
				}
			};
		}
	}
}
