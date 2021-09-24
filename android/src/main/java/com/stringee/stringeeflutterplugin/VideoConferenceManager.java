package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.util.Log;

import com.stringee.video.StringeeVideo;
import com.stringee.video.StringeeVideoTrack;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class VideoConferenceManager {
    private ClientWrapper _clientWrapper;
    private StringeeManager _manager;
    private Handler _handler;
    private StringeeVideo _stringeeVideo;
    private Map<String, RoomManager> roomsMap = new HashMap<>();
    private Map<String, StringeeVideoTrack> tracksMap = new HashMap<>();
    private static final String TAG = "StringeeSDK";

    public VideoConferenceManager(ClientWrapper clientWrapper) {
        _clientWrapper = clientWrapper;
        _manager = StringeeManager.getInstance();
        _handler = _manager.getHandler();
        _stringeeVideo = new StringeeVideo();
    }
    
    public Map<String, RoomManager> getRoomsMap(){
        return roomsMap;
    }
    public Map<String, StringeeVideoTrack> getTracksMap(){
        return tracksMap;
    }
    

    public void connect(final String roomToken, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "Room connect: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = new RoomManager(_clientWrapper, this);
        roomManager.connect(_stringeeVideo, roomToken, result);
    }

    public void createLocalVideoTrack(String localId, final StringeeVideoTrack.Options options, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "createLocalVideoTrack: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        StringeeVideoTrack localVideoTrack = _stringeeVideo.createLocalVideoTrack(_manager.getContext(), options);
        tracksMap.put(localId, localVideoTrack);

        Log.d(TAG, "createLocalVideoTrack: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        map.put("body", Utils.convertVideoTrackToMap(localVideoTrack, localId));
        result.success(map);
    }

    public void release(final String roomId, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "release: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "release: false - -3 - Room is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        _stringeeVideo.release(roomManager.getStringeeRoom());

        Log.d(TAG, "release: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    public void publish(final String roomId, final String localId, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "release: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "release: false - -3 - Room is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        StringeeVideoTrack videoTrack = tracksMap.get(localId);
        if (videoTrack == null) {
            Log.d(TAG, "release: false - -3 - Video track is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        roomManager.publish(videoTrack, result);
    }
}
