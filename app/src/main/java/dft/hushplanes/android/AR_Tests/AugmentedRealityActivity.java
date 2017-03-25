package dft.hushplanes.android.AR_Tests;

import javax.inject.Inject;

import org.slf4j.*;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;

import dft.hushplanes.android.*;
import dft.hushplanes.android.data.BackendService;
import dft.hushplanes.model.Flights;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AugmentedRealityActivity extends Activity {
	private static final Logger LOG =
			LoggerFactory.getLogger(AugmentedRealityActivity.class);

	@Inject BackendService backend;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ar);

		GLSurfaceView glView = (GLSurfaceView)findViewById(R.id.overlay);
		glView.setZOrderOnTop(true);

		App.getAppComponent().inject(this);
		backend
				.flights(0)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new SingleObserver<Flights>() {
					@Override public void onSubscribe(Disposable disposable) {

					}
					@Override public void onSuccess(Flights flights) {
						toast(flights.flights.toString());
					}
					@Override public void onError(Throwable throwable) {
						LOG.warn("Failed", throwable);
						toast(throwable.toString());
					}
				});
	}
	private void toast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
}
