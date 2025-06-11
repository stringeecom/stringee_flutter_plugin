package com.stringee.stringeeflutterplugin.conference;

import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeRoomListener;
import com.stringee.stringeeflutterplugin.ClientWrapper;
import com.stringee.stringeeflutterplugin.StringeeFlutterPlugin;
import com.stringee.stringeeflutterplugin.common.Constants;
import com.stringee.stringeeflutterplugin.common.StringeeManager;
import com.stringee.stringeeflutterplugin.common.Utils;
import com.stringee.stringeeflutterplugin.common.StringeeEventType;
import com.stringee.video.RemoteParticipant;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeVideo;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;

public class RoomManager implements StringeeRoomListener {
    private final ClientWrapper clientWrapper;
    private final VideoConferenceManager videoConferenceManager;
    private StringeeRoom stringeeRoom;
    private Result connectRoomResult;

    public RoomManager(ClientWrapper clientWrapper, VideoConferenceManager videoConferenceManager) {
        this.clientWrapper = clientWrapper;
        this.videoConferenceManager = videoConferenceManager;
    }

    public void connect(final String roomToken, final Result result) {
        connectRoomResult = result;
        stringeeRoom = StringeeVideo.connect(clientWrapper.getClient(), roomToken, this);
    }

    public void publish(final VideoTrackManager trackManager, final Result result) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        stringeeRoom.publish(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "publish: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", ConferenceUtils.convertVideoTrackToMap(trackManager));
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "publish: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    public void unpublish(final StringeeVideoTrack videoTrack, final Result result) {
        stringeeRoom.unpublish(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    videoTrack.release();
                    Log.d(Constants.TAG, "unpublish: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "unpublish: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    public void subscribe(final VideoTrackManager trackManager,
                          final StringeeVideoTrack.Options options, final Result result) {
        stringeeRoom.subscribe(trackManager.getVideoTrack(), options, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "subscribe: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", ConferenceUtils.convertVideoTrackToMap(trackManager));
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "subscribe: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    public void unsubscribe(final StringeeVideoTrack videoTrack, final Result result) {
        stringeeRoom.unsubscribe(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "unsubscribe: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "unsubscribe: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    public void leave(final boolean allClient, final Result result) {
        stringeeRoom.leave(allClient, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    StringeeVideo.release(stringeeRoom);
                    Log.d(Constants.TAG, "leave: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "leave: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    public void sendMessage(final JSONObject msg, final Result result) {
        stringeeRoom.sendMessage(msg, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(Constants.TAG, "sendMessage: success");
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Log.d(Constants.TAG, "sendMessage: false - " + stringeeError.getCode() + " - " +
                            stringeeError.getMessage());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    @Override
    public void onConnected(StringeeRoom stringeeRoom) {
        Utils.post(() -> {
            List<RemoteParticipant> participantList =
                    RoomManager.this.stringeeRoom.getRemoteParticipants();
            ArrayList<Map<String, Object>> videoTracks = new ArrayList<>();
            ArrayList<Map<String, Object>> users = new ArrayList<>();
            for (int i = 0; i < participantList.size(); i++) {
                RemoteParticipant participant = participantList.get(i);
                users.add(ConferenceUtils.convertRoomUserToMap(participant));
                for (int j = 0; j < participant.getVideoTracks().size(); j++) {
                    StringeeVideoTrack videoTrack = participant.getVideoTracks().get(j);
                    VideoTrackManager videoTrackManager =
                            new VideoTrackManager(clientWrapper, videoTrack, "", false);
                    StringeeManager.getInstance()
                            .getTracksMap()
                            .put(videoTrack.getId(), videoTrackManager);
                    videoTracks.add(ConferenceUtils.convertVideoTrackInfoToMap(videoTrackManager));
                }
            }
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("room", ConferenceUtils.convertRoomToMap(stringeeRoom));
            bodyMap.put("videoTrackInfos", videoTracks);
            bodyMap.put("users", users);

            Log.d(Constants.TAG, "Room connect: success");
            Map<String, Object> map = new HashMap<>();
            map.put("status", true);
            map.put("code", 0);
            map.put("message", "Success");
            map.put("body", bodyMap);
            connectRoomResult.success(map);

            videoConferenceManager.getRoomsMap().put(stringeeRoom.getId(), RoomManager.this);
        });
    }

    @Override
    public void onDisconnected(StringeeRoom stringeeRoom) {

    }

    @Override
    public void onError(StringeeRoom stringeeRoom, StringeeError stringeeError) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "Room connect: false - " + stringeeError.getCode() + " - " +
                    stringeeError.getMessage());
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", stringeeError.getCode());
            map.put("message", stringeeError.getMessage());
            connectRoomResult.success(map);
        });
    }

    @Override
    public void onParticipantConnected(StringeeRoom stringeeRoom,
                                       RemoteParticipant remoteParticipant) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "didJoinRoom: " + remoteParticipant.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.ROOM_EVENT.getValue());
            map.put("event", "didJoinRoom");
            map.put("uuid", clientWrapper.getId());
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("roomId", stringeeRoom.getId());
            bodyMap.put("user", ConferenceUtils.convertRoomUserToMap(remoteParticipant));
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onParticipantDisconnected(StringeeRoom stringeeRoom,
                                          RemoteParticipant remoteParticipant) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "didLeaveRoom: " + remoteParticipant.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.ROOM_EVENT.getValue());
            map.put("event", "didLeaveRoom");
            map.put("uuid", clientWrapper.getId());
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("roomId", stringeeRoom.getId());
            bodyMap.put("user", ConferenceUtils.convertRoomUserToMap(remoteParticipant));
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onVideoTrackAdded(StringeeRoom stringeeRoom,
                                  StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            Log.d(Constants.TAG, "didAddVideoTrack: " + stringeeVideoTrack.getId());
            VideoTrackManager videoTrackManager =
                    new VideoTrackManager(clientWrapper, stringeeVideoTrack, "", false);
            StringeeManager.getInstance()
                    .getTracksMap()
                    .put(stringeeVideoTrack.getId(), videoTrackManager);
            Map<String, Object> map = new HashMap<>();
            map.put("nativeEventType", StringeeEventType.ROOM_EVENT.getValue());
            map.put("event", "didAddVideoTrack");
            map.put("uuid", clientWrapper.getId());
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("roomId", stringeeRoom.getId());
            bodyMap.put("videoTrackInfo",
                    ConferenceUtils.convertVideoTrackInfoToMap(videoTrackManager));
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeRoom stringeeRoom,
                                    StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            if (!stringeeVideoTrack.isLocal()) {
                Log.d(Constants.TAG, "didRemoveVideoTrack: " + stringeeVideoTrack.getId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.ROOM_EVENT.getValue());
                map.put("event", "didRemoveVideoTrack");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("roomId", stringeeRoom.getId());
                VideoTrackManager videoTrackManager = StringeeManager.getInstance()
                        .getTracksMap()
                        .get(stringeeVideoTrack.getId());
                if (videoTrackManager != null) {
                    bodyMap.put("videoTrackInfo",
                            ConferenceUtils.convertVideoTrackInfoToMap(videoTrackManager));
                }
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onMessage(StringeeRoom stringeeRoom, JSONObject jsonObject,
                          RemoteParticipant remoteParticipant) {
        Utils.post(() -> {
            try {
                Log.d(Constants.TAG, "didReceiveRoomMessage: " + jsonObject.toString() + " from: " +
                        remoteParticipant.getId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", StringeeEventType.ROOM_EVENT.getValue());
                map.put("event", "didReceiveRoomMessage");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("msg", Utils.convertJsonToMap(jsonObject));
                bodyMap.put("from", ConferenceUtils.convertRoomUserToMap(remoteParticipant));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            } catch (JSONException e) {
                Utils.reportException(RoomManager.class, e);
            }
        });
    }

    @Override
    public void onVideoTrackNotification(RemoteParticipant remoteParticipant,
                                         StringeeVideoTrack stringeeVideoTrack,
                                         StringeeVideoTrack.MediaType mediaType) {
//        Utils.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(Constants.TAG, "didReceiveVideoTrackControlNotification: " + remoteParticipant.getId());
//                Map<String,Object> map = new HashMap<>();
//                map.put("nativeEventType", RoomEvent.getValue());
//                map.put("event", "didReceiveVideoTrackControlNotification");
//                map.put("uuid", clientWrapper.getId());
//                Map<String,Object> bodyMap = new HashMap<>();
//                bodyMap.put("roomId", _stringeeRoom.getId());
//                bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(stringeeVideoTrack));
//                bodyMap.put("from", Utils.convertRoomUserToMap(remoteParticipant));
//                map.put("body", bodyMap);
//                StringeeFlutterPlugin.eventSink.success(map);
//            }
//        });
    }
}
