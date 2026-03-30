"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.HostRuntime = void 0;
const crypto_1 = require("crypto");
const bridge_1 = require("../bridge/bridge");
const models_1 = require("../core/models");
const script_registry_1 = require("./script-registry");
class HostRuntime {
    constructor(logger) {
        this.logger = logger;
        this.pendingRequests = new Map();
        this.registry = new script_registry_1.ScriptRegistry(logger.child('Registry'));
        this.bridge = new bridge_1.Bridge(logger.child('Bridge'));
    }
    start() {
        this.bridge.startPolling(packet => {
            void this.handleIncomingPacket(packet);
        });
    }
    sendEvent(name, payload = {}) {
        this.bridge.send({ type: 'event', name, payload });
    }
    sendCommand(name, payload = {}) {
        this.bridge.send({ type: 'command', name, payload });
    }
    async request(name, payload = {}) {
        const id = (0, crypto_1.randomUUID)();
        return await new Promise((resolve, reject) => {
            this.pendingRequests.set(id, {
                resolve: value => resolve(value),
                reject,
                name
            });
            this.bridge.send({ id, type: 'request', name, payload });
        });
    }
    async getCurrentTrack() {
        const payload = await this.request('track.getCurrent', {});
        return payload ? models_1.SpotifyTrack.from(payload) : null;
    }
    async handleIncomingPacket(packet) {
        this.logger.info(`Incoming ${packet.type}:${packet.name}}`);
        switch (packet.type) {
            case 'event':
            case 'command':
                await this.registry.emit(packet.name, packet.payload);
                break;
            case 'response':
                this.handleResponse(packet);
                break;
            case 'error':
                this.handleErrorPacket(packet);
                break;
            case 'request':
                this.logger.warn(`Unexpected request form Java: ${packet.name}`);
                break;
        }
    }
    handleResponse(packet) {
        if (!packet.id) {
            this.logger.warn(`Response without ID for ${packet.name}`);
            return;
        }
        const pending = this.pendingRequests.get(packet.id);
        if (!pending) {
            this.logger.warn(`No pending request for response ${packet.id}`);
            return;
        }
        this.pendingRequests.delete(packet.id);
        pending.resolve(packet.payload);
    }
    handleErrorPacket(packet) {
        if (packet.id) {
            const pending = this.pendingRequests.get(packet.id);
            if (pending) {
                this.pendingRequests.delete(packet.id);
                const error = new Error(packet.payload?.message ?? `Request fialed: ${packet.name}`);
                if (packet.payload?.stack)
                    error.stack = packet.payload.stack;
                pending.reject(error);
                return;
            }
        }
        this.logger.error(`Unhandled error packet ${packet.name}`, packet.payload);
    }
}
exports.HostRuntime = HostRuntime;
