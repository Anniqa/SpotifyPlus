package com.lenerd.spotifyplus.module.hooks;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.lenerd.spotifyplus.manager.bridge.BridgeClient;
import com.lenerd.spotifyplus.module.SpotifyCallback;
import com.lenerd.spotifyplus.module.SpotifyHook;
import com.lenerd.spotifyplus.module.Utils;
import com.lenerd.spotifyplus.module.entities.SpotifyTrack;
import com.lenerd.spotifyplus.module.scripting.ScriptManager;
import org.json.JSONObject;

public class DebugHook extends SpotifyHook {
    @Override
    protected void hookSetup() throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        ScriptManager.registerHandler("ui", this);
        ScriptManager.registerHandler("track", this);
    }

    @Override
    protected void beforeHook(SpotifyCallback callback) { }

    @Override
    protected void afterHook(SpotifyCallback callback) { }

    @Override
    public void handle(String id, String command, JSONObject json) {
        try {
            if (command.equals("toast")) {
                String text = json.getString("text");
                String length = json.optString("length", "short");

                if (currentActivity == null) return;

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Toast.makeText(currentActivity, text, length.equals("long") ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            logError(e);
        }
    }
}
