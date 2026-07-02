package com.lenerd46.spotifyplus.hooks;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import org.luckypray.dexkit.DexKitBridge;

public abstract class SpotifyHook {
    protected XC_LoadPackage.LoadPackageParam lpparm;
    protected DexKitBridge bridge;

    public void init(XC_LoadPackage.LoadPackageParam lpparm, DexKitBridge bridge) {
        this.lpparm = lpparm;
        this.bridge = bridge;
        try {
            hook();
        } catch (Throwable t) {
            XposedBridge.log("[SpotifyPlus] Disabled hook " + getClass().getSimpleName() + " after init failure");
            XposedBridge.log(t);
        }
    }

    protected abstract void hook();
}
