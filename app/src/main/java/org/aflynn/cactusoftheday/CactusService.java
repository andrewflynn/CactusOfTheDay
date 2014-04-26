package org.aflynn.cactusoftheday;

import java.util.Date;
import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

interface CactusService {
    // perPage max == 500, page is 1-indexed
    // https://www.flickr.com/services/api/flickr.people.getPhotos.html
    @GET("/?method=flickr.people.getPhotos&api_key=" + PrivateConfig.API_KEY + "&user_id="
            + Config.NSID + "&extras=date_taken%2Cdate_upload&format=json&nojsoncallback=1")
    CactusResponse getPhotos(@Query("min_upload_date") Long minUploadDate,
            @Query("per_page") Integer perPage, @Query("page") Integer page);

    static class CactusResponse {
        CactusPhotos photos;
    }

    static class CactusPhotos {
        int total;
        List<CactusPhoto> photo;
    }

    static class CactusPhoto {
        String id;
        String title;
        int farm;
        String server;
        String secret;
        Date dateupload; // unix time

        // http://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}_b.jpg
        public String getPhotoUrl() {
            return "http://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret
                    + "_b.jpg";
        }

        // http://www.flickr.com/photos/{user-id}/{photo-id} - individual photo
        public String getPageUrl() {
            return "http://www.flickr.com/photos/" + Config.NSID + "/" + id;
        }
    }
}