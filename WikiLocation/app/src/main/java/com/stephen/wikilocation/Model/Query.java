package com.stephen.wikilocation.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by Stephen on 3/15/2017.
 */
public class Query
{
    @SerializedName("geosearch")
    private List<Article> articles = null;

    @SerializedName("pages")
    private Map<String, Article> pageid;

    public List<Article> getArticles()
    {
        return articles;
    }

    public void setArticles( List<Article> articles){ this.articles = articles;}


    public Map<String, Article> getPageid() {return pageid;}

    public void setPageid(Map<String, Article> pageid) {this.pageid = pageid;}
}
