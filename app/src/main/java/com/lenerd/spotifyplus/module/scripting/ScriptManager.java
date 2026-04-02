package com.lenerd.spotifyplus.module.scripting;

import android.util.Log;
import com.lenerd.spotifyplus.manager.bridge.BridgeMessageBus;
import com.lenerd.spotifyplus.manager.bridge.BridgeMessageListener;
import com.lenerd.spotifyplus.module.SpotifyHook;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ScriptManager implements BridgeMessageListener {
    private static final Map<String, SpotifyHook> handlers = new HashMap<>();

    public ScriptManager() {
        BridgeMessageBus.register(this);
    }

    public static void registerHandler(String type, SpotifyHook hook) {
        handlers.put(type, hook);
    }

    @Override
    public void onMessage(String id, String type, String name, JSONObject payload) {
        String tag = name.split("\\.")[0];

        SpotifyHook hook = handlers.get(tag);
        if(hook == null) {
            Log.w("SpotifyPlus", "Unknown command: " + name);
            return;
        }

        try {
            hook.handle(id, name.split("\\.")[1], payload);
        } catch(Exception e) {
            Log.e("SpotifyPlus", e.getMessage(), e);
        }
    }
}
