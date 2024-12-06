package com.stringee.stringeeflutterplugin;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Map;

import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class StringeeVideoViewFactory extends PlatformViewFactory {
    public StringeeVideoViewFactory() {
        super(StandardMessageCodec.INSTANCE);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public PlatformView create(Context context, int viewId, Object args) {
        return new StringeeVideoView(context, (Map<String, Object>) args);
    }
}