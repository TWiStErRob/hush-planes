package dft.hushplanes.android;

import javax.inject.Singleton;

import dagger.*;
import dft.hushplanes.android.AR_Tests.AugmentedRealityActivity;
import dft.hushplanes.android.AppComponent.BackendModule;
import dft.hushplanes.android.data.BackendService;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Singleton
@Component(modules = {
		BackendModule.class
})
public interface AppComponent {
	void inject(AugmentedRealityActivity activity);

	@Module class BackendModule {
		@Provides
		BackendService provideService() {
			Retrofit retrofit = new Retrofit.Builder()
					.baseUrl("http://192.168.16.59:8080/backend/")
					.addConverterFactory(GsonConverterFactory.create())
					.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
					.build();
			return retrofit.create(BackendService.class);
		}
	}
}
