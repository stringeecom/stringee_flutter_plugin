package com.stringee.stringeeflutterplugin;

import android.app.Activity;
import android.content.Context;
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

/** Coordinates native Stringee video rooms and local or remote track operations for one client. */
public class VideoConferenceManager {

    private final ClientWrapper clientWrapper;
    private final Map<String, RoomManager> roomsMap = new HashMap<>();

    private static final String TAG = "StringeeSDK";

    /** Creates a conference manager bound to {@code clientWrapper}. */
    public VideoConferenceManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    /** Returns active room bridges keyed by native room ID. */
    public Map<String, RoomManager> getRoomsMap() {
        return roomsMap;
    }

    /**
     * Connects to a video room.
     *
     * @param roomToken
     *         Video room token.
     * @param result
     *         Flutter method result.
     */
    public void connect(final String roomToken, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "Room connect: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = new RoomManager(clientWrapper, this);
        roomManager.connect(roomToken, result);
    }

    /**
     * Creates a local video track with the provided options.
     *
     * @param context
     *         Android context.
     * @param options
     *         Local track options.
     * @param result
     *         Flutter method result.
     */
    public void createLocalVideoTrack(
            final Context context,
            final StringeeVideoTrack.Options options, final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "createLocalVideoTrack: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        StringeeVideoTrack localVideoTrack = StringeeVideo.createLocalVideoTrack(
                context, options, new StatusListener() {
                    @Override
                    public void onSuccess() {

                    }
                }
        );
        String localId = Utils.createLocalId();
        VideoTrackManager videoTrackManager = new VideoTrackManager(
                clientWrapper, localVideoTrack, localId, false);
        StringeeManager.getInstance().getTracksMap().put(localId, videoTrackManager);

