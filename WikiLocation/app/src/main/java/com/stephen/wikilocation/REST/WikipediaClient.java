package com.stephen.wikilocation.REST;

import com.stephen.wikilocation.Model.Article;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;


public interface WikipediaClient {

   @GET("")
   Call<List<Article>> getArticlesNearby(
           @Query("action") String action,
           @Query("list") String geoSearch,
           @Query("gsradius") int radius,
           @Query("gscoord") String cordinates
   );
}

