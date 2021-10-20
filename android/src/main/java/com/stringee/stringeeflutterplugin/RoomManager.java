package com.stringee.stringeeflutterplugin;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.RoomEvent;

import android.os.Handler;
import android.util.Log;

import com.stringee.common.StringeeAudioManager.AudioDevice;
import com.stringee.common.StringeeAudioManager.AudioManagerEvents;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeRoomListener;
import com.stringee.video.RemoteParticipant;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeVideo;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void connect(final StringeeVideo stringeeVideo, final String roomToken, final Result result) {
        _connectRoomResult = result;
        _stringeeRoom = stringeeVideo.connect(_clientWrapper.getClient(), roomToken, this);
    }

    public void publish(final StringeeVideoTrack videoTrack, final String localId, final Result result) {
        _stringeeRoom.publish(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "publish: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        map.put("body", Utils.convertVideoTrackToMap(videoTrack));
                        result.success(map);

                        _manager.getTracksMap().put(videoTrack.getId(), new VideoTrackManager(videoTrack, false));
                        _manager.getTracksMap().remove(localId);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                _handler.post(new Runnable() {
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
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
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
                _handler.post(new Runnable() {
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

    public void subscribe(final StringeeVideoTrack videoTrack, final StringeeVideoTrack.Options options, final Result result) {
        _stringeeRoom.subscribe(videoTrack, options, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "subscribe: success");
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
                _handler.post(new Runnable() {
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
                _handler.post(new Runnable() {
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
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "unsubscribe: success");
                        Map map = new HashMap();
                        map.put("status", true);
                        map.put("code", 0);
                        map.put("message", "Success");
                        result.success(map);
//                        Log.d(TAG, "unsubscribe: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
//                        Map map = new HashMap();
//                        map.put("status", false);
//                        map.put("code", stringeeError.getCode());
//                        map.put("message", stringeeError.getMessage());
//                        result.success(map);
                    }
                });
            }
        });
    }

    public void leave(final boolean allClient, final Result result) {
        _stringeeRoom.leave(allClient, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
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
                _handler.post(new Runnable() {
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
        _stringeeRoom.sendMessge(msg, new StatusListener() {
            @Override
            public void onSuccess() {
                _handler.post(new Runnable() {
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
                _handler.post(new Runnable() {
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
                        _manager.getTracksMap().put(videoTrack.getId(), new VideoTrackManager(videoTrack, false));
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

                _manager.startAudioManager(_manager.getContext(), new AudioManagerEvents() {
                    @Override
                    public void onAudioDeviceChanged(final AudioDevice selectedAudioDevice, final Set<AudioDevice> availableAudioDevices) {
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "onAudioDeviceChanged: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
                                switch (selectedAudioDevice) {
                                    case BLUETOOTH:
                                    case WIRED_HEADSET:
                                        _manager.setSpeakerphoneOn(false);
                                        break;
                                    default:
                                        _manager.setSpeakerphoneOn(true);
                                        break;
                                }
                            }
                        });
                    }
                });
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
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didJoinRoom: " + remoteParticipant.getId());
                Map map = new HashMap();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didJoinRoom");
                map.put("uuid", _clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("user", Utils.convertRoomUserToMap(remoteParticipant));
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onParticipantDisconnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didLeaveRoom: " + remoteParticipant.getId());
                Map map = new HashMap();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didLeaveRoom");
                map.put("uuid", _clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("user", Utils.convertRoomUserToMap(remoteParticipant));
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onVideoTrackAdded(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didAddVideoTrack: " + stringeeVideoTrack.getId());
                _manager.getTracksMap().put(stringeeVideoTrack.getId(), new VideoTrackManager(stringeeVideoTrack, false));
                Map map = new HashMap();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didAddVideoTrack");
                map.put("uuid", _clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(stringeeVideoTrack));
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didRemoveVideoTrack: " + stringeeVideoTrack.getId());
                Map map = new HashMap();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didRemoveVideoTrack");
                map.put("uuid", _clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(stringeeVideoTrack));
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }

    @Override
    public void onMessage(StringeeRoom stringeeRoom, JSONObject jsonObject, RemoteParticipant remoteParticipant) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "didReceiveRoomMessage: " + jsonObject.toString() + " from: " + remoteParticipant.getId());
                    Map map = new HashMap();
                    map.put("nativeEventType", RoomEvent.getValue());
                    map.put("event", "didReceiveRoomMessage");
                    map.put("uuid", _clientWrapper.getId());
                    Map bodyMap = new HashMap();
                    bodyMap.put("roomId", stringeeRoom.getId());
                    bodyMap.put("msg", Utils.convertJsonToMap(jsonObject));
                    bodyMap.put("from", Utils.convertRoomUserToMap(remoteParticipant));
                    map.put("body", bodyMap);
                    StringeeFlutterPlugin._eventSink.success(map);
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onVideoTrackNotification(RemoteParticipant remoteParticipant, StringeeVideoTrack stringeeVideoTrack) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "didReceiveVideoTrackControlNotification: " + remoteParticipant.getId());
                Map map = new HashMap();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didReceiveVideoTrackControlNotification");
                map.put("uuid", _clientWrapper.getId());
                Map bodyMap = new HashMap();
                bodyMap.put("roomId", _stringeeRoom.getId());
                bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(stringeeVideoTrack));
                bodyMap.put("from", Utils.convertRoomUserToMap(remoteParticipant));
                map.put("body", bodyMap);
                StringeeFlutterPlugin._eventSink.success(map);
            }
        });
    }
}
