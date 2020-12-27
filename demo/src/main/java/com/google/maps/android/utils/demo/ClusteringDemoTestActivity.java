/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.utils.demo.model.MyItem;
import com.google.maps.android.utils.demo.model.Person_Test;
import com.google.maps.android.utils.demo.model.Person_Test;

import org.json.JSONException;

import java.io.InputStream;
import java.util.List;

/**
 * Simple activity demonstrating ClusterManager.
 */
public class ClusteringDemoTestActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private ClusterManager<Person_Test> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // 구글 맵 프래그먼트를 띄운다
        // SupprotMapFragment 를 통해 레이아웃에 만든 fragment 의 ID 를 참조하고 구글맵을 호출한다.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.test_map);
        // getMapAsync 는 반드시 main Thread 에서 호출되어야한다
        mapFragment.getMapAsync(this);
    }



    //맵이 사용할 준비가 되었을 때 호출되는 메서드
    //(NULL이 아닌 GoogleMap 객체를 파라미터로 제공해 줄 수 있을 때)
    @Override
    public void onMapReady(GoogleMap map) {

        if (mMap != null) {
            return;
        }
        mMap = map;

        //맵 시작할 때 카메라 zoom in 할 위치
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.56, 126.97), 10));
        //클러스터 매니저 생성
        mClusterManager = new ClusterManager<>(this, mMap);
        //맵에 클러스터 매니저 연결
        mMap.setOnCameraIdleListener(mClusterManager);

        //클러스터링되는 마커를 커스텀하기 위해 필요한 것
        mClusterManager.setRenderer(new MyClusterRenderer(this, mMap, mClusterManager));


        //클러스터 클릭 리스너
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Person_Test>() {
            @Override
            public boolean onClusterClick(Cluster<Person_Test> cluster) {

                Toast.makeText(ClusteringDemoTestActivity.this,"클러스터 클릭", Toast.LENGTH_LONG).show();

                LatLng latLng = new LatLng(cluster.getPosition().latitude, cluster.getPosition().longitude);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,10);
                map.moveCamera(cameraUpdate);
                return false;
            }
        });


        //개별 마커 정보창 클릭 리스너
        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(ClusteringDemoTestActivity.this,marker.getTitle()+" 정보창 클릭", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), ClusteringDemoActivity.class);
                startActivity(intent);
            }
        });




        //아이템 추가하기
        addItems();

    }



    private void addItems() {

        // Set some lat/lng coordinates to start with.
        double lat = 37.56;
        double lng = 126.97;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 15; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            Person_Test offsetItem = new Person_Test(new LatLng(lat,lng), "Title " + i,"Snippet " + i, R.drawable.walter);
            mClusterManager.addItem(offsetItem);
        }
    }




    public class MyClusterRenderer extends DefaultClusterRenderer<Person_Test> {

        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;


        public MyClusterRenderer(Context context, GoogleMap map,
                                 ClusterManager<Person_Test> clusterManager) {
            super(context, map, clusterManager);

            mImageView = new ImageView(getApplicationContext());
            //mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(150,150));
            //int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(2,2,2,2);
            mIconGenerator.setContentView(mImageView);
        }

        //클러스터링 하기전에 아이템을 랜더링 하는 것입니다.
        @Override
        protected void onBeforeClusterItemRendered(Person_Test Person_Test,
                                                   MarkerOptions markerOptions) {

            //BitmapDescriptor markerDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);

            markerOptions.icon(getItemIcon(Person_Test));
        }

        @Override
        protected void onClusterItemRendered(Person_Test clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);

            //Toast.makeText(ClusteringDemoTestActivity.this,"클러스터 클릭", Toast.LENGTH_LONG).show();
        }

        private BitmapDescriptor getItemIcon(Person_Test Person_Test) {
            mImageView.setImageResource(Person_Test.profilePhoto);
            Bitmap icon = mIconGenerator.makeIcon();
            return BitmapDescriptorFactory.fromBitmap(icon);
        }

    }


}
