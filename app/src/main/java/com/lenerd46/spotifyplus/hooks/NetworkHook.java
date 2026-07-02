package com.lenerd46.spotifyplus.hooks;

import android.content.Context;
import android.content.SharedPreferences;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class NetworkHook extends SpotifyHook {
    private final Context context;

    public NetworkHook(Context context) {
        this.context = context;
    }

    @Override
    protected void hook() {
        SharedPreferences prefs = context.getSharedPreferences("SpotifyPlus", Context.MODE_PRIVATE);

        if (prefs.getBoolean("block_ads", false)) {
            Class<?> requestClass = XposedHelpers.findClassIfExists("okhttp3.Request", lpparm.classLoader);
            Class<?> httpUrlClass = XposedHelpers.findClassIfExists("okhttp3.HttpUrl", lpparm.classLoader);
            if (requestClass == null || httpUrlClass == null) {
                XposedBridge.log("[SpotifyPlus] NetworkHook disabled: OkHttp classes not found");
                return;
            }

            XposedBridge.hookAllConstructors(requestClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if (param.args == null || param.args.length == 0 || param.args[0] == null) return;

                        String url = param.args[0].toString();

                        //  || url.contains("gabo-receiver-service") || url.contains("net-fortune") || url.contains("darwin-experiments") || url.contains("speechless-sharing") || url.contains("pendragon")
                        if (url.contains("/ads")) {
                            Object companion = XposedHelpers.getStaticObjectField(httpUrlClass, "k");
                            if (companion != null) {
                                param.args[0] = XposedHelpers.callMethod(companion, "c", "http://127.0.0.1:404/");
                            }
                        }
                    } catch (Throwable t) {
                        XposedBridge.log("[SpotifyPlus] NetworkHook request rewrite skipped after error");
                        XposedBridge.log(t);
                    }
                }
            });
        }
    }
}