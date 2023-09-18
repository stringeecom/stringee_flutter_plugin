package com.stringee.stringeeflutterplugin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.messaging.listeners.CallbackListener;
import com.stringee.video.StringeeVideo;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class VideoConferenceManager {
    private ClientWrapper clientWrapper;
    private StringeeManager stringeeManager;
    private Map<String, RoomManager> roomsMap = new HashMap<>();

    public VideoConferenceManager(ClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
        this.stringeeManager = StringeeManager.getInstance();
    }

    public Map<String, RoomManager> getRoomsMap() {
        return roomsMap;
    }

    /**
     * Connect room
     */
    public void connect(final String roomToken, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "Room connect", result)) {
            return;
        }

        RoomManager roomManager = new RoomManager(clientWrapper, this);
        roomManager.connect(roomToken, result);
    }

    /**
     * Release room
     */
    public void release(final String roomId, final Result result) {
        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Utils.sendErrorResponse("Room release", -3, "Room is not found", result);
            return;
        }
        roomManager.release(result);
    }

    /**
     * Create local video track
     */
    public void createLocalVideoTrack(final StringeeVideoTrack.Options options, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "createLocalVideoTrack", result)) {
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

        Utils.sendSuccessResponse("createLocalVideoTrack", Utils.convertLocalVideoTrackToMap(videoTrackManager, clientWrapper.getClient().getUserId()), result);
    }

    /**
     * Create capture screen track
     */
    public void createCaptureScreenTrack(final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "createCaptureScreenTrack", result)) {
            return;
        }

        final int REQUEST_CODE = new Random().nextInt(65536);

        stringeeManager.getCaptureManager().getActivityResult(new ActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                            stringeeManager.getCaptureManager().getScreenCapture().createCapture(data);
                        }
                    }
                });
                return false;
            }
        });

        stringeeManager.getCaptureManager().getScreenCapture().startCapture(REQUEST_CODE, new CallbackListener<StringeeVideoTrack>() {
            @Override
            public void onSuccess(StringeeVideoTrack videoTrack) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        String localId = Utils.createLocalId();
                        VideoTrackManager videoTrackManager = new VideoTrackManager(clientWrapper, videoTrack, localId, false);
                        stringeeManager.getTracksMap().put(localId, videoTrackManager);
                        Utils.sendSuccessResponse("createCaptureScreenTrack", Utils.convertLocalVideoTrackToMap(videoTrackManager, clientWrapper.getClient().getUserId()), result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("createCaptureScreenTrack", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Publish video track
     */
    public void publish(final String roomId, final String localId, StringeeVideoTrack.Options options, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "publish", result)) {
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Utils.sendErrorResponse("publish", -3, "Room is not found", result);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("publish", -3, "Video track is not found", result);
            return;
        }

        if (options == null) {
            options = new StringeeVideoTrack.Options();
            options.audio(videoTrackManager.getVideoTrack().audioEnabled());
            options.video(videoTrackManager.getVideoTrack().videoEnabled());
            options.screen(videoTrackManager.getVideoTrack().isScreenCapture());
        }

        roomManager.publish(videoTrackManager, options, result);
    }

    /**
     * Unpublish video track
     */
    public void unpublish(final String roomId, final String trackId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "unpublish", result)) {
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Utils.sendErrorResponse("unpublish", -3, "Room is not found", result);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(trackId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("unpublish", -3, "Video track is not found", result);
            return;
        }

        roomManager.unpublish(videoTrackManager.getVideoTrack(), result);
    }

    /**
     * Subscribe video track
     */
    public void subscribe(final String roomId, final String trackId, final StringeeVideoTrack.Options options, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "subscribe", result)) {
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Utils.sendErrorResponse("subscribe", -3, "Room is not found", result);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(trackId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("subscribe", -3, "Video track is not found", result);
            return;
        }

        roomManager.subscribe(videoTrackManager, options, result);
    }

    /**
     * Unsubscribe video track
     */
    public void unsubscribe(final String roomId, final String trackId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "unsubscribe", result)) {
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Utils.sendErrorResponse("unsubscribe", -3, "Room is not found", result);
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(trackId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("unsubscribe", -3, "Video track is not found", result);
            return;
        }

        roomManager.unsubscribe(videoTrackManager.getVideoTrack(), result);
    }

    /**
     * Leave room
     */
    public void leave(final String roomId, final boolean allClient, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "leave", result)) {
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Utils.sendErrorResponse("leave", -3, "Room is not found", result);
            return;
        }

        roomManager.leave(allClient, result);
    }

    /**
     * Send message in room
     */
    public void sendMessage(final String roomId, final JSONObject msg, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "sendMessage", result)) {
            return;
        }

        RoomManager roomManager = roomsMap.get(roomId);
        if (roomManager == null) {
            Utils.sendErrorResponse("sendMessage", -3, "Room is not found", result);
            return;
        }

        roomManager.sendMessage(msg, result);
    }

    /**
     * Mute video track
     */
    public void mute(final String localId, final boolean mute, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "mute", result)) {
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("mute", -3, "Video track is not found", result);
            return;
        }

        videoTrackManager.getVideoTrack().mute(mute);
        Utils.sendSuccessResponse("createCaptureScreenTrack", null, result);
    }

    /**
     * Enable video
     */
    public void enableVideo(final String localId, final boolean enable, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "enableVideo", result)) {
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("enableVideo", -3, "Video track is not found", result);
            return;
        }

        videoTrackManager.getVideoTrack().enableVideo(enable);
        Utils.sendSuccessResponse("enableVideo", null, result);
    }

    /**
     * Switch camera
     */
    public void switchCamera(final String localId, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "switchCamera", result)) {
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("switchCamera", -3, "Video track is not found", result);
            return;
        }

        videoTrackManager.getVideoTrack().switchCamera(new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("switchCamera", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("switchCamera", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Switch camera
     */
    public void switchCamera(final String localId, final String cameraName, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "switchCamera", result)) {
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("switchCamera", -3, "Video track is not found", result);
            return;
        }

        videoTrackManager.getVideoTrack().switchCamera(new StatusListener() {
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("switchCamera", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("switchCamera", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        }, cameraName);
    }

    /**
     * Release track
     */
    public void releaseTrack(final String localId, final Result result) {
        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("releaseTrack", -3, "Video track is not found", result);
            return;
        }

        videoTrackManager.getVideoTrack().release();
        Utils.sendSuccessResponse("releaseTrack", null, result);
    }

    /**
     * Snap shot local track
     */
    public void snapShot(final String localId, final Result result) {
        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("snapShot", -3, "Video track is not found", result);
            return;
        }

        videoTrackManager.getVideoTrack().snapshotLocal(new CallbackListener<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] bytearray = stream.toByteArray();
                        bitmap.recycle();
                        Utils.sendSuccessResponse("snapShot", "image", bytearray, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("snapShot", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Send audio enable notification
     */
    public void sendAudioEnableNotification(final String localId, final boolean enable, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "sendAudioEnableNotification", result)) {
            return;
        }
        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("sendAudioEnableNotification", -3, "Video track is not found", result);
            return;
        }

        videoTrackManager.getVideoTrack().sendAudioEnableNotification(enable, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("sendAudioEnableNotification", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("sendAudioEnableNotification", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Send audio enable notification
     */
    public void sendVideoEnableNotification(final String localId, final boolean enable, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "sendVideoEnableNotification", result)) {
            return;
        }
        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(localId);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("sendVideoEnableNotification", -3, "Video track is not found", result);
            return;
        }

        videoTrackManager.getVideoTrack().sendVideoEnableNotification(enable, new StatusListener() {
            @Override
            public void onSuccess() {
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendSuccessResponse("sendVideoEnableNotification", null, result);
                    }
                });
            }

            @Override
            public void onError(StringeeError stringeeError) {
                super.onError(stringeeError);
                stringeeManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.sendErrorResponse("sendVideoEnableNotification", stringeeError.getCode(), stringeeError.getMessage(), result);
                    }
                });
            }
        });
    }

    /**
     * Set mirror
     */
    public void setMirror(final String id, final boolean isMirror, final Result result) {
        if (!Utils.isClientConnected(clientWrapper, "setMirror", result)) {
            return;
        }

        VideoTrackManager videoTrackManager = stringeeManager.getTracksMap().get(id);
        if (videoTrackManager == null) {
            Utils.sendErrorResponse("setMirror", -3, "Video track is not found", result);
            return;
        }

        videoTrackManager.getVideoTrack().getView2(stringeeManager.getContext()).setMirror(isMirror);
        //save track view option
        videoTrackManager.getViewOptions().put("isMirror", isMirror);

        Utils.sendSuccessResponse("setMirror", null, result);
    }
}
