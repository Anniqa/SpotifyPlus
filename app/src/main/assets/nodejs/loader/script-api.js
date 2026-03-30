"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ScriptApiFactory = void 0;
class ScriptApiFactory {
    constructor(runtime, logger) {
        this.runtime = runtime;
        this.logger = logger;
    }
    create(scriptId) {
        const scriptLogger = this.logger.child(scriptId);
        const api = {
            scriptId,
            version: 1,
            log: (...args) => scriptLogger.info(formatLogArgs(args)),
            warn: (...args) => scriptLogger.warn(formatLogArgs(args)),
            error: (...args) => scriptLogger.error(formatLogArgs(args)),
            on: (eventName, handler) => this.runtime.registry.on(scriptId, eventName, handler),
            off: (eventName, handler) => this.runtime.registry.off(scriptId, eventName, handler),
            request: (name, payload = {}) => this.runtime.request(name, payload),
            toast: text => this.runtime.sendCommand('ui.toast', { text }),
            openUrl: url => this.runtime.sendCommand('system.openUrl', { url }),
            emit: (eventName, payload = {}) => this.runtime.sendEvent(eventName, payload),
            Player: {
                getCurrentTrack: () => this.runtime.getCurrentTrack()
            },
            UI: {
                toast: text => this.runtime.sendCommand('ui.toast', { text })
            },
            Menu: {
                addItem: item => this.runtime.sendCommand('menu.addItem', { scriptId, ...item })
            }
        };
        const scriptConsole = {
            log: (...args) => api.log(...args),
            warn: (...args) => api.warn(...args),
            error: (...args) => api.error(...args)
        };
        const globals = {
            SpotifyPlus: api,
            console: scriptConsole,
            setTimeout,
            setInterval,
            clearTimeout,
            clearInterval,
            global: undefined,
            globalThis: undefined
        };
        globals.global = globals;
        globals.globalThis = globals;
        return globals;
    }
}
exports.ScriptApiFactory = ScriptApiFactory;
function formatLogArgs(args) {
    return args.map(arg => {
        if (typeof arg === 'string')
            return arg;
        try {
            return JSON.stringify(arg);
        }
        catch {
            return String(arg);
        }
    }).join(' ');
}
