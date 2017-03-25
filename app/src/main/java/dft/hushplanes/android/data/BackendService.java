package dft.hushplanes.android.data;

import dft.hushplanes.model.Flights;
import io.reactivex.Single;
import retrofit2.http.GET;

public interface BackendService {
	@GET("/")
	Single<Flights> flights();
}
