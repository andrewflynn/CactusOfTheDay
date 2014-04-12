package org.aflynn.cactusoftheday;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.aflynn.cactusoftheday.CactusService.CactusPhoto;
import org.aflynn.cactusoftheday.CactusService.CactusResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class CactusArtSource extends RemoteMuzeiArtSource {
    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("MMMM d yyyy"); // January 31 2014
    private static final Random RANDOM = new Random();
    private static final int PAGINATION = 500;
    private static final long CHECK_INTERVAL_MILLIS = 1 * 60 * 60 * 1000L; // 1 hr
    private static final int CACTUS_OF_THE_DAY_WINDOW_SECS = 48 * 60 * 60; // 48 hrs

    public CactusArtSource() {
        super(CactusArtSource.class.getSimpleName());
    }

    // TODO: Add 'Next' button when random cacti are showing

    @Override
    protected void onEnabled() {
        super.onEnabled();
        Analytics.track(this, Analytics.CATEGORY_USER, Analytics.ACTION_ENABLED);
    }

    @Override
    protected void onDisabled() {
        super.onDisabled();
        Analytics.track(this, Analytics.CATEGORY_USER, Analytics.ACTION_DISABLED);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateAdapter())
                .create();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Config.ENDPOINT)
                .setConverter(new GsonConverter(gson))
                .build();
        CactusService cactusService = restAdapter.create(CactusService.class);

        if (!tryCactusOfTheDay(cactusService)) {
            randomCactus(cactusService);
        }

        scheduleUpdate(System.currentTimeMillis() + CHECK_INTERVAL_MILLIS);
    }

    private boolean tryCactusOfTheDay(CactusService cactusService) throws RetryException {
        long fortyEightHoursAgoSecs =
                (System.currentTimeMillis() / 1000) - CACTUS_OF_THE_DAY_WINDOW_SECS;
        CactusResponse cactusResponse = cactusService.getPhotos(
                fortyEightHoursAgoSecs /* minUploadDate */, 1 /* perPage */, 1 /* page */);
        Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_RPC,
                Analytics.LABEL_SINGLE_CACTUS);

        if (cactusResponse == null || cactusResponse.photos == null) {
            Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_ERROR,
                    Analytics.LABEL_NULL_RESPONSE);
            Log.w(Config.LOG_TAG, "Null response from API- COTD");
            throw new RetryException();
        }

        if (cactusResponse.photos.photo == null || cactusResponse.photos.photo.size() == 0) {
            return false;
        }

        // Else grab this one
        CactusPhoto photo = cactusResponse.photos.photo.get(0);
        postCactus(photo);
        // Also null out the random preference since we should start from fresh
        CactusPreference.clearRandomWindow(this);
        return true;
    }

    private void randomCactus(CactusService cactusService) throws RetryException {
        // Only grab a new random cactus if enough time has passed
        if (!CactusPreference.checkRandomWindow(this)) {
            Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_NOT_UPDATING);
            return;
        }

        CactusResponse cactusResponse = cactusService.getPhotos(null /* minUploadDate */,
                PAGINATION /* perPage */, 1 /* page */);
        Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_RPC,
                Analytics.LABEL_RANDOM_CACTUS, 1 /* value == page number */);

        if (cactusResponse == null || cactusResponse.photos == null) {
            Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_ERROR,
                    Analytics.LABEL_NULL_RESPONSE, 1 /* value == page number */);
            Log.w(Config.LOG_TAG, "Null response from API- Random page 1");
            throw new RetryException();
        }

        int totalNumberOfPics = cactusResponse.photos.total;
        int randomPicNumber = RANDOM.nextInt(totalNumberOfPics - 1);
        randomPicNumber++; // Choose random + 1 so we don't pick the most recent COTD

        // If we need to look at a second page
        if (randomPicNumber >= PAGINATION) {
            int pageNumber = 1;
            while (randomPicNumber >= PAGINATION) {
                pageNumber++;
                randomPicNumber -= PAGINATION;
            }

            cactusResponse = cactusService.getPhotos(null /* minUploadDate */,
                    PAGINATION /* perPage */, pageNumber /* page */);
            Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_RPC,
                    Analytics.LABEL_RANDOM_CACTUS, pageNumber /* value == page number */);

            if (cactusResponse == null || cactusResponse.photos == null) {
                Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_ERROR,
                        Analytics.LABEL_NULL_RESPONSE, pageNumber /* value == page number */);
                Log.w(Config.LOG_TAG, "Null response from API- Random page " + pageNumber);
                throw new RetryException();
            }
        }

        CactusPhoto photo = cactusResponse.photos.photo.get(randomPicNumber);
        postCactus(photo);
        // Also update preferences
        CactusPreference.updateRandomWindow(this);
    }

    private void postCactus(CactusPhoto photo) {
        // Only publish if it's new
        Artwork artwork = getCurrentArtwork();
        if (artwork == null || !photo.id.equals(artwork.getToken())) {
            String pageUrl = photo.getPageUrl();
            publishArtwork(new Artwork.Builder()
                    .token(photo.id)
                    .title(photo.title)
                    .byline(FORMAT.format(photo.dateupload))
                    .imageUri(Uri.parse(photo.getPhotoUrl()))
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl)))
                    .build());
            Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_PUBLISH_WALLPAPER,
                    pageUrl);
        }
    }
}
