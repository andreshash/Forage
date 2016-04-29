package io.github.plastix.forage.tests;

import android.content.ComponentName;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.plastix.forage.R;
import io.github.plastix.forage.data.local.DatabaseInteractor;
import io.github.plastix.forage.rules.NeedsMockWebServer;
import io.github.plastix.forage.screens.CacheListScreen;
import io.github.plastix.forage.screens.MapScreen;
import io.github.plastix.forage.ui.cachelist.CacheListActivity;
import io.github.plastix.forage.ui.map.MapActivity;
import io.github.plastix.forage.util.LocationUtils;
import io.github.plastix.forage.util.TestUtils;
import io.github.plastix.forage.util.UiAutomatorUtils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CacheListTest {

    @Rule
    public IntentsTestRule<CacheListActivity> activityTestRule =
            new IntentsTestRule<>(CacheListActivity.class);

    private CacheListScreen cacheListScreen;
    private MapScreen mapScreen;
    private UiDevice device;

    private DatabaseInteractor databaseInteractor;

    @BeforeClass
    public static void onlyOnce() {

        final GoogleApiClient apiClient = TestUtils.app().getComponent().googleClient();

        apiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                final Location location = LocationUtils.buildLocation(0, 0);
                LocationServices.FusedLocationApi.setMockMode(apiClient, true)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    LocationServices.FusedLocationApi.setMockLocation(apiClient, location);
                                }

                            }
                        });
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        });
    }


    @Before
    public void beforeEachTest() {
        cacheListScreen = new CacheListScreen();
        mapScreen = new MapScreen();
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        databaseInteractor = TestUtils.app().getComponent().databaseInteractor();
    }

    @Test
    public void testToolbarTitle() {
        UiAutomatorUtils.allowPermissionsIfNeeded(device);

        cacheListScreen.shouldDisplayTitle(TestUtils.app().getString(R.string.cachelist_title));
    }


    @Test
    public void testMapButton() {
        UiAutomatorUtils.allowPermissionsIfNeeded(device);

        // Opens MapActivity
        onView(withId(R.id.cachelist_action_map)).perform(click());

        // Verify that an MapActivity intent has been launched
        intended(hasComponent(new ComponentName(InstrumentationRegistry.getTargetContext(), MapActivity.class)));

        // Verify the MapActivity toolbar title
        mapScreen.shouldDisplayTitle(TestUtils.app().getString(R.string.map_title));
    }

    @Test
    public void testNoGeocachesDownloaded() {
        UiAutomatorUtils.allowPermissionsIfNeeded(device);

        onView(withId(R.id.empty_view)).check(matches(isDisplayed()));
    }

    @Ignore
    @Test
    @NeedsMockWebServer(setupMethod = "queueNetworkRequest")
    public void shouldDisplayGeocachesFromServer() throws UiObjectNotFoundException {
        UiAutomatorUtils.allowPermissionsIfNeeded(device);

        // Swipe the refresh view
        onView(withId(R.id.cachelist_swiperefresh)).perform(swipeDown());
    }

    public void queueNetworkRequest(final MockWebServer mockWebServer) {
        String jsonResponse = "{\n" +
                "  \"CACHE1\":{\n" +
                "    \"code\":\"CACHE1\",\n" +
                "    \"name\":\"Cache Name 1\",\n" +
                "    \"location\":\"45|45\",\n" +
                "    \"type\":\"Traditional\",\n" +
                "    \"status\":\"Available\",\n" +
                "    \"terrain\":1,\n" +
                "    \"difficulty\":2.5,\n" +
                "    \"size2\":\"micro\",\n" +
                "    \"description\":\"<p>Cache Description 1<\\/p>\"\n" +
                "  },\n" +
                "  \"CACHE2\":{\n" +
                "    \"code\":\"CACHE2\",\n" +
                "    \"name\":\"Cache Name 2\",\n" +
                "    \"location\":\"0|0\",\n" +
                "    \"type\":\"Virtual\",\n" +
                "    \"status\":\"Available\",\n" +
                "    \"terrain\":1,\n" +
                "    \"difficulty\":1,\n" +
                "    \"size2\":\"none\",\n" +
                "    \"description\":\"<p>Cache Description 2<\\/p>\"\n" +
                "  }\n" +
                "}";
        mockWebServer.enqueue(new MockResponse().setBody(jsonResponse));

    }


}