        Log.d(TAG, "createLocalVideoTrack: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        map.put(
                "body", Utils.convertLocalVideoTrackToMap(
                        videoTrackManager,
                        clientWrapper.getClient().getUserId()
                )
        );
        result.success(map);
    }

    /**
     * Creates a local screen-capture video track.
     *
     * @param result
     *         Flutter method result.
     */
    public void createCaptureScreenTrack(final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "createCaptureScreenTrack: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        final int REQUEST_CODE = new Random().nextInt(65536);

        StringeeManager.getInstance().getCaptureManager().getActivityResult(
                (requestCode, resultCode, data) -> {
                    if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                        StringeeManager.getInstance().getCaptureManager().getScreenCapture().createCapture(
                                data, new CallbackListener<StringeeVideoTrack>() {
                                    @Override
                                    public void onSuccess(
                                            StringeeVideoTrack videoTrack
                                    ) {
                                        String localId = Utils.createLocalId();
                                        VideoTrackManager videoTrackManager = new VideoTrackManager(
                                                clientWrapper, videoTrack, localId, false);
                                        StringeeManager.getInstance().getTracksMap().put(
                                                localId, videoTrackManager);
                                        Log.d(TAG, "createCaptureScreenTrack: success");
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("status", true);
                                        map.put("code", 0);
                                        map.put("message", "Success");
                                        map.put(
                                                "body", Utils.convertLocalVideoTrackToMap(
                                                        videoTrackManager,
                                                        clientWrapper.getClient().getUserId()
                                                )
                                        );
                                        result.success(map);
                                    }

                                    @Override
                                    public void onError(StringeeError stringeeError) {
                                        super.onError(stringeeError);
                                        Log.d(
                                                TAG, "createCaptureScreenTrack: false - " +
                                                        stringeeError.getCode() + " - " +
                                                        stringeeError.getMessage()
                                        );
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("status", false);
                                        map.put("code", stringeeError.getCode());
                                        map.put("message", stringeeError.getMessage());
                                        result.success(map);
                                    }
                                }
                        );
                    }
                    return false;
                });
    }

    /**
     * Publishes a local track to a room.
     *
     * @param roomId
     *         Room id.
     * @param localId
     *         Local track id.
     * @param result
     *         Flutter method result.
     */
    public void publish(final String roomId, final String localId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "publish: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "publish: false - -3 - Room is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "publish: false - -3 - Video track is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        roomManager.publish(videoTrackManager, result);
    }

    /**
     * Unpublishes a track from a room.
     *
     * @param roomId
     *         Room id.
     * @param trackId
     *         Published track id.
     * @param result
     *         Flutter method result.
     */
    public void unpublish(final String roomId, final String trackId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "unpublish: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "unpublish: false - -3 - Room is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                trackId);
        if (videoTrackManager == null) {
            Log.d(TAG, "unpublish: false - -3 - Video track is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        roomManager.unpublish(videoTrackManager.getVideoTrack(), result);
    }

    /**
     * Subscribes to a remote track in a room.
     *
     * @param roomId
     *         Room id.
     * @param trackId
     *         Remote track id.
     * @param options
     *         Subscription options.
     * @param result
     *         Flutter method result.
     */
    public void subscribe(
            final String roomId, final String trackId,
            final StringeeVideoTrack.Options options, final Result result
    ) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "subscribe: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "subscribe: false - -3 - Room is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                trackId);
        if (videoTrackManager == null) {
            Log.d(TAG, "subscribe: false - -3 - Video track is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        roomManager.subscribe(videoTrackManager, options, result);
    }

    /**
     * Unsubscribes from a remote track in a room.
     *
     * @param roomId
     *         Room id.
     * @param trackId
     *         Remote track id.
     * @param result
     *         Flutter method result.
     */
    public void unsubscribe(final String roomId, final String trackId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "unsubscribe: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "unsubscribe: false - -3 - Room is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                trackId);
        if (videoTrackManager == null) {
            Log.d(TAG, "unsubscribe: false - -3 - Video track is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        roomManager.unsubscribe(videoTrackManager.getVideoTrack(), result);
    }

    /**
     * Leaves a video room.
     *
     * @param roomId
     *         Room id.
     * @param allClient
     *         True to leave for all clients.
     * @param result
     *         Flutter method result.
     */
    public void leave(final String roomId, final boolean allClient, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "leave: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "leave: false - -3 - Room is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        roomManager.leave(allClient, result);
    }

    /**
     * Sends a data message in a video room.
     *
     * @param roomId
     *         Room id.
     * @param msg
     *         Message payload.
     * @param result
     *         Flutter method result.
     */
    public void sendMessage(final String roomId, final JSONObject msg, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "sendMessage: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Log.d(TAG, "sendMessage: false - -3 - Room is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Room is not found");
            result.success(map);
            return;
        }

        roomManager.sendMessage(msg, result);
    }

    /**
     * Mutes or unmutes a local track audio stream.
     *
     * @param localId
     *         Local track id.
     * @param mute
     *         True to mute; false to unmute.
     * @param result
     *         Flutter method result.
     */
    public void mute(final String localId, final boolean mute, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "mute: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "mute: false - -3 - Video track is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().mute(mute);

        Log.d(TAG, "mute: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Enables or disables a local track video stream.
     *
     * @param localId
     *         Local track id.
     * @param enable
     *         True to enable video; false to disable it.
     * @param result
     *         Flutter method result.
     */
    public void enableVideo(final String localId, final boolean enable, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "enableVideo: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "enableVideo: false - -3 - Video track is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().enableVideo(enable);

        Log.d(TAG, "enableVideo: success");
        Map<String, Object> map = new HashMap<>();
        map.put("status", true);
        map.put("code", 0);
        map.put("message", "Success");
        result.success(map);
    }

    /**
     * Switches the camera used by a local video track.
     *
     * @param localId
     *         Local track id.
     * @param result
     *         Flutter method result.
     */
    public void switchCamera(final String localId, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "switchCamera: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "switchCamera: false - -3 - Video track is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                Utils.post(() -> {
                    Log.d(TAG, "switchCamera: success");
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
                    Log.d(
                            TAG, "switchCamera: false - " + stringeeError.getCode() + " - " +
                                    stringeeError.getMessage()
                    );
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", false);
                    map.put("code", stringeeError.getCode());
                    map.put("message", stringeeError.getMessage());
                    result.success(map);
                });
            }
        });
    }

    /**
     * Switches a local video track to a specific camera.
     *
     * @param localId
     *         Local track id.
     * @param cameraName
     *         Camera name or identifier.
     * @param result
     *         Flutter method result.
     */
    public void switchCamera(final String localId, final String cameraName, final Result result) {
        if (!clientWrapper.isConnected()) {
            Log.d(TAG, "switchCamera: false - -1 - StringeeClient is disconnected");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -1);
            map.put("message", "StringeeClient is disconnected");
            result.success(map);
            return;
        }

        VideoTrackManager videoTrackManager = StringeeManager.getInstance().getTracksMap().get(
                localId);
        if (videoTrackManager == null) {
            Log.d(TAG, "switchCamera: false - -3 - Video track is not found");
            Map<String, Object> map = new HashMap<>();
            map.put("status", false);
            map.put("code", -3);
            map.put("message", "Video track is not found");
            result.success(map);
            return;
        }

        videoTrackManager.getVideoTrack().switchCamera(
                new StatusListener() {
                    public void onSuccess() {
                        Utils.post(() -> {
                            Log.d(TAG, "switchCamera: success");
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
                            Log.d(
                                    TAG,
                                    "switchCamera: false - " + stringeeError.getCode() + " - " +
                                            stringeeError.getMessage()
                            );
                            Map<String, Object> map = new HashMap<>();
                            map.put("status", false);
                            map.put("code", stringeeError.getCode());
                            map.put("message", stringeeError.getMessage());
                            result.success(map);
                        });
                    }
                }, cameraName
        );
    }
}
