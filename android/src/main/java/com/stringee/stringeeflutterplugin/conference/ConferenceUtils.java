package com.stringee.stringeeflutterplugin.conference;

import androidx.annotation.NonNull;

import com.stringee.video.RemoteParticipant;
import com.stringee.video.StringeeRoom;
import com.stringee.video.StringeeVideoTrack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConferenceUtils {

    public static String createLocalId() {
        return "android-" + UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    public static Map<String, Object> convertRoomToMap(@NonNull StringeeRoom room) {
        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("id", room.getId());
        roomMap.put("recorded", room.isRecorded());
        return roomMap;
    }

    public static Map<String, Object> convertRoomUserToMap(@NonNull RemoteParticipant participant) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", participant.getId());
        return userMap;
    }

    public static Map<String, Object> convertLocalVideoTrackToMap(
            @NonNull VideoTrackManager trackManager, String clientId) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        Map<String, Object> trackMap = new HashMap<>();
        trackMap.put("id", videoTrack.getId() != null ? videoTrack.getId() : "");
        trackMap.put("localId", trackManager.getLocalId());
        trackMap.put("audio", videoTrack.audioEnabled());
        trackMap.put("video", videoTrack.videoEnabled());
        trackMap.put("screen", videoTrack.isScreenCapture());
        trackMap.put("isLocal", videoTrack.isLocal());
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", clientId);
        trackMap.put("publisher", userMap);
        return trackMap;
    }

    public static Map<String, Object> convertVideoTrackToMap(
            @NonNull VideoTrackManager trackManager) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        Map<String, Object> trackMap = new HashMap<>();
        trackMap.put("id", videoTrack.getId() != null ? videoTrack.getId() : "");
        trackMap.put("localId", trackManager.getLocalId());
        trackMap.put("audio", videoTrack.audioEnabled());
        trackMap.put("video", videoTrack.videoEnabled());
        trackMap.put("screen", videoTrack.isScreenCapture());
        trackMap.put("isLocal", videoTrack.isLocal());
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", videoTrack.getUserId());
        trackMap.put("publisher", userMap);
        return trackMap;
    }

    public static Map<String, Object> convertVideoTrackInfoToMap(
            @NonNull VideoTrackManager trackManager) {
        StringeeVideoTrack videoTrack = trackManager.getVideoTrack();
        Map<String, Object> trackMap = new HashMap<>();
        trackMap.put("id", videoTrack.getId() != null ? videoTrack.getId() : "");
        trackMap.put("localId", trackManager.getLocalId());
        trackMap.put("audio", videoTrack.audioEnabled());
        trackMap.put("video", videoTrack.videoEnabled());
        trackMap.put("screen", videoTrack.isScreenCapture());
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", videoTrack.getUserId());
        trackMap.put("publisher", userMap);
        return trackMap;
    }
}
