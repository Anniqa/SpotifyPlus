"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.Bridge = void 0;
const path_1 = __importDefault(require("path"));
const protocol_1 = require("../core/protocol");
class Bridge {
    constructor(logger) {
        this.logger = logger;
        this.pollingHandle = null;
        const addonPath = path_1.default.join(__dirname, '..', 'spotifyplus_bridge.node');
        this.logger.info(`Loading addon from ${addonPath}`);
        this.addon = require(addonPath);
    }
    send(packet) {
        const json = (0, protocol_1.stringify)(packet);
        this.logger.info(`Sending packet ${packet.type}:${packet.name}`);
        this.addon.sendToJava(json);
    }
    startPolling(onPacket, intervalMs = 50) {
        if (this.pollingHandle)
            return;
        this.pollingHandle = setInterval(() => {
            const json = this.addon.pollFromJava();
            if (!json)
                return;
            try {
                const packet = (0, protocol_1.parsePacket)(json);
                onPacket(packet);
            }
            catch (error) {
                this.logger.error('Failed to parse packet from Java', error);
            }
        }, intervalMs);
    }
    stopPolling() {
        if (!this.pollingHandle)
            return;
        clearInterval(this.pollingHandle);
        this.pollingHandle = null;
    }
}
exports.Bridge = Bridge;
