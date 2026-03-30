import { EventHandler } from "./script-registry";
import { MenuItemDefinition, SpotifyTrack } from "../core/models";
import { Logger } from "../core/logger";
import { HostRuntime } from "./host-runtime";

export interface ScriptConsole {
    log: (...args: unknown[]) => void;
    warn: (...args: unknown[]) => void;
    error: (...args: unknown[]) => void;
}

export interface ScriptGlobals {
    SpotifyPlus: SpotifyPlusApi;
    console: ScriptConsole;
    setTimeout: typeof setTimeout;
    setInterval: typeof setInterval;
    clearTimeout: typeof clearTimeout;
    clearInterval: typeof clearInterval;
    global: unknown;
    globalThis: unknown;
}

export interface SpotifyPlusApi {
    readonly scriptId: string;
    readonly version: number;

    log(...args: unknown[]): void;
    warn(...args: unknown[]): void;
    error(...args: unknown[]): void;

    on(eventName: string, handler: EventHandler): void;
    off(eventName: string, handler: EventHandler): void;

    request<TPayload = unknown>(name: string, payload?: unknown): Promise<TPayload>;
    toast(text: string): void;
    openUrl(url: string): void;
    emit(eventName: string, payload?: unknown): void;

    Player: {
        getCurrentTrack(): Promise<SpotifyTrack | null>;
    }
    UI: {
        toast(text: string): void;
    }
    Menu: {
        addItem(item: MenuItemDefinition): void;
    }
}

export class ScriptApiFactory {
    constructor(private readonly runtime: HostRuntime, private readonly logger: Logger) { }

    create(scriptId: string): ScriptGlobals {
        const scriptLogger = this.logger.child(scriptId);

        const api: SpotifyPlusApi = {
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

        const scriptConsole: ScriptConsole = {
            log: (...args) => api.log(...args),
            warn: (...args) => api.warn(...args),
            error: (...args) => api.error(...args)
        };

        const globals: ScriptGlobals = {
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

function formatLogArgs(args: unknown[]): string {
    return args.map(arg => {
        if (typeof arg === 'string') return arg;
        try {
            return JSON.stringify(arg);
        } catch {
            return String(arg);
        }
    }).join(' ');
}