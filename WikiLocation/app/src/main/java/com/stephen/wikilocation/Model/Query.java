package com.stephen.wikilocation.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Stephen on 3/15/2017.
 */
public class Query
{
    @SerializedName("geosearch")
    private List<Article> articles = null;

    public List<Article> getArticles()
    {
        return articles;
    }

    public void setArticles( List<Article> articles)
    {
        this.articles = articles;
    }


}
