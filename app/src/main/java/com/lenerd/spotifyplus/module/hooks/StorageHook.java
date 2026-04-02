package com.lenerd.spotifyplus.module.hooks;

import android.content.Context;
import android.content.SharedPreferences;
import com.lenerd.spotifyplus.manager.bridge.BridgeClient;
import com.lenerd.spotifyplus.module.SpotifyCallback;
import com.lenerd.spotifyplus.module.SpotifyHook;
import com.lenerd.spotifyplus.module.scripting.ScriptManager;
import org.json.JSONObject;

public class StorageHook extends SpotifyHook {
    @Override
    protected void hookSetup() throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        ScriptManager.registerHandler("storage", this);
    }

    @Override
    protected void beforeHook(SpotifyCallback callback) {
    }

    @Override
    protected void afterHook(SpotifyCallback callback) {
    }

    @Override
    public void handle(String id, String command, JSONObject json) {
        if (command.equals("set")) {
            try {
                if(currentActivity == null) return;

                String scriptId = json.getString("scriptId");
                String key = json.getString("key");
                Object value = json.get("value");

                SharedPreferences prefs = currentActivity.getSharedPreferences(scriptId, Context.MODE_PRIVATE);

                switch (value) {
                    case String s -> prefs.edit().putString(key, s).apply();
                    case Boolean b -> prefs.edit().putBoolean(key, b).apply();
                    case Integer i -> prefs.edit().putInt(key, i).apply();
                    case Long l -> prefs.edit().putLong(key, l).apply();
                    case Float v -> prefs.edit().putFloat(key, v).apply();
                    default -> prefs.edit().putString(key, value.toString()).apply();
                }
            } catch(Exception e) {
                logError("Failed to parse JSON");
            }
        } else if(command.equals("get")) {
            try {
                String scriptId = json.getString("scriptId");
                String key = json.getString("key");

                SharedPreferences prefs = currentActivity.getSharedPreferences(scriptId, Context.MODE_PRIVATE);
                Object value = prefs.getAll().get(key);

                if(value == null) {
                    BridgeClient.send(id, "response", "storage.get", new JSONObject());
                } else {
                    BridgeClient.send(id, "response", "storage.get", new JSONObject().put("value", value));
                }
            } catch(Exception e) {
                logError(e);
                BridgeClient.send(id, "response", "storage.get", new JSONObject());
            }
        }
    }
}