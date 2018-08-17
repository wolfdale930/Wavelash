package com.tech.areeb.wavelash;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.gjiazhe.scrollparallaximageview.ScrollParallaxImageView;
import com.gjiazhe.scrollparallaximageview.parallaxstyle.VerticalMovingStyle;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RoomFragment extends Fragment{

    private List<RoomDetails> roomList;
    private ListView roomListView;

    public RoomFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomList = new ArrayList<>();
        RoomDetails bedroom = new RoomDetails(R.drawable.bedroom,"BedRoom");
        RoomDetails drawingroom = new RoomDetails(R.drawable.drawingroom,"Drawing Room");
        RoomDetails kitchen = new RoomDetails(R.drawable.kitchen,"Kitchen");
        RoomDetails diningroom = new RoomDetails(R.drawable.diningroom,"Dining Room");
        RoomDetails bathroom = new RoomDetails(R.drawable.bathroom,"BathRoom");
        roomList.add(bedroom);
        roomList.add(drawingroom);
        roomList.add(kitchen);
        roomList.add(diningroom);
        roomList.add(bathroom);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_room, container, false);
        roomListView = fragment.findViewById(R.id.roomListView);

        ArrayAdapter<RoomDetails> adapter = new ArrayAdapter<RoomDetails>(getActivity(),R.layout.list_item_layout,roomList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView==null){
                    convertView = getLayoutInflater().inflate(R.layout.list_item_layout,null);
                }

                ScrollParallaxImageView roomImage = convertView.findViewById(R.id.roomImage);
                TextView roomName = convertView.findViewById(R.id.roomName);
                roomImage.setParallaxStyles(new VerticalMovingStyle());

                int id = roomList.get(position).getImageDrawable();
                Picasso.with(getActivity()).load(id).resize(1500,900).centerCrop().noFade().into(roomImage);
                roomName.setText(roomList.get(position).getRoomName());

                return convertView;
            }
        };

        roomListView.setAdapter(adapter);

        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),RoomOnClickActivity.class);
                intent.putExtra("RoomId",i);
                intent.putExtra("Long",l);
                startActivity(intent);
            }
        });

        return fragment;
    }
}
