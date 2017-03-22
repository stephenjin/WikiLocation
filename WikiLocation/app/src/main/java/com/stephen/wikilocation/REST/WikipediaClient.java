package com.stephen.wikilocation.REST;

import com.stephen.wikilocation.Model.Data;

import retrofit2.Call;
import retrofit2.http.*;


public interface WikipediaClient {
   @Headers("User-Agent: WikiLocation/1.1")
   @GET("./")
   Call<Data> getArticlesNearby(
           @Query("action") String action,
           @Query("list") String geoSearch,
           @Query("gsradius") int radius,
           @Query("gscoord") String coordinates,
           @Query("format") String format
   );

   @Headers("User-Agent: WikiLocation/1.1")
   @GET("./")
   Call<Data> getThumbnailURL(
           @Query("action") String action,
           @Query("prop") String pageImages,
           @Query("piprop") String imageType,
           @Query("format") String format,
           @Query("titles") String title
   );

}

