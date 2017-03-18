package com.stephen.wikilocation.REST;

import com.stephen.wikilocation.Model.Data;

import retrofit2.Call;
import retrofit2.http.*;


public interface WikipediaClient {
   @Headers("User-Agent: WikiLocation/1.1")
   @GET("./git")
   Call<Data> getArticlesNearby(
           @Query("action") String action,
           @Query("list") String geoSearch,
           @Query("gsradius") int radius,
           @Query("gscoord") String coordinates,
           @Query("format") String format
   );

}

