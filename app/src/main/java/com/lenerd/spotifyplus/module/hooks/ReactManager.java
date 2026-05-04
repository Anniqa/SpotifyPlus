package com.lenerd.spotifyplus.module.hooks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import com.lenerd.spotifyplus.manager.bridge.BridgeClient;
import com.lenerd.spotifyplus.module.SpotifyCallback;
import com.lenerd.spotifyplus.module.SpotifyHook;
import com.lenerd.spotifyplus.module.scripting.ScriptManager;
import com.lenerd.spotifyplus.module.scripting.ScriptViewHost;
import com.lenerd.spotifyplus.module.scripting.SpotifyNativeBridge;
import com.lenerd.spotifyplus.module.scripting.UiSurfaceHost;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReactManager extends SpotifyHook {
    private static final Map<String, SurfaceEntry> surfaces = new HashMap<>();

    private static class SurfaceEntry {
        final ViewGroup root;
        final ScriptViewHost host;

        SurfaceEntry(ViewGroup root, ScriptViewHost host) {
            this.root = root;
            this.host = host;
        }
    }

    @Override
    protected void hookSetup() throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        SpotifyNativeBridge.registerHandler("react", this);
    }

    @Override
    protected void beforeHook(SpotifyCallback callback) {
    }

    @Override
    protected void afterHook(SpotifyCallback callback) {
    }

    @Override
    public Object handle(String command, Object[] args) {
        if (command.equals("commit")) {
            try {
                String surfaceId = (String) args[0];
                applyCommit(surfaceId, (JSONArray) args[1]);
            } catch (Exception e) {
                logError(e);
            }
        }

        return null;
    }

    public static void registerSurface(String surfaceId, ViewGroup root) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                SurfaceEntry existing = surfaces.get(surfaceId);
                if (existing != null) {
                    if (existing.root == root) return;
                    existing.host.dispose();
                    surfaces.remove(surfaceId);
                }

                ScriptViewHost host = new ScriptViewHost(surfaceId, root);
                surfaces.put(surfaceId, new SurfaceEntry(root, host));

                JSONObject json = new JSONObject();
                json.put("id", surfaceId);
                json.put("type", surfaceId);

                SpotifyNativeBridge.attachSurfaceHost(surfaceId, root);
                SpotifyNativeBridge.sendEvent("react.surfaceEvent", json.toString());
//                ScriptManager.send("", "event", "react.surfaceEvent", json);
            } catch (Exception e) {
                logError(e);
            }
        });
    }

    public static void registerSurfaceSilent(String surfaceId, ViewGroup root) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                SurfaceEntry existing = surfaces.get(surfaceId);
                if (existing != null) {
                    if (existing.root == root) return;
                    existing.host.dispose();
                    surfaces.remove(surfaceId);
                }
                ScriptViewHost host = new ScriptViewHost(surfaceId, root);
                surfaces.put(surfaceId, new SurfaceEntry(root, host));
            } catch (Exception e) {
                logError(e);
            }
        });
    }

    public static void unregisterSurface(String surfaceId) {
        new Handler(Looper.getMainLooper()).post(() -> {
            SurfaceEntry existing = surfaces.remove(surfaceId);
            if (existing != null) existing.host.dispose();
        });
    }

    private void applyCommit(String surfaceId, JSONArray ops) {
        SurfaceEntry entry = surfaces.get(surfaceId);
        if (entry == null) {
            Log.w("SpotifyPlus", "No surface registered for " + surfaceId);
            return;
        }

        entry.host.applyOps(ops);
    }
}
