package com.lenerd.spotifyplus.module.hooks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.lenerd.spotifyplus.manager.bridge.BridgeClient;
import com.lenerd.spotifyplus.module.SpotifyCallback;
import com.lenerd.spotifyplus.module.SpotifyHook;
import com.lenerd.spotifyplus.module.scripting.ScriptManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class StorageHook extends SpotifyHook {
    private static final String TYPE_TEXT = "text";
    private static final String TYPE_JSON = "json";
    private static final String TYPE_BINARY = "binary";

    @Override
    protected void hookSetup() throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        ScriptManager.registerHandler("storage", this);
    }

    @Override
    protected void beforeHook(SpotifyCallback callback) { }

    @Override
    protected void afterHook(SpotifyCallback callback) { }

    @Override
    public void handle(String id, String command, JSONObject json) {
        if (command.equals("set")) {
            handleSet(id, json);
        } else if (command.equals("get")) {
            handleGet(id, json);
        } else if (command.equals("remove")) {
            handleRemove(id, json);
        } else if (command.equals("write")) {
            handleWrite(id, json);
        } else if (command.equals("read")) {
            handleRead(id, json);
        }
    }

    private void handleSet(String id, JSONObject json) {
        try {
            if (currentActivity == null) return;

            String scriptId = json.getString("scriptId");
            String key = json.getString("key");
            Object value = json.opt("value");

            SharedPreferences prefs = currentActivity.getSharedPreferences(scriptId, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            if (value == null || value == JSONObject.NULL) {
                editor.remove(key).apply();
                return;
            }

            if (value instanceof String s) editor.putString(key, s).apply();
            else if (value instanceof Boolean b) editor.putBoolean(key, b).apply();
            else if (value instanceof Integer i) editor.putInt(key, i).apply();
            else if (value instanceof Long l) editor.putLong(key, l).apply();
            else if (value instanceof Float f) editor.putFloat(key, f).apply();
            else if (value instanceof Double d) editor.putString(key, d.toString()).apply();
            else editor.putString(key, value.toString()).apply();
        } catch (Exception e) {
            logError(e);
        }
    }

    private void handleGet(String id, JSONObject json) {
        try {
            if (currentActivity == null) {
                BridgeClient.send(id, "response", "storage.get", new JSONObject());
                return;
            }

            String scriptId = json.getString("scriptId");
            String key = json.getString("key");

            SharedPreferences prefs = currentActivity.getSharedPreferences(scriptId, Context.MODE_PRIVATE);
            Object value = prefs.getAll().get(key);

            if (value == null) BridgeClient.send(id, "response", "storage.get", new JSONObject());
            else BridgeClient.send(id, "response", "storage.get", new JSONObject().put("value", value));
        } catch (Exception e) {
            logError(e);
            BridgeClient.send(id, "response", "storage.get", new JSONObject());
        }
    }

    private void handleRemove(String id, JSONObject json) {
        try {
            if (currentActivity == null) return;

            String scriptId = json.getString("scriptId");
            String key = json.getString("key");

            SharedPreferences prefs = currentActivity.getSharedPreferences(scriptId, Context.MODE_PRIVATE);
            prefs.edit().remove(key).apply();
        } catch (Exception e) {
            logError(e);
        }
    }

    private void handleWrite(String id, JSONObject json) {
        try {
            if (currentActivity == null) return;

            String scriptId = json.getString("scriptId");
            String scriptPath = json.getString("path");

            File scriptDir = new File(currentActivity.getFilesDir(), scriptId);
            if (!scriptDir.exists()) scriptDir.mkdirs();

            File file = resolveScriptFile(scriptDir, scriptPath);

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            String type = json.optString("type", "");

            if (json.has("data")) {
                String data = json.getString("data");
                byte[] bytes = Base64.decode(data, Base64.NO_WRAP);
                Files.write(file.toPath(), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                writeMetaType(file, TYPE_BINARY);
                return;
            }

            Object value = json.opt("value");

            if (TYPE_JSON.equals(type) || value instanceof JSONObject || value instanceof JSONArray ||
                    value instanceof Boolean || value instanceof Integer || value instanceof Long ||
                    value instanceof Float || value instanceof Double || value == JSONObject.NULL) {

                String text = value == null || value == JSONObject.NULL ? "null" : value.toString();
                Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                writeMetaType(file, TYPE_JSON);
                return;
            }

            String text = value == null ? "" : String.valueOf(value);
            Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            writeMetaType(file, TYPE_TEXT);
        } catch (Exception e) {
            logError(e);
        }
    }

    private void handleRead(String id, JSONObject json) {
        try {
            if (currentActivity == null) {
                BridgeClient.send(id, "response", "storage.read", new JSONObject());
                return;
            }

            String scriptId = json.getString("scriptId");
            String scriptPath = json.getString("path");

            File scriptDir = new File(currentActivity.getFilesDir(), scriptId);
            File file = resolveScriptFile(scriptDir, scriptPath);

            if (!file.exists() || !file.isFile()) {
                BridgeClient.send(id, "response", "storage.read", new JSONObject());
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            String type = readMetaType(file);

            if (type == null || type.isEmpty()) {
                type = isValidUtf8(bytes) ? TYPE_TEXT : TYPE_BINARY;
            }

            if (TYPE_BINARY.equals(type)) {
                String data = Base64.encodeToString(bytes, Base64.NO_WRAP);
                BridgeClient.send(id, "response", "storage.read",
                        new JSONObject()
                                .put("type", TYPE_BINARY)
                                .put("data", data));
                return;
            }

            String text = new String(bytes, StandardCharsets.UTF_8);

            if (TYPE_JSON.equals(type)) {
                try {
                    Object value = new JSONTokener(text).nextValue();
                    BridgeClient.send(id, "response", "storage.read",
                            new JSONObject()
                                    .put("type", TYPE_JSON)
                                    .put("value", value));
                } catch (Exception e) {
                    BridgeClient.send(id, "response", "storage.read",
                            new JSONObject()
                                    .put("type", TYPE_TEXT)
                                    .put("value", text));
                }
                return;
            }

            BridgeClient.send(id, "response", "storage.read",
                    new JSONObject()
                            .put("type", TYPE_TEXT)
                            .put("value", text));
        } catch (Exception e) {
            logError(e);
            BridgeClient.send(id, "response", "storage.read", new JSONObject());
        }
    }

    private File resolveScriptFile(File scriptDir, String relativePath) throws IOException {
        File target = new File(scriptDir, relativePath);

        String root = scriptDir.getCanonicalPath();
        String path = target.getCanonicalPath();

        if (!path.startsWith(root + File.separator) && !path.equals(root)) {
            throw new SecurityException("Path escapes script sandbox");
        }

        return target;
    }

    private File getMetaFile(File file) {
        return new File(file.getParentFile(), "." + file.getName() + ".spmeta");
    }

    private void writeMetaType(File file, String type) throws IOException {
        File meta = getMetaFile(file);
        Files.write(meta.toPath(), type.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    private String readMetaType(File file) {
        try {
            File meta = getMetaFile(file);
            if (!meta.exists()) return null;
            return new String(Files.readAllBytes(meta.toPath()), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidUtf8(byte[] bytes) {
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            decoder.decode(ByteBuffer.wrap(bytes));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
}