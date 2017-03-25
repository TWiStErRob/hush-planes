package dft.hushplanes.android.data;

import dft.hushplanes.model.Flights;
import io.reactivex.Single;
import retrofit2.http.*;

public interface BackendService {
	@GET("flights/{time}")
	Single<Flights> flights(@Path("time") long time);
}
