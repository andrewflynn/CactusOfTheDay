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

import java.util.Date;
import java.util.Random;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;

public class CactusArtSource extends RemoteMuzeiArtSource {
    private static final Random RANDOM = new Random();

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

        try {
            if (!tryCactusOfTheDay(cactusService)) {
                randomCactus(cactusService);
            }
        } catch (RetrofitError e) {
            Log.w(Config.LOG_TAG, "Retrofit error while calling CactusOfTheDay");
            Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_ERROR,
                    Analytics.LABEL_NETWORK);
            // Continue through to reschedule an update
        }

        scheduleUpdate(System.currentTimeMillis() + Config.CHECK_INTERVAL_MILLIS);
    }

    private boolean tryCactusOfTheDay(CactusService cactusService) throws RetryException {
        long fortyEightHoursAgoSecs =
                (System.currentTimeMillis() / 1000) - Config.CACTUS_OF_THE_DAY_DISPLAY_WINDOW_SECS;
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
                Config.RANDOM_CACTUS_PAGINATION /* perPage */, 1 /* page */);
        Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_RPC,
                Analytics.LABEL_RANDOM_CACTUS, 1 /* value == page number */);

        if (cactusResponse == null || cactusResponse.photos == null) {
            Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_ERROR,
                    Analytics.LABEL_NULL_RESPONSE, 1 /* value == page number */);
            Log.w(Config.LOG_TAG, "Null response from API- Random page 1");
            throw new RetryException();
        }

        int totalNumberOfPics = cactusResponse.photos.total;
        if (totalNumberOfPics <= 0) {
            Log.w(Config.LOG_TAG, "No cacti exist in the account. Not showing anything.");
            // TODO: Show a bundled or static picture
            return;
        }

        int randomPicNumber;
        if (totalNumberOfPics == 1) {
            randomPicNumber = 0;
        } else {
            randomPicNumber = RANDOM.nextInt(totalNumberOfPics - 1);
            randomPicNumber++; // Choose random + 1 so we don't pick the most recent COTD
        }

        // If we need to look at a second page
        if (randomPicNumber >= Config.RANDOM_CACTUS_PAGINATION) {
            int pageNumber = 1;
            while (randomPicNumber >= Config.RANDOM_CACTUS_PAGINATION) {
                pageNumber++;
                randomPicNumber -= Config.RANDOM_CACTUS_PAGINATION;
            }

            cactusResponse = cactusService.getPhotos(null /* minUploadDate */,
                    Config.RANDOM_CACTUS_PAGINATION /* perPage */, pageNumber /* page */);
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
                    .byline(Config.DESCRIPTION_DATE_FORMAT.format(photo.dateupload))
                    .imageUri(Uri.parse(photo.getPhotoUrl()))
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl)))
                    .build());
            Analytics.track(this, Analytics.CATEGORY_AUTO, Analytics.ACTION_PUBLISH_WALLPAPER,
                    pageUrl);
        }
    }
}
