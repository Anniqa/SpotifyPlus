package com.lenerd.spotifyplus.manager.scripting;

import org.json.JSONObject;

public interface CommandHandler {
    void handle(JSONObject payload) throws Exception;
}
