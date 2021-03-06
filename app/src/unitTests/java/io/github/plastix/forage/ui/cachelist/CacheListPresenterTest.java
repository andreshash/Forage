package io.github.plastix.forage.ui.cachelist;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.github.plastix.forage.data.api.OkApiInteractor;
import io.github.plastix.forage.data.local.DatabaseInteractor;
import io.github.plastix.forage.data.local.model.Cache;
import io.github.plastix.forage.data.location.LocationInteractor;
import io.github.plastix.forage.data.network.NetworkInteractor;
import rx.Completable;
import rx.Single;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CacheListPresenterTest {

    private CacheListPresenter cacheListPresenter;
    private CacheListView view;

    private OkApiInteractor okApiInteractor;
    private DatabaseInteractor databaseInteractor;
    private LocationInteractor locationInteractor;
    private NetworkInteractor networkInteractor;

    @Before
    public void beforeEachTest() {
        okApiInteractor = mock(OkApiInteractor.class);
        databaseInteractor = mock(DatabaseInteractor.class);
        locationInteractor = mock(LocationInteractor.class);
        networkInteractor = mock(NetworkInteractor.class);

        cacheListPresenter = new CacheListPresenter(okApiInteractor,
                databaseInteractor,
                locationInteractor,
                networkInteractor);
        view = mock(CacheListView.class);

        cacheListPresenter.onViewAttached(view);

        when(locationInteractor.getUpdatedLocation()).thenReturn(Single.just(mock(Location.class)));
    }

    @Test
    public void fetchGeocaches_savesDownloadedGeocachesToDatabase() {
        List<Cache> caches = asList(mock(Cache.class), mock(Cache.class), mock(Cache.class));
        when(okApiInteractor.getNearbyCaches(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(Single.just(caches));

        when(networkInteractor.hasInternetConnectionCompletable()).thenReturn(Completable.complete());
        when(locationInteractor.isLocationAvailable()).thenReturn(Completable.complete());


        cacheListPresenter.getGeocachesFromInternet();

        verify(databaseInteractor).clearAndSaveGeocaches(caches);
    }

    @Test
    public void fetchGeocaches_networkErrorUpdatesView() {
        when(networkInteractor.hasInternetConnectionCompletable()).thenReturn(
                Completable.error(new Throwable()));

        cacheListPresenter.getGeocachesFromInternet();

        verify(view).onErrorInternet();
    }

    @Test
    public void fetchGeocaches_locationErrorUpdatesView() {
        when(networkInteractor.hasInternetConnectionCompletable()).thenReturn(
                Completable.complete());
        when(locationInteractor.isLocationAvailable()).thenReturn(
                Completable.error(new Throwable()));

        cacheListPresenter.getGeocachesFromInternet();

        verify(view).onErrorLocation();
    }

    @Test
    public void fetchGeocaches_fetchErrorUpdatesView() {
        when(networkInteractor.hasInternetConnectionCompletable()).thenReturn(
                Completable.complete());
        when(locationInteractor.isLocationAvailable()).thenReturn(
                Completable.complete());
        when(okApiInteractor.getNearbyCaches(anyDouble(), anyDouble(), anyDouble())).thenReturn(
                Single.<List<Cache>>error(new Throwable()));

        cacheListPresenter.getGeocachesFromInternet();

        verify(view).onErrorFetch();
    }

    @Test
    public void onDestroy_callsDatabaseInteractor() {
        cacheListPresenter.onDestroyed();

        verify(databaseInteractor).onDestroy();
        verifyNoMoreInteractions(databaseInteractor);
    }
}
