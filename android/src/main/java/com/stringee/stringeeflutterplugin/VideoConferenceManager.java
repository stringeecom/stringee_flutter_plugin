package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.video.RemoteParticipant;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeVideo;
import com.stringee.video.StringeeVideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class VideoConferenceManager implements com.stringee.listener.StringeeRoomListener {
    private ClientWrapper _clientWrapper;
    private StringeeManager _manager;
    private Handler _handler;
    private StringeeVideo _stringeeVideo;
    private StringeeRoom _stringeeRoom;
    private Result _connectRoomResult;
    private StringeeVideoTrack localVideoTrack;
    private StringeeVideoTrack captureVideoTrack;
    private Map<String, StringeeVideoTrack> tracksMap = new HashMap<>();
    private static final String TAG = "StringeeSDK";

    public VideoConferenceManager(ClientWrapper clientWrapper) {
        _clientWrapper = clientWrapper;
        _manager = StringeeManager.getInstance();
        _handler = _manager.getHandler();
        _stringeeVideo = new StringeeVideo();
    }

    public void connect(final String roomToken, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "connect: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }
        _connectRoomResult = result;
        _stringeeRoom = _stringeeVideo.connect(_clientWrapper.getClient(), roomToken, this);
    }

    public void createLocalVideoTrack(final StringeeVideoTrack.Options options, final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "createLocalVideoTrack: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        localVideoTrack = _stringeeVideo.createLocalVideoTrack(_manager.getContext(), options);

        Log.d(TAG, "createLocalVideoTrack: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        map.put("body", Utils.convertVideoTrackToMap(localVideoTrack));
        result.success(map);
    }

    public void release(final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "release: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        _stringeeVideo.release(_stringeeRoom);

        Log.d(TAG, "release: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    public void publish(final Result result) {
        if (!_clientWrapper.isConnected()) {
            Log.d(TAG, "release: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        _stringeeVideo.release(_stringeeRoom);

        Log.d(TAG, "release: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    @Override
    public void onConnected(StringeeRoom stringeeRoom) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                List<RemoteParticipant> participantList = _stringeeRoom.getRemoteParticipants();
                ArrayList videoTracks = new ArrayList();
                ArrayList users = new ArrayList();
                for (int i = 0; i < participantList.size(); i++) {
                    RemoteParticipant participant = participantList.get(i);
                    users.add(Utils.convertRoomUserToMap(participant));
                    for (int j = 0; j < participant.getVideoTracks().size(); j++) {
                        StringeeVideoTrack videoTrack = participant.getVideoTracks().get(j);
                        videoTracks.add(Utils.convertVideoTrackToMap(videoTrack));
                        tracksMap.put(videoTrack.getId(), videoTrack);
                    }
                }
                Map bodyMap = new java.util.HashMap();
                bodyMap.put("room", Utils.convertRoomToMap(stringeeRoom));
                bodyMap.put("videoTracks", videoTracks);
                bodyMap.put("users", users);
                Log.d(TAG, "connect: success");
                Map map = new HashMap();
                map.put("status", true);
                map.put("code", 0);
                map.put("message", "Success");
                map.put("body", bodyMap);
                _connectRoomResult.success(map);
            }
        });
    }

    @Override
    public void onDisconnected(StringeeRoom stringeeRoom) {

    }

    @Override
    public void onError(StringeeRoom stringeeRoom, StringeeError stringeeError) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "connect: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                Map map = new HashMap();
                map.put("status", false);
                map.put("code", stringeeError.getCode());
                map.put("message", stringeeError.getMessage());
                _connectRoomResult.success(map);
            }
        });
    }

    @Override
    public void onParticipantConnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {

    }

    @Override
    public void onParticipantDisconnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {

    }

    @Override
    public void onVideoTrackAdded(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {

    }

    @Override
    public void onVideoTrackRemoved(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {

    }

    @Override
    public void onMessage(StringeeRoom stringeeRoom, org.json.JSONObject jsonObject, RemoteParticipant remoteParticipant) {

    }

    @Override
    public void onVideoTrackNotification(RemoteParticipant remoteParticipant, StringeeVideoTrack stringeeVideoTrack) {

    }
}
