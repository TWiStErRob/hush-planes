package dft.hushplanes.android.AR_Tests;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.inject.*;

import org.slf4j.*;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;

import dft.hushplanes.android.AR_Tests.GL.OverlayView;
import dft.hushplanes.android.*;
import dft.hushplanes.android.data.BackendService;
import dft.hushplanes.model.Flights;
import io.reactivex.Observable;
import io.reactivex.*;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.*;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.*;

public class AugmentedRealityActivity extends Activity {
	private static final Logger LOG =
			LoggerFactory.getLogger(AugmentedRealityActivity.class);

	@Inject @Named("mock")
	BackendService backend;
	private SeekBar seeker;
	private TextView time;
	private OverlayView glView;
	private View progress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ar);

		seeker = (SeekBar)findViewById(R.id.seeker);
		progress = findViewById(R.id.progress);
		time = (TextView)findViewById(R.id.time);
		glView = (OverlayView)findViewById(R.id.overlay);
		glView.setZOrderOnTop(true);
		App.getAppComponent().inject(this);

		observeSeeker()
				.subscribeOn(AndroidSchedulers.mainThread())
				.map(new Function<Integer, Long>() {
					@Override public Long apply(Integer integer) throws Exception {
						return 1489536000000L + TimeUnit.MINUTES.toMillis(integer);
					}
				})
				.doOnNext(new Consumer<Long>() {
					@Override public void accept(Long stamp) {
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(stamp);
						time.setText(String.format(Locale.ROOT, "Time: %1$tH:%<tM", cal));
					}
				})
				.debounce(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
				.observeOn(AndroidSchedulers.mainThread())
				.doOnNext(new Consumer<Long>() {
					@Override public void accept(Long stamp) throws Exception {
						LOG.trace("Debounced {}", stamp);
						progress.setVisibility(View.VISIBLE);
					}
				})
				.flatMap(new Function<Long, ObservableSource<Flights>>() {
					@Override
					public ObservableSource<Flights> apply(Long stamp) {
						return backend
								.flights(stamp)
								.subscribeOn(Schedulers.io())
								.toObservable();
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Observer<Flights>() {
					@Override public void onSubscribe(Disposable d) {

					}
					@Override public void onNext(Flights flights) {
						progress.setVisibility(View.GONE);
						LOG.info("Flights loaded: {}", flights.flights);
						glView.setFlights(flights);
						toast(glView.msg);
					}
					@Override public void onError(Throwable e) {
						progress.setVisibility(View.GONE);
						LOG.warn("Failed", e);
						toast(e.toString());
					}
					@Override public void onComplete() {

					}
				})
		;
		seeker.setProgress(840);
	}

	private Observable<Integer> observeSeeker() {
		final Subject<Integer> seekSubject = BehaviorSubject.create();
		seeker.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				seekSubject.onNext(progress);
			}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {

			}
			@Override public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		return seekSubject;
	}
	private void toast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
}
