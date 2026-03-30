#include <node_api.h>
#include <string>
#include <dlfcn.h>
#include <android/log.h>

typedef void (*SendToJavaFn)(const char *);
typedef char *(*PollFromJavaFn)();
typedef void (*FreeStringFn)(char *);

static SendToJavaFn g_sendToJava = nullptr;
static PollFromJavaFn g_pollFromJava = nullptr;
static FreeStringFn g_freeString = nullptr;
static bool g_symbolsResolved = false;
static void *g_nativeLibHandle = nullptr;
static const char *TAG = "SpotifyPlusBridge";

static bool resolve_symbols()
{
    if (g_symbolsResolved)
    {
        return g_sendToJava && g_pollFromJava && g_freeString;
    }

    dlerror();

    g_nativeLibHandle = dlopen("libnative-lib.so", RTLD_NOW | RTLD_GLOBAL);
    if (!g_nativeLibHandle)
    {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "dlopen(libnative-lib.so) failed: %s", dlerror());
        g_symbolsResolved = true;
        return false;
    }

    g_sendToJava = (SendToJavaFn)dlsym(g_nativeLibHandle, "SpotifyPlusBridge_SendToJava");
    g_pollFromJava = (PollFromJavaFn)dlsym(g_nativeLibHandle, "SpotifyPlusBridge_PollFromJava");
    g_freeString = (FreeStringFn)dlsym(g_nativeLibHandle, "SpotifyPlusBridge_FreeString");
    g_symbolsResolved = true;

    __android_log_print(ANDROID_LOG_INFO, TAG, "Resolved symbols from libnative-lib.so: send=%p poll=%p free=%p", g_sendToJava, g_pollFromJava, g_freeString);

    if (!g_sendToJava || !g_pollFromJava || !g_freeString)
    {
        const char *err = dlerror();
        __android_log_print(ANDROID_LOG_ERROR, TAG, "dlsym failed: %s", err ? err : "null");
        return false;
    }

    return true;
}

static napi_value sendToJava(napi_env env, napi_callback_info info)
{
    size_t argc = 1;
    napi_value args[1];
    napi_value undefined;

    napi_get_undefined(env, &undefined);
    napi_get_cb_info(env, info, &argc, args, nullptr, nullptr);

    __android_log_print(ANDROID_LOG_INFO, TAG, "sendToJava called, argc=%zu", argc);

    if (argc < 1 || !resolve_symbols())
    {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "sendToJava failed: missing args or symbols");
        return undefined;
    }

    size_t strSize = 0;
    napi_get_value_string_utf8(env, args[0], nullptr, 0, &strSize);

    std::string json;
    json.resize(strSize);
    napi_get_value_string_utf8(env, args[0], json.data(), strSize + 1, &strSize);

    __android_log_print(ANDROID_LOG_INFO, TAG, "sendToJava payload: %s", json.c_str());
    g_sendToJava(json.c_str());
    return undefined;
}

static napi_value pollFromJava(napi_env env, napi_callback_info info)
{
    napi_value result;

    if (!resolve_symbols())
    {
        __android_log_write(ANDROID_LOG_ERROR, TAG, "pollFromJava failed: symbols not resolved");
        napi_get_undefined(env, &result);
        return result;
    }

    char *msg = g_pollFromJava();
    if (msg == nullptr)
    {
        napi_get_undefined(env, &result);
        return result;
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "pollFromJava received: %s", msg);
    napi_create_string_utf8(env, msg, NAPI_AUTO_LENGTH, &result);
    g_freeString(msg);
    return result;
}

static napi_value init(napi_env env, napi_value exports)
{
    __android_log_write(ANDROID_LOG_INFO, TAG, "Addon init called");

    napi_value sendFn, pollFn;

    napi_create_function(env, "sendToJava", NAPI_AUTO_LENGTH, sendToJava, nullptr, &sendFn);
    napi_set_named_property(env, exports, "sendToJava", sendFn);

    napi_create_function(env, "pollFromJava", NAPI_AUTO_LENGTH, pollFromJava, nullptr, &pollFn);
    napi_set_named_property(env, exports, "pollFromJava", pollFn);

    return exports;
}

NAPI_MODULE(NODE_GYP_MODULE_NAME, init)