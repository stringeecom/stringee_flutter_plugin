package com.stringee.stringeeflutterplugin;

import android.os.Handler;
import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StringeeRoomListener;
import com.stringee.video.RemoteParticipant;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeVideo;
import com.stringee.video.StringeeVideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class RoomManager implements StringeeRoomListener {
    private ClientWrapper _clientWrapper;
    private VideoConferenceManager _videoConferenceManager;
    private StringeeManager _manager;
    private Handler _handler;
    private StringeeRoom _stringeeRoom;
    private Result _connectRoomResult;
    private static final String TAG = "StringeeSDK";

    public RoomManager(ClientWrapper clientWrapper, VideoConferenceManager videoConferenceManager) {
        _clientWrapper = clientWrapper;
        _videoConferenceManager = videoConferenceManager;
        _manager = StringeeManager.getInstance();
        _handler = _manager.getHandler();
    }

    public StringeeRoom getStringeeRoom() {
        return _stringeeRoom;
    }

    public void connect(StringeeVideo stringeeVideo, String roomToken, Result result) {
        _connectRoomResult = result;
        _stringeeRoom = stringeeVideo.connect(_clientWrapper.getClient(), roomToken, this);
    }

    public void publish(){

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
                        _videoConferenceManager.getTracksMap().put(videoTrack.getId(), videoTrack);
                    }
                }
                Map bodyMap = new HashMap();
                bodyMap.put("room", Utils.convertRoomToMap(stringeeRoom));
                bodyMap.put("videoTracks", videoTracks);
                bodyMap.put("users", users);

                Log.d(TAG, "Room connect: success");
                Map map = new HashMap();
                map.put("status", true);
                map.put("code", 0);
                map.put("message", "Success");
                map.put("body", bodyMap);
                _connectRoomResult.success(map);

                _videoConferenceManager.getRoomsMap().put(stringeeRoom.getId(), RoomManager.this);
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
                Log.d(TAG, "Room connect: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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
