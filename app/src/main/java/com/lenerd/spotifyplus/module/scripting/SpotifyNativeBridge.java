package com.lenerd.spotifyplus.module.scripting;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;

import com.lenerd.spotifyplus.module.SpotifyApi;
import com.lenerd.spotifyplus.module.SpotifyHook;
import com.lenerd.spotifyplus.module.Utils;
import com.lenerd.spotifyplus.module.entities.SpotifyTrack;
import com.lenerd.spotifyplus.module.scripting.entities.PlatformData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpotifyNativeBridge {
    private static final String TAG = "SpotifyPlus:NativeBridge";
    private static final Map<String, SpotifyHook> handlers = new HashMap<>();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final Map<String, ScriptViewHost> surfaceHosts = new ConcurrentHashMap<>();
    private static final Set<String> registeredSurfaces = ConcurrentHashMap.newKeySet();
    private final ClassLoader classLoader;

    public static class StorageReadResult {
        public boolean found;
        public String type;
        public String value;
        public String data;

        public StorageReadResult() {
            this(false, "", null, null);
        }

        public StorageReadResult(boolean found, String type, String value, String data) {
            this.found = found;
            this.type = type;
            this.value = value;
            this.data = data;
        }
    }

    public SpotifyNativeBridge(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private static Object invokeHandler(String type, String id, Object... args) {
        SpotifyHook hook = handlers.get(type);
        if (hook == null) {
            Log.w(TAG, "Handler not registered for type: " + type);
            return null;
        }

        try {
            return hook.handle(id, args);
        } catch (Throwable e) {
            Log.e(TAG, "Failed to invoke handler " + type + ":" + id, e);
            return null;
        }
    }

    public PlatformData getPlatformData() {
        return Utils.platformData;
    }

    public String getAccessToken() {
        return Utils.token;
    }

    public SpotifyTrack getCurrentTrack() {
        return Utils.getTrack(classLoader);
    }

    public SpotifyTrack getTrack(String uri) {
        return null;
    }

    public double getPlaybackPosition() {
        try {
            return Utils.getCurrentPlaybackPosition();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get playback position", e);
            return 0.0;
        }
    }

    public void seek(long position) {
        try {
            SpotifyHook hook = handlers.get("player");
            if (hook == null) {
                Log.w(TAG, "Player hook not registered");
                return;
            }

            hook.handle("seek", new Object[]{position});
        } catch (Exception e) {
            Log.e(TAG, "Failed to seek", e);
        }
    }

    public void play() {
        try {
            SpotifyHook hook = handlers.get("player");
            if (hook == null) {
                Log.w(TAG, "Player hook not registered");
                return;
            }

            hook.handle("play", new Object[]{});
        } catch (Exception e) {
            Log.e(TAG, "Failed to play", e);
        }
    }

    public void pause() {
        try {
            SpotifyHook hook = handlers.get("player");
            if (hook == null) {
                Log.w(TAG, "Player hook not registered");
                return;
            }

            hook.handle("pause", new Object[]{});
        } catch (Exception e) {
            Log.e(TAG, "Failed to pause", e);
        }
    }

    public void togglePlay() {
        try {
            SpotifyHook hook = handlers.get("player");
            if (hook == null) {
                Log.w(TAG, "Player hook not registered");
                return;
            }

            hook.handle("togglePlay", new Object[]{});
        } catch (Exception e) {
            Log.e(TAG, "Failed to togglePlay", e);
        }
    }

    public void skipNext() {
        try {
            SpotifyHook hook = handlers.get("player");
            if (hook == null) {
                Log.w(TAG, "Player hook not registered");
                return;
            }

            hook.handle("skipNext", new Object[]{});
        } catch (Exception e) {
            Log.e(TAG, "Failed to skipNext", e);
        }
    }

    public void skipPrevious() {
        try {
            SpotifyHook hook = handlers.get("player");
            if (hook == null) {
                Log.w(TAG, "Player hook not registered");
                return;
            }

            hook.handle("skipPrevious", new Object[]{});
        } catch (Exception e) {
            Log.e(TAG, "Failed to skipPrevious", e);
        }
    }

    public void toast(String text, boolean longLength) {
        try {
            invokeHandler("ui", "toast", text, longLength);
        } catch (Exception e) {
            Log.e(TAG, "Failed to show toast", e);
        }
    }

    public void openUri(String uri) {
        try {
            invokeHandler("system", "openUri", uri);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open uri", e);
        }
    }

    public void storageSet(String scriptId, String key, String value) {
        try {
            invokeHandler("storage", "set", scriptId, key, value);
        } catch (Exception e) {
            Log.e(TAG, "Failed to storageSet", e);
        }
    }

    public String storageGet(String scriptId, String key) {
        try {
            Object result = invokeHandler("storage", "get", scriptId, key);
            return result instanceof String ? (String) result : null;
        } catch (Exception e) {
            Log.e(TAG, "Failed to storageGet", e);
            return null;
        }
    }

    public void storageRemove(String scriptId, String key) {
        try {
            invokeHandler("storage", "remove", scriptId, key);
        } catch (Exception e) {
            Log.e(TAG, "Failed to storageRemove", e);
        }
    }

    public void storageWriteText(String scriptId, String path, String value) {
        try {
            invokeHandler("storage", "writeText", scriptId, path, value);
        } catch (Exception e) {
            Log.e(TAG, "Failed to storageWriteText", e);
        }
    }

    public void storageWriteJson(String scriptId, String path, String value) {
        try {
            invokeHandler("storage", "writeJson", scriptId, path, value);
        } catch (Exception e) {
            Log.e(TAG, "Failed to storageWriteJson", e);
        }
    }

    public void storageWriteBinary(String scriptId, String path, String data) {
        try {
            invokeHandler("storage", "writeBinary", scriptId, path, data);
        } catch (Exception e) {
            Log.e(TAG, "Failed to storageWriteBinary", e);
        }
    }

    public StorageReadResult storageRead(String scriptId, String path) {
        try {
            Object result = invokeHandler("storage", "read", scriptId, path);
            return result instanceof StorageReadResult ? (StorageReadResult) result : new StorageReadResult();
        } catch (Exception e) {
            Log.e(TAG, "Failed to storageRead", e);
            return new StorageReadResult();
        }
    }

    public void registerContextMenu(String id, String scriptId, String title) {
        try {
            invokeHandler("menu", "register", id, scriptId, title);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register context menu", e);
        }
    }

    public void registerSideDrawer(String id, String scriptId, String title) {
        try {
            invokeHandler("side", "register", id, scriptId, title);
        } catch (Exception e) {
            Log.e(TAG, "Failed to register side drawer", e);
        }
    }

    public void registerSurface(String surfaceId) {
        registeredSurfaces.add(surfaceId);
    }

    public void unregisterSurface(String surfaceId) {
        registeredSurfaces.remove(surfaceId);
    }

    public void commitSurface(String surfaceId, String opsJson) {
        if (!registeredSurfaces.contains(surfaceId)) return;

        mainHandler.post(() -> {
            ScriptViewHost host = surfaceHosts.get(surfaceId);
            if (host == null) {
                Log.w(TAG, "commitSurface ignored because host was missing for " + surfaceId);
                return;
            }

            try {
                host.applyOps(new JSONArray(opsJson));
            } catch (Exception e) {
                Log.e(TAG, "Failed to apply commit for surface " + surfaceId, e);
            }
        });
    }

    public static void attachSurfaceHost(String surfaceId, ViewGroup root) {
        Log.d(TAG, "Attaching surface!!!!!!  " + surfaceId);

        mainHandler.post(() -> {
            var existing = surfaceHosts.get(surfaceId);
            if (existing == null) {
                surfaceHosts.put(surfaceId, new ScriptViewHost(surfaceId, root));
            }
        });
    }

    public static void detachSurfaceHost(String surfaceId) {
        mainHandler.post(() -> {
            ScriptViewHost existing = surfaceHosts.remove(surfaceId);
            if (existing != null) existing.dispose();
        });
    }

    public static void registerHandler(String type, SpotifyHook hook) {
        handlers.put(type, hook);
    }

    public static native void sendEvent(String type, String payload);
}