package com.stringee.stringeeflutterplugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.util.Log;

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
    private ClientWrapper clientWrapper;
    private StringeeManager stringeeManager;
    private Handler handler;
    private StringeeVideo stringeeVideo;
    private Map<String, RoomManager> roomsMap = new HashMap<>();

    private static final String TAG = "StringeeSDK";

    public VideoConferenceManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
        stringeeManager = StringeeManager.getInstance();
        handler = stringeeManager.getHandler();
        stringeeVideo = new StringeeVideo();
    }

    public StringeeVideo getStringeeVideo() {
        return stringeeVideo;
    }

    public Map<String, RoomManager> getRoomsMap() {
        return roomsMap;
    }

    /**
     * Connect room
     *
     * @param roomToken
     * @param result
     */
    public void connect(final String roomToken, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "Room connect: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = new RoomManager(clientWrapper, this);
        roomManager.connect(stringeeVideo, roomToken, result);
    }

    /**
     * Create local video track
     *
     * @param options
     * @param result
     */
    public void createLocalVideoTrack(final StringeeVideoTrack.Options options, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "createLocalVideoTrack: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        StringeeVideoTrack localVideoTrack = stringeeVideo.createLocalVideoTrack(stringeeManager.getContext(), options);
        String localId = Utils.createLocalId();
        VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, localVideoTrack, localId, false);
        stringeeManager.getTracksMap().put(localId, videoTrackManager);

        Log.d(TAG, "createLocalVideoTrack: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        map.put("body", Utils.convertLocalVideoTrackToMap(videoTrackManager, clientWrapper.getClient().getUserId()));
        result.success(map);
    }

    /**
     * Create capture screen track
     *
     * @param result
     */
    public void createCaptureScreenTrack(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "createCaptureScreenTrack: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            final int REQUEST_CODE = new Random().nextInt(65536);

            stringeeManager.getCaptureManager().getActivityResult(new ActivityResultListener() {
                @Override
                public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        stringeeManager.getCaptureManager().getScreenCapture().createCapture(data);
                    }
                    return false;
                }
            });

            stringeeManager.getCaptureManager().getScreenCapture().startCapture(REQUEST_CODE, new CallbackListener<StringeeVideoTrack>() {
                @Override
                public void onSuccess(StringeeVideoTrack videoTrack) {
                    String localId = Utils.createLocalId();
                    VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, videoTrack, localId, false);
                    stringeeManager.getTracksMap().put(localId, videoTrackManager);
                    Log.d(TAG, "createCaptureScreenTrack: success");
                    Map map = new HashMap();
                    map.put("status", true);
                    map.put("code", 0);
                    map.put("message", "Success");
                    map.put("body", Utils.convertLocalVideoTrackToMap(videoTrackManager, clientWrapper.getClient().getUserId()));
                    result.success(map);
                }

                @Override
                public void onError(StringeeError stringeeError) {
                    super.onError(stringeeError);
                    Log.d(TAG, "createCaptureScreenTrack: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                    Map map = new HashMap();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                }
            });
        } else {
            Log.d(TAG, "createCaptureScreenTrack: false - -5 - This feature requires android api level >= 21");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -5);
            map.put("message", "This feature requires android api level >= 21");
            result.success(map);
        }
    }

    /**
     * Publish video track
     *
     * @param roomId
     * @param localId
     * @param result
     */
    public void publish(final String roomId, final String localId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "publish: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "publish: false - -3 - Room is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "publish: false - -3 - Video track is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        roomManager.publish(videoTrackManager, result);
    }

    /**
     * Unpublish video track
     *
     * @param roomId
     * @param trackId
     * @param result
     */
    public void unpublish(final String roomId, final String trackId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "unpublish: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "unpublish: false - -3 - Room is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(trackId);
        if (videoTrackManager == null) {
            Log.d(TAG, "unpublish: false - -3 - Video track is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        roomManager.unpublish(videoTrackManager.getVideoTrack(), result);
    }

    /**
     * Subscribe video track
     *
     * @param roomId
     * @param trackId
     * @param options
     * @param result
     */
    public void subscribe(final String roomId, final String trackId, final StringeeVideoTrack.Options options, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "subscribe: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "subscribe: false - -3 - Room is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(trackId);
        if (videoTrackManager == null) {
            Log.d(TAG, "subscribe: false - -3 - Video track is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        roomManager.subscribe(videoTrackManager, options, result);
    }

    /**
     * Unsubscribe video track
     *
     * @param roomId
     * @param trackId
     * @param result
     */
    public void unsubscribe(final String roomId, final String trackId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "unsubscribe: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "unsubscribe: false - -3 - Room is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(trackId);
        if (videoTrackManager == null) {
            Log.d(TAG, "unsubscribe: false - -3 - Video track is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        roomManager.unsubscribe(videoTrackManager.getVideoTrack(), result);
    }

    /**
     * Leave room
     *
     * @param roomId
     * @param allClient
     * @param result
     */
    public void leave(final String roomId, final boolean allClient, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "leave: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "leave: false - -3 - Room is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        roomManager.leave(allClient, result);
    }

    /**
     * Send message in room
     *
     * @param roomId
     * @param msg
     * @param result
     */
    public void sendMessage(final String roomId, final JSONObject msg, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "sendMessage: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "sendMessage: false - -3 - Room is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        roomManager.sendMessage(msg, result);
    }

    /**
     * Mute video track
     *
     * @param localId
     * @param mute
     * @param result
     */
    public void mute(final String localId, final boolean mute, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "mute: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "mute: false - -3 - Video track is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().mute(mute);

        Log.d(TAG, "mute: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Enable video
     *
     * @param localId
     * @param enable
     * @param result
     */
    public void enableVideo(final String localId, final boolean enable, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "enableVideo: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "enableVideo: false - -3 - Video track is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().enableVideo(enable);

        Log.d(TAG, "enableVideo: success");
        Map map = new HashMap();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Switch camera
     *
     * @param localId
     * @param result
     */
    public void switchCamera(final String localId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "switchCamera: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "switchCamera: false - -3 - Video track is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "switchCamera: success");
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
                        Log.d(TAG, "switchCamera: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
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

    /**
     * Switch camera
     *
     * @param localId
     * @param cameraId
     * @param result
     */
    public void switchCamera(final String localId, final int cameraId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "switchCamera: false - -1 - StringeeClient is disconnected");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "switchCamera: false - -3 - Video track is not found");
            Map map = new HashMap();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().switchCamera(new StatusListener() {
            public void onSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "switchCamera: success");
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
                        Log.d(TAG, "switchCamera: false - " + stringeeError.getCode() + " - " + stringeeError.getMessage());
                        Map map = new HashMap();
                        map.put("status", false);
                        map.put("code", stringeeError.getCode());
                        map.put("message", stringeeError.getMessage());
                        result.success(map);
                    }
                });
            }
        }, cameraId);
    }
}
