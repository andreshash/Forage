package io.github.plastix.forage.ui.map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.github.plastix.forage.ForageApplication;
import io.github.plastix.forage.R;
import io.github.plastix.forage.data.local.model.Cache;
import io.github.plastix.forage.ui.base.PresenterActivity;
import io.github.plastix.forage.ui.cachedetail.CacheDetailActivity;
import io.github.plastix.forage.util.ActivityUtils;

/**
 * Activity that represents the map screen of the app.
 */
public class MapActivity extends PresenterActivity<MapPresenter, MapActivityView> implements
        MapActivityView, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    @BindView(R.id.map_toolbar)
    Toolbar toolbar;

    private SupportMapFragment mapFrag;
    private GoogleMap googleMap;
    private Map<Marker, String> markers;

    /**
     * Static factory method that returns a new intent for opening a {@link MapActivity}.
     *
     * @param context A context
     * @return A new intent.
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, MapActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        injectDependencies();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        setSupportActionBar(toolbar);
        ActivityUtils.setSupportActionBarBack(getDelegate());
        ActivityUtils.setStatusBarTranslucent(this);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_mapfragment);
        markers = new HashMap<>();
    }


    private void injectDependencies() {
        ForageApplication.getComponent(this)
                .plus(new MapModule(this))
                .injectTo(this);
    }

    @Override
    public void addMapMarkers(List<Cache> caches) {
        if (googleMap != null) {
            for (Cache cache : caches) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(cache.location.latitude, cache.location.longitude))
                        .title(cache.name);
                Marker marker = googleMap.addMarker(markerOptions);
                markers.put(marker, cache.cacheCode);
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String cacheCode = markers.get(marker);

        startActivity(CacheDetailActivity.newIntent(this, cacheCode));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setPadding(0, toolbar.getHeight(), 0, 0);

        this.googleMap = googleMap;
        presenter.setupMap();
    }

    @Override
    public void animateMapCamera(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    @Override
    protected void onStart() {
        super.onStart();

        mapFrag.getMapAsync(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        googleMap.setOnInfoWindowClickListener(null);
        googleMap = null;
        markers.clear();
        markers = null;
    }
}
