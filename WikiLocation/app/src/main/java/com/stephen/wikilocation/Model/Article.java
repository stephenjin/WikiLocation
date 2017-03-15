package com.stephen.wikilocation.Model;

/**
 * Created by Stephen on 3/8/2017.
 */
public class Article {

    String title;
    double lat, lon;
    int pageid;
    int dist;

    public Article(String title, double lat, double lon, int pageid, int distance){
        this.title = title;
        this.lat = lat;
        this.lon = lon;
        this.pageid = pageid;
        this.dist = distance;
    }

    public String getTitle() {
        return title;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getPageid() {
        return pageid;
    }

    public int getDist() {
        return dist;
    }
}
