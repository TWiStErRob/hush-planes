package dft.hushplanes.android;

import org.slf4j.*;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

	private static final Logger LOG = LoggerFactory.getLogger(MapsActivity.class);

	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LOG.trace("Inflating map");
		setContentView(R.layout.activity_maps);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment =
				(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;

		LatLng airfieldReferenceHeathrow = new LatLng(51.4775, -0.461389);
		map.addMarker(new MarkerOptions()
				.position(airfieldReferenceHeathrow)
				.title("Airfield reference point in Heathrow Airport")
		);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(airfieldReferenceHeathrow, 10));
	}
}
