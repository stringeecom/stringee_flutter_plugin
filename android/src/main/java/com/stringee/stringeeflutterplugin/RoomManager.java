package com.stringee.stringeeflutterplugin;

import static com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType.RoomEvent;

import android.util.Log;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeRoomListener;
import com.stringee.stringeeflutterplugin.StringeeManager.StringeeEventType;
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
    private final StringeeManager stringeeManager;
    private StringeeRoom stringeeRoom;
    private Result _connectRoomResult;

    public RoomManager(ClientWrapper clientWrapper, VideoConferenceManager videoConferenceManager) {
        this.clientWrapper = clientWrapper;
        this.videoConferenceManager = videoConferenceManager;
        stringeeManager = StringeeManager.getInstance();
    }

    public void connect(final String roomToken, final Result result) {
        _connectRoomResult = result;
        stringeeRoom = StringeeVideo.connect(clientWrapper.getClient(), roomToken, this);
    }

    public void publish(final VideoTrackManager trackManager, final Result result) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        stringeeRoom.publish(videoTrack, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("publish");
                    Map<String, Object> videoTrackMap = Utils.convertVideoTrackToMap(trackManager);
                    Logging.d(videoTrackMap.toString());
                    map.put("body", videoTrackMap);
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("publish", stringeeError.getCode(), stringeeError.getMessage());
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
                    Map<String, Object> map = Utils.createSuccessMap("unpublish");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("unpublish", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    public void subscribe(final VideoTrackManager trackManager, final StringeeVideoTrack.Options options, final Result result) {
        stringeeRoom.subscribe(trackManager.getVideoTrack(), options, new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("subscribe");
                    Map<String, Object> videoTrackMap = Utils.convertVideoTrackToMap(trackManager);
                    Logging.d(videoTrackMap.toString());
                    map.put("body", videoTrackMap);
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("subscribe", stringeeError.getCode(), stringeeError.getMessage());
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
                    Map<String, Object> map = Utils.createSuccessMap("unsubscribe");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("unsubscribe", stringeeError.getCode(), stringeeError.getMessage());
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
                    Map<String, Object> map = Utils.createSuccessMap("leave");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("leave", stringeeError.getCode(), stringeeError.getMessage());
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
                    Map<String, Object> map = Utils.createSuccessMap("sendMessage");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("sendMessage", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    @Override
    public void onConnected(StringeeRoom stringeeRoom) {
        Utils.post(() -> {
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
            Map<String, Object> map = Utils.createSuccessMap("connectRoom");
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("room", Utils.convertRoomToMap(stringeeRoom));
            bodyMap.put("videoTrackInfos", videoTracks);
            bodyMap.put("users", users);
            Logging.d(bodyMap.toString());
            map.put("body", bodyMap);
            _connectRoomResult.success(map);

            videoConferenceManager.getRoomsMap().put(stringeeRoom.getId(), RoomManager.this);

            stringeeManager.startAudioManager(stringeeManager.getContext(), (selectedAudioDevice, availableAudioDevices) -> Utils.post(() -> {
                Logging.d("onAudioDeviceChanged: " + availableAudioDevices + ", " + "selected: " + selectedAudioDevice);
                switch (selectedAudioDevice) {
                    case BLUETOOTH:
                    case WIRED_HEADSET:
                        stringeeManager.setSpeakerphoneOn(false);
                        break;
                    default:
                        stringeeManager.setSpeakerphoneOn(true);
                        break;
                }
            }));
        });
    }

    @Override
    public void onDisconnected(StringeeRoom stringeeRoom) {

    }

    @Override
    public void onError(StringeeRoom stringeeRoom, StringeeError stringeeError) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createErrorMap("roomConnect", stringeeError.getCode(), stringeeError.getMessage());
            _connectRoomResult.success(map);
        });
    }

    @Override
    public void onParticipantConnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("didJoinRoom", clientWrapper.getId(), StringeeEventType.RoomEvent);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("roomId", stringeeRoom.getId());
            bodyMap.put("user", Utils.convertRoomUserToMap(remoteParticipant));
            Logging.d("participant: " + bodyMap);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onParticipantDisconnected(StringeeRoom stringeeRoom, RemoteParticipant remoteParticipant) {
        Utils.post(() -> {
            Map<String, Object> map = Utils.createEventMap("didLeaveRoom", clientWrapper.getId(), StringeeEventType.RoomEvent);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("roomId", stringeeRoom.getId());
            bodyMap.put("user", Utils.convertRoomUserToMap(remoteParticipant));
            Logging.d("participant: " + bodyMap);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onVideoTrackAdded(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, stringeeVideoTrack, "", false);
            stringeeManager.getTracksMap().put(stringeeVideoTrack.getId(), videoTrackManager);
            Map<String, Object> map = Utils.createEventMap("didAddVideoTrack", clientWrapper.getId(), StringeeEventType.RoomEvent);
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("roomId", stringeeRoom.getId());
            bodyMap.put("videoTrackInfo", Utils.convertVideoTrackInfoToMap(videoTrackManager));
            Logging.d("track: " + bodyMap);
            map.put("body", bodyMap);
            StringeeFlutterPlugin.eventSink.success(map);
        });
    }

    @Override
    public void onVideoTrackRemoved(StringeeRoom stringeeRoom, StringeeVideoTrack stringeeVideoTrack) {
        Utils.post(() -> {
            if (!stringeeVideoTrack.isLocal()) {
                Map<String, Object> map = Utils.createEventMap("didRemoveVideoTrack", clientWrapper.getId(), StringeeEventType.RoomEvent);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("roomId", stringeeRoom.getId());
                VideoTrackManager trackManager = stringeeManager.getTracksMap().get(stringeeVideoTrack.getId());
                if (trackManager != null) {
                    bodyMap.put("videoTrackInfo", Utils.convertVideoTrackInfoToMap(trackManager));
                }
                Logging.d("track: " + bodyMap);
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            }
        });
    }

    @Override
    public void onMessage(StringeeRoom stringeeRoom, JSONObject jsonObject, RemoteParticipant remoteParticipant) {
        Utils.post(() -> {
            try {
                Map<String, Object> map = Utils.createEventMap("didReceiveRoomMessage", clientWrapper.getId(), StringeeEventType.RoomEvent);
                Map<String, Object> bodyMap = new HashMap<>();
                bodyMap.put("roomId", stringeeRoom.getId());
                bodyMap.put("msg", Utils.convertJsonToMap(jsonObject));
                bodyMap.put("from", Utils.convertRoomUserToMap(remoteParticipant));
                Logging.d("message: " + bodyMap);
                map.put("body", bodyMap);
                StringeeFlutterPlugin.eventSink.success(map);
            } catch (JSONException e) {
                Logging.e(RoomManager.class, e);
            }
        });
    }

    @Override
    public void onVideoTrackNotification(RemoteParticipant remoteParticipant, StringeeVideoTrack stringeeVideoTrack, StringeeVideoTrack.MediaType mediaType) {
//        Utils.post(() -> {
//            Map<String, Object> map = Utils.createEventMap("didReceiveVideoTrackControlNotification", clientWrapper.getId(), StringeeEventType.RoomEvent);
//            Map<String, Object> bodyMap = new HashMap<>();
//            bodyMap.put("roomId", stringeeRoom.getId());
//            VideoTrackManager trackManager = stringeeManager.getTracksMap().get(stringeeVideoTrack.getId());
//            if (trackManager != null) {
//                bodyMap.put("videoTrack", Utils.convertVideoTrackToMap(trackManager));
//            }
//            bodyMap.put("from", Utils.convertRoomUserToMap(remoteParticipant));
//            Logging.d("track: " + bodyMap);
//            map.put("body", bodyMap);
//            StringeeFlutterPlugin.eventSink.success(map);
//        });
    }
}
