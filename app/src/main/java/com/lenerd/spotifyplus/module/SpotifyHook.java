package com.lenerd.spotifyplus.module;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;
import org.luckypray.dexkit.DexKitBridge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class SpotifyHook implements XposedInterface.Hooker {
    protected static XposedModule module;
    protected static XposedModuleInterface.PackageLoadedParam lpparam;
    protected static DexKitBridge bridge;
    protected static ClassLoader classLoader;
    public static Activity currentActivity;

    public void init(XposedModule module, XposedModuleInterface.PackageLoadedParam lpparam, DexKitBridge bridge) {
        SpotifyHook.module = module;
        SpotifyHook.lpparam = lpparam;
        SpotifyHook.bridge = bridge;
        SpotifyHook.classLoader = lpparam.getClassLoader();

        try {
            hookSetup();
        } catch(NoSuchMethodException | ClassNotFoundException | NoSuchFieldException e) {
            logError(e);
        }
    }

    protected abstract void hookSetup() throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException;

    protected Class<?> findClass(String name) {
        try {
            return Class.forName(name, false, classLoader);
        } catch (ClassNotFoundException e) {
            logError(e);
            return null;
        }
    }

    protected void hook(Method member) {
        module.hook(member, this.getClass());
    }

    protected static void hook(Method member, Class<? extends SpotifyHook> clazz) {
        module.hook(member, clazz);
    }

    protected void hook(Constructor<?> member) {
        module.hook(member, this.getClass());
    }

    protected static void log(String message) {
        Log.d("SpotifyPlus", message);
    }

    protected static void logError(String message) {
        Log.e("SpotifyPlus", message);
    }

    protected static void logError(Exception e) {
        Log.e("SpotifyPlus", e.getMessage(), e);
    }

    protected static void logError(Throwable t) {
        Log.e("SpotifyPlus", t.getMessage(), t);
    }

    protected static void logError(String message, Exception e) {
        Log.e("SpotifyPlus", message, e);
    }

    protected static void toast(String message) {
        if(currentActivity == null) return;

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show());
    }
}