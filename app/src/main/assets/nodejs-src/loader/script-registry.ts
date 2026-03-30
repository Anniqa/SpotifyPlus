import { Logger } from "../core/logger";
import { ScriptManifest } from "./script-manifest";

export type EventHandler = (payload: unknown) => void | Promise<void>;

export interface Script {
    manifest: ScriptManifest;
    directoryPath: string;
}

export class ScriptRegistry {
    private readonly scripts = new Map<string, Script>();
    private readonly eventHandlers = new Map<string, Map<string, Set<EventHandler>>>();

    constructor(private readonly logger: Logger) { }

    registerScript(script: Script): void {
        if (this.scripts.has(script.manifest.id)) throw new Error(`Duplicate script ID '${script.manifest.id}'`);
        this.scripts.set(script.manifest.id, script);
        this.logger.info(`Registered script ${script.manifest.id}`);
    }

    getScript(scriptId: string): Script | undefined {
        return this.scripts.get(scriptId);
    }

    getScripts(): Script[] {
        return Array.from(this.scripts.values());
    }

    on(scriptId: string, eventName: string, handler: EventHandler): void {
        let scriptMap = this.eventHandlers.get(eventName);
        if (!scriptMap) {
            scriptMap = new Map<string, Set<EventHandler>>();
            this.eventHandlers.set(eventName, scriptMap);
        }

        let handlers = scriptMap.get(scriptId);
        if (!handlers) {
            handlers = new Set<EventHandler>();
            scriptMap.set(scriptId, handlers);
        }

        handlers.add(handler);
        this.logger.info(`Script ${scriptId} subscribed to ${eventName}`);
    }

    off(scriptId: string, eventName: string, handler: EventHandler): void {
        const scriptMap = this.eventHandlers.get(eventName);
        const handlers = scriptMap?.get(scriptId);
        if (!handlers) return;

        handlers.delete(handler);
        if (handlers.size === 0) scriptMap?.delete(scriptId);
        if (scriptMap && scriptMap.size === 0) this.eventHandlers.delete(eventName);
    }

    async emit(eventName: string, payload: unknown): Promise<void> {
        const scriptMap = this.eventHandlers.get(eventName);
        if (!scriptMap) return;

        for (const [scriptId, handlers] of scriptMap.entries()) {
            for (const handler of handlers.values()) {
                try {
                    await Promise.resolve(handler(payload));
                } catch (error) {
                    this.logger.error(`Handler failed for script ${scriptId} on event ${eventName}`, error);
                }
            }
        }
    }
}