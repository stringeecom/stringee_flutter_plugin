package com.stringee.stringeeflutterplugin;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.RoomEvent;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.flutter.plugin.common.MethodChannel.Result;

public class RoomManager implements StringeeRoomListener {
    private ClientWrapper clientWrapper;
    private VideoConferenceManager videoConferenceManager;
    private StringeeManager stringeeManager;
    private StringeeRoom stringeeRoom;
    private Result connectRoomResult;

    public RoomManager(ClientWrapper clientWrapper, VideoConferenceManager videoConferenceManager) {
        this.clientWrapper = clientWrapper;
        this.videoConferenceManager = videoConferenceManager;
        this.stringeeManager = StringeeManager.getInstance();
    }

    public void connect(final String roomToken, final Result result) {
        connectRoomResult = result;
        stringeeRoom = StringeeVideo.connect(clientWrapper.getClient(), roomToken, this);
    }

    public void release(final Result result) {
        StringeeVideo.release(stringeeRoom);
        Utils.sendSuccessResponse("release", null, result);
    }

    public void publish(final VideoTrackManager trackManager, final StringeeVideoTrack.Options options, final Result result) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        stringeeRoom.publish(videoTrack, options, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("publish", Utils.convertVideoTrackToMap(trackManager), result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("publish", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    public void unpublish(final StringeeVideoTrack videoTrack, final Result result) {
        stringeeRoom.unpublish(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        videoTrack.release();
                        Utils.sendSuccessResponse("unpublish", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("unpublish", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    public void subscribe(final VideoTrackManager trackManager, final StringeeVideoTrack.Options options, final Result result) {
        stringeeRoom.subscribe(trackManager.getVideoTrack(), options, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("subscribe", Utils.convertVideoTrackToMap(trackManager), result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("subscribe", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    public void unsubscribe(final StringeeVideoTrack videoTrack, final Result result) {
        stringeeRoom.unsubscribe(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("unsubscribe", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("unsubscribe", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    public void leave(final boolean allClient, final Result result) {
        stringeeRoom.leave(allClient, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        StringeeVideo.release(stringeeRoom);
                        Utils.sendSuccessResponse("leave", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("leave", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    public void sendMessage(final JSONObject msg, final Result result) {
        stringeeRoom.sendMessage(msg, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("sendMessage", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("sendMessage", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    @Override
    public void onConnected(StringeeRoom stringeeRoom) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                List<RemoteParticipant> participantList = RoomManager.this.stringeeRoom.getRemoteParticipants();
                List<Map<String, Object>> videoTracks = new ArrayList<>();
                List<Map<String, Object>> users = new ArrayList<>();
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
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("room", Utils.convertRoomToMap(stringeeRoom));
                bodyMap.put("videoTrackInfos", videoTracks);
                bodyMap.put("users", users);
                Utils.sendSuccessResponse("Room connect", bodyMap, connectRoomResult);

                videoConferenceManager.getRoomsMap().put(stringeeRoom.getId(), RoomManager.this);

                stringeeManager.startAudioManager(stringeeManager.getContext(), new AudioManagerEvents() {
                    @Override
                    public void onAudioDeviceChanged(final AudioDevice selectedAudioDevice, final Set<AudioDevice> availableAudioDevices) {
                        stringeeManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(StringeeFlutterPlugin.TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
                                List<AudioDevice> audioDeviceList = new ArrayList<>(availableAudioDevices);
                                List<Integer> codeList = new ArrayList<>();
                                for (int i = 0; i < audioDeviceList.size(); i++) {
                                    codeList.add(audioDeviceList.get(i).ordinal());
                                }
                                Map<String, Object> map = new HashMap<>();
                                map.put("nativeEventType", RoomEvent.getValue());
                                map.put("event", "didChangeAudioDevice");
                                map.put("uuid", clientWrapper.getId());
                                Map<String, Object> bodyMap = new HashMap<>();
                                bodyMap.put("code", selectedAudioDevice.ordinal());
                                bodyMap.put("codeList", codeList);
                                map.put("body", bodyMap);
                                StringeeFlutterPlugin.eventSink.success(map);
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
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Utils.sendErrorResponse("Room connect", stringeeError.getCode(), stringeeError.getMessage(), connectRoomResult);
            }
        });
    }

    @Override
    public void onParticipantConnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "didJoinRoom: " + remoteParticipant.getId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didJoinRoom");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("user", Utils.convertRoomUserToMap(remoteParticipant));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onParticipantDisconnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "didLeaveRoom: " + remoteParticipant.getId());
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didLeaveRoom");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("user", Utils.convertRoomUserToMap(remoteParticipant));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onVideoTrackAdded(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(StringeeFlutterPlugin.TAG, "didAddVideoTrack: " + stringeeVideoTrack.getId());
                VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, stringeeVideoTrack, "", false);
                stringeeManager.getTracksMap().put(stringeeVideoTrack.getId(), videoTrackManager);
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didAddVideoTrack");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("videoTrackInfo", Utils.convertVideoTrackInfoToMap(videoTrackManager));
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (!stringeeVideoTrack.isLocal()) {
                    Log.d(StringeeFlutterPlugin.TAG, "didRemoveVideoTrack: " + stringeeVideoTrack.getId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", RoomEvent.getValue());
                    map.put("event", "didRemoveVideoTrack");
                    map.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap = new HashMap<>();
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
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(StringeeFlutterPlugin.TAG, "didReceiveRoomMessage: msg - " + jsonObject.toString() + " - from: " + remoteParticipant.getId());
                    Map<String, Object> map = new HashMap<>();
                    map.put("nativeEventType", RoomEvent.getValue());
                    map.put("event", "didReceiveRoomMessage");
                    map.put("uuid", clientWrapper.getId());
                    Map<String, Object> bodyMap = new HashMap<>();
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
        stringeeManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean enable = true;
                switch (mediaType) {
                    case AUDIO:
                        enable = stringeeVideoTrack.audioEnabled();
                        break;
                    case VIDEO:
                        enable = stringeeVideoTrack.videoEnabled();
                        break;
                }
                Log.d(StringeeFlutterPlugin.TAG, "didTrackMediaStateChange: mediaType: " + mediaType.name() + " - enable: " + enable);
                Map<String, Object> map = new HashMap<>();
                map.put("nativeEventType", RoomEvent.getValue());
                map.put("event", "didTrackMediaStateChange");
                map.put("uuid", clientWrapper.getId());
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("videoTrackInfo", Utils.convertVideoTrackInfoToMap(stringeeManager.getTracksMap().get(stringeeVideoTrack.getId())));
                bodyMap.put("from", Utils.convertRoomUserToMap(remoteParticipant));
                switch (mediaType) {
                    case AUDIO:
                        bodyMap.put("mediaType", 0);
                        break;
                    case VIDEO:
                        bodyMap.put("mediaType", 1);
                        break;
                }
                bodyMap.put("enable", enable);
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }
}
