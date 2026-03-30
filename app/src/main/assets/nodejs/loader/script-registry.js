"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ScriptRegistry = void 0;
class ScriptRegistry {
    constructor(logger) {
        this.logger = logger;
        this.scripts = new Map();
        this.eventHandlers = new Map();
    }
    registerScript(script) {
        if (this.scripts.has(script.manifest.id))
            throw new Error(`Duplicate script ID '${script.manifest.id}'`);
        this.scripts.set(script.manifest.id, script);
        this.logger.info(`Registered script ${script.manifest.id}`);
    }
    getScript(scriptId) {
        return this.scripts.get(scriptId);
    }
    getScripts() {
        return Array.from(this.scripts.values());
    }
    on(scriptId, eventName, handler) {
        let scriptMap = this.eventHandlers.get(eventName);
        if (!scriptMap) {
            scriptMap = new Map();
            this.eventHandlers.set(eventName, scriptMap);
        }
        let handlers = scriptMap.get(scriptId);
        if (!handlers) {
            handlers = new Set();
            scriptMap.set(scriptId, handlers);
        }
        handlers.add(handler);
        this.logger.info(`Script ${scriptId} subscribed to ${eventName}`);
    }
    off(scriptId, eventName, handler) {
        const scriptMap = this.eventHandlers.get(eventName);
        const handlers = scriptMap?.get(scriptId);
        if (!handlers)
            return;
        handlers.delete(handler);
        if (handlers.size === 0)
            scriptMap?.delete(scriptId);
        if (scriptMap && scriptMap.size === 0)
            this.eventHandlers.delete(eventName);
    }
    async emit(eventName, payload) {
        const scriptMap = this.eventHandlers.get(eventName);
        if (!scriptMap)
            return;
        for (const [scriptId, handlers] of scriptMap.entries()) {
            for (const handler of handlers.values()) {
                try {
                    await Promise.resolve(handler(payload));
                }
                catch (error) {
                    this.logger.error(`Handler failed for script ${scriptId} on event ${eventName}`, error);
                }
            }
        }
    }
}
exports.ScriptRegistry = ScriptRegistry;
