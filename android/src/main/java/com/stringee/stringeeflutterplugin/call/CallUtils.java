package com.stringee.stringeeflutterplugin.call;

import com.stringee.stringeeflutterplugin.common.FlutterResult;
import com.stringee.stringeeflutterplugin.common.StringeeManager;
import com.stringee.stringeeflutterplugin.common.Utils;

import io.flutter.plugin.common.MethodChannel;

public class CallUtils {
    public static boolean isCallAvailable(String methodName, String callId,
                                          MethodChannel.Result result) {
        if (Utils.isEmpty(callId)) {
            result.success(FlutterResult.error(methodName, -2, "callId is invalid").getMap());
            return false;
        }

        StringeeCallWrapper call = StringeeManager.getInstance().getCallsMap().get(callId);
        if (call == null) {
            result.success(FlutterResult.error(methodName, -3, "Call is not found").getMap());
            return false;
        }

        return true;
    }
}
