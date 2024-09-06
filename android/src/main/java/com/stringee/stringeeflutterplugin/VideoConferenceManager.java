package com.stringee.stringeeflutterplugin;

import android.app.Activity;
import android.content.Intent;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.video.StringeeVideo;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class VideoConferenceManager {
    private final ClientWrapper clientWrapper;
    private final StringeeManager stringeeManager;
    private final Map<String, RoomManager> roomsMap = new HashMap<>();

    public VideoConferenceManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
        stringeeManager = StringeeManager.getInstance();
    }

    public Map<String, RoomManager> getRoomsMap() {
        return roomsMap;
    }

    /**
     * Connect room
     *
     * @param roomToken room token
     * @param result    result
     */
    public void connect(final String roomToken, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("roomConnect");
            result.success(map);
            return;
        }

        RoomManager roomManager = new RoomManager(clientWrapper, this);
        roomManager.connect(roomToken, result);
    }

    /**
     * Create local video track
     *
     * @param options options
     * @param result  result
     */
    public void createLocalVideoTrack(final StringeeVideoTrack.Options options, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("createLocalVideoTrack");
            result.success(map);
            return;
        }

        StringeeVideoTrack localVideoTrack = StringeeVideo.createLocalVideoTrack(stringeeManager.getContext(), options, new StatusListener() {
            @Override
            public void onSuccess() {

            }
        });
        String localId = Utils.createLocalId();
        VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, localVideoTrack, localId, false);
        stringeeManager.getTracksMap().put(localId, videoTrackManager);

        Map<String, Object> map = Utils.createSuccessMap("createLocalVideoTrack");
        Map<String, Object> videoTrackMap = Utils.convertLocalVideoTrackToMap(videoTrackManager, clientWrapper.getClient().getUserId());
        Logging.d(videoTrackMap.toString());
        map.put("body", videoTrackMap);
        result.success(map);
    }

    /**
     * Create capture screen track
     *
     * @param result result
     */
    public void createCaptureScreenTrack(final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("createCaptureScreenTrack");
            result.success(map);
            return;
        }

        final int REQUEST_CODE = new Random().nextInt(65536);
        stringeeManager.getCaptureManager().createCapture(REQUEST_CODE, new ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                Utils.post(() -> {
                    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        stringeeManager.getCaptureManager().getScreenCapture().createCapture(data, new CallbackListener<StringeeVideoTrack>() {
                            @Override
                            public void onSuccess(StringeeVideoTrack stringeeVideoTrack) {
                                Utils.post(() -> {
                                    String localId = Utils.createLocalId();
                                    VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, stringeeVideoTrack, localId, false);
                                    stringeeManager.getTracksMap().put(localId, videoTrackManager);
                                    Map<String, Object> map = Utils.createSuccessMap("createCaptureScreenTrack");
                                    Map<String, Object> videoTrackMap = Utils.convertLocalVideoTrackToMap(videoTrackManager, clientWrapper.getClient().getUserId());
                                    Logging.d(videoTrackMap.toString());
                                    map.put("body", videoTrackMap);
                                    result.success(map);
                                });
                            }

                            @Override
                            public void onError(StringeeError stringeeError) {
                                super.onError(stringeeError);
                                Utils.post(() -> {
                                    Map<String, Object> map = Utils.createErrorMap("createCaptureScreenTrack", stringeeError.getCode(), stringeeError.getMessage());
                                    result.success(map);
                                });
                            }
                        });
                    } else {
                        Map<String, Object> map = Utils.createErrorMap("createCaptureScreenTrack", -101, "Capture request is rejected");
                        result.success(map);
                    }
                });
                return false;
            }
        });
    }

    /**
     * Publish video track
     *
     * @param roomId  room id
     * @param localId video track id
     * @param result  result
     */
    public void publish(final String roomId, final String localId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("publish");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("publish", "Room");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("publish", "Video track");
            result.success(map);
            return;
        }

        roomManager.publish(videoTrackManager, result);
    }

    /**
     * Unpublish video track
     *
     * @param roomId  room id
     * @param trackId video track id
     * @param result  result
     */
    public void unpublish(final String roomId, final String trackId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("unpublish");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("unpublish", "Room");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(trackId);
        if (videoTrackManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("unpublish", "Video track");
            result.success(map);
            return;
        }

        roomManager.unpublish(videoTrackManager.getVideoTrack(), result);
    }

    /**
     * Subscribe video track
     *
     * @param roomId  room id
     * @param trackId video track id
     * @param options options
     * @param result  result
     */
    public void subscribe(final String roomId, final String trackId, final StringeeVideoTrack.Options options, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("subscribe");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("subscribe", "Room");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(trackId);
        if (videoTrackManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("subscribe", "Video track");
            result.success(map);
            return;
        }

        roomManager.subscribe(videoTrackManager, options, result);
    }

    /**
     * Unsubscribe video track
     *
     * @param roomId  room id
     * @param trackId video track id
     * @param result  result
     */
    public void unsubscribe(final String roomId, final String trackId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("unsubscribe");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("unsubscribe", "Room");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(trackId);
        if (videoTrackManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("unsubscribe", "Video track");
            result.success(map);
            return;
        }

        roomManager.unsubscribe(videoTrackManager.getVideoTrack(), result);
    }

    /**
     * Leave room
     *
     * @param roomId    room id
     * @param allClient true: leave all clients, false: leave current client
     * @param result    result
     */
    public void leave(final String roomId, final boolean allClient, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("leave");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("leave", "Room");
            result.success(map);
            return;
        }

        roomManager.leave(allClient, result);
    }

    /**
     * Send message in room
     *
     * @param roomId room id
     * @param msg    message
     * @param result result
     */
    public void sendMessage(final String roomId, final JSONObject msg, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("sendMessage");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("sendMessage", "Room");
            result.success(map);
            return;
        }

        roomManager.sendMessage(msg, result);
    }

    /**
     * Mute video track
     *
     * @param localId video track id
     * @param mute    true: mute, false: unmute
     * @param result  result
     */
    public void mute(final String localId, final boolean mute, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("mute");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("mute", "Video track");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().mute(mute);

        Map<String, Object> map = Utils.createSuccessMap("mute");
        result.success(map);
    }

    /**
     * Enable video
     *
     * @param localId video track id
     * @param enable  true: enable, false: disable
     * @param result  result
     */
    public void enableVideo(final String localId, final boolean enable, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("enableVideo");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("enableVideo", "Video track");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().enableVideo(enable);

        Map<String, Object> map = Utils.createSuccessMap("enableVideo");
        result.success(map);
    }

    /**
     * Switch camera
     *
     * @param localId video track id
     * @param result  result
     */
    public void switchCamera(final String localId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("switchCamera");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("switchCamera", "Video track");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("switchCamera");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("switchCamera", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Switch camera
     *
     * @param localId    video track id
     * @param cameraName camera name
     * @param result     result
     */
    public void switchCamera(final String localId, final String cameraName, final Result result) {
        if (!clientWrapper.isConnected()) {
            Map<String, Object> map = Utils.createErrorClientDisconnectedMap("switchCamera");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Map<String, Object> map = Utils.createNotFoundErrorMap("switchCamera", "Video track");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().switchCamera(new StatusListener() {
            public void onSuccess() {
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createSuccessMap("switchCamera");
                    result.success(map);
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                Utils.post(() -> {
                    Map<String, Object> map = Utils.createErrorMap("switchCamera", stringeeError.getCode(), stringeeError.getMessage());
                    result.success(map);
                });
            }
        }, cameraName);
    }
}
