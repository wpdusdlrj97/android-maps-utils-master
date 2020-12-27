package com.google.maps.android.utils.demo.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Person_Test implements ClusterItem {
    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;
    public final int profilePhoto;


    public Person_Test(LatLng position, String title, String snippet, int pictureResource) {
        mPosition = position;
        mTitle = title;
        mSnippet = snippet;
        profilePhoto = pictureResource;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}