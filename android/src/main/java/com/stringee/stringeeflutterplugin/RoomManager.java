package com.stringee.stringeeflutterplugin;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.RoomEvent;

import android.os.Handler;
import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeRoomListener;
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
    private ClientWrapper clientWrapper;
    private VideoConferenceManager videoConferenceManager;
    private StringeeManager stringeeManager;
    private Handler handler;
    private StringeeRoom _stringeeRoom;
    private Result _connectRoomResult;

    private static final String TAG = "StringeeSDK";

    public RoomManager(ClientWrapper clientWrapper, VideoConferenceManager videoConferenceManager) {
        this.clientWrapper = clientWrapper;
        this.videoConferenceManager = videoConferenceManager;
        stringeeManager = StringeeManager.getInstance();
        handler = stringeeManager.getHandler();
    }

    public void connect(final StringeeVideo stringeeVideo, final String roomToken, final Result result) {
        _connectRoomResult = result;
        _stringeeRoom = stringeeVideo.connect(clientWrapper.getClient(), roomToken, this);
    }

    public void publish(final VideoTrackManager trackManager, final Result result) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        _stringeeRoom.publish(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "publish: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", Utils.convertVideoTrackToMap(trackManager));
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "publish: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    public void unpublish(final StringeeVideoTrack videoTrack, final Result result) {
        _stringeeRoom.unpublish(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        videoTrack.release();
                        Log.d(TAG, "unpublish: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "unpublish: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    public void subscribe(final VideoTrackManager trackManager, final StringeeVideoTrack.Options options, final Result result) {
        _stringeeRoom.subscribe(trackManager.getVideoTrack(), options, new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "subscribe: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", Utils.convertVideoTrackToMap(trackManager));
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "subscribe: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    public void unsubscribe(final StringeeVideoTrack videoTrack, final Result result) {
        _stringeeRoom.unsubscribe(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "unsubscribe: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "unsubscribe: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    public void leave(final boolean allClient, final Result result) {
        _stringeeRoom.leave(allClient, new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        videoConferenceManager.getStringeeVideo().release(_stringeeRoom);
                        Log.d(TAG, "leave: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "leave: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    public void sendMessage(final JSONObject msg, final Result result) {
        _stringeeRoom.sendMessage(msg, new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "sendMessage: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "sendMessage: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        });
    }

    @Override
    public void onConnected(StringeeRoom stringeeRoom) {
        handler.post(new Runnable() {
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
                        VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, videoTrack, "", false);
                        stringeeManager.getTracksMap().put(videoTrack.getId(), videoTrackManager);
                        videoTracks.add(Utils.convertVideoTrackInfoToMap(videoTrackManager));
                    }
                }
                Map bodyMap = new HashMap();
                bodyMap.put("room", Utils.convertRoomToMap(stringeeRoom));
                bodyMap.put("videoTrackInfos", videoTracks);
                bodyMap.put("users", users);

                Log.d(TAG, "Room connect: success");
                Map map = new HashMap();
                map.put("status", true);
                map.put("code", 0);
                map.put("message", "Success");
                map.put("body", bodyMap);
                _connectRoomResult.success(map);

                videoConferenceManager.getRoomsMap().put(stringeeRoom.getId(), RoomManager.this);

                stringeeManager.startAudioManager(RoomEvent.getValue(), clientWrapper.getId());
            }
        });
    }

    @Override
    public void onDisconnected(StringeeRoom stringeeRoom) {

    }

    @Override
    public void onError(StringeeRoom stringeeRoom, StringeeError stringeeError) {
        handler.post(new Runnable() {
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didJoinRoom: " + remoteParticipant.getId());
                Map map = new HashMap();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didJoinRoom");
                map.put("uuid", clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("user", Utils.convertRoomUserToMap(remoteParticipant));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onParticipantDisconnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didLeaveRoom: " + remoteParticipant.getId());
                Map map = new HashMap();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didLeaveRoom");
                map.put("uuid", clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("user", Utils.convertRoomUserToMap(remoteParticipant));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onVideoTrackAdded(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didAddVideoTrack: " + stringeeVideoTrack.getId());
                VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, stringeeVideoTrack, "", false);
                stringeeManager.getTracksMap().put(stringeeVideoTrack.getId(), videoTrackManager);
                Map map = new HashMap();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didAddVideoTrack");
                map.put("uuid", clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("videoTrackInfo", Utils.convertVideoTrackInfoToMap(videoTrackManager));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!stringeeVideoTrack.isLocal()) {
                    Log.d(TAG, "didRemoveVideoTrack: " + stringeeVideoTrack.getId());
                    Map map = new HashMap();
                    map.put("nativeEventType", RoomEvent.getValue());
                    map.put("event", "didRemoveVideoTrack");
                    map.put("uuid", clientWrapper.getId());
                    Map bodyMap = new HashMap();
                    bodyMap.put("roomId", stringeeRoom.getId());
                    bodyMap.put("videoTrackInfo", Utils.convertVideoTrackInfoToMap(stringeeManager.getTracksMap().get(stringeeVideoTrack.getId())));
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                }
            }
        });
    }

    @Override
    public void onMessage(StringeeRoom stringeeRoom, JSONObject jsonObject, RemoteParticipant remoteParticipant) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "didReceiveRoomMessage: " + jsonObject.toString() + " from: " + remoteParticipant.getId());
                    Map map = new HashMap();
                    map.put("nativeEventType", RoomEvent.getValue());
                    map.put("event", "didReceiveRoomMessage");
                    map.put("uuid", clientWrapper.getId());
                    Map bodyMap = new HashMap();
                    bodyMap.put("roomId", stringeeRoom.getId());
                    bodyMap.put("msg", Utils.convertJsonToMap(jsonObject));
                    bodyMap.put("from", Utils.convertRoomUserToMap(remoteParticipant));
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin.eventSink.success(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onVideoTrackNotification(RemoteParticipant remoteParticipant, StringeeVideoTrack stringeeVideoTrack, StringeeVideoTrack.MediaType mediaType) {
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG, "didReceiveVideoTrackControlNotification: " + remoteParticipant.getId());
//                Map map = new HashMap();
//                map.put("nativeEventType", RoomEvent.getValue());
//                map.put("event", "didReceiveVideoTrackControlNotification");
//                map.put("uuid", clientWrapper.getId());
//                Map bodyMap = new HashMap();
//                bodyMap.put("roomId", _stringeeRoom.getId());
//                bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(stringeeVideoTrack));
//                bodyMap.put("from", Utils.convertRoomUserToMap(remoteParticipant));
//                map.put("body", bodyMap);
//                StringeeFlutterPlugin.eventSink.success(map);
//            }
//        });
    }
}
