package com.lenerd.spotifyplus.manager.scripting.handlers;

import android.util.Log;
import com.lenerd.spotifyplus.manager.scripting.CommandHandler;
import org.json.JSONObject;

public class DebugHandler implements CommandHandler {
    @Override
    public void handle(JSONObject payload) throws Exception {
        String message = payload.optString("message", "");
        String level = payload.optString("level", "");
        if(message.isBlank() || level.isBlank()) return;

        switch(level) {
            case "debug":
                Log.d("SpotifyPlus", message);
                break;

            case "info":
                Log.i("SpotifyPlus", message);
                break;

            case "warning":
                Log.w("SpotifyPlus", message);
                break;

            case "error":
                Log.e("SpotifyPlus", message);
                break;
        }
    }
}