import path from 'path';
import { Logger } from '../core/logger';
import { Packet, parsePacket, stringify } from '../core/protocol';

interface NativeBridge {
    sendToJava(json: string): void;
    pollFromJava(): string | undefined;
}

export class Bridge {
    private readonly addon: NativeBridge;
    private pollingHandle: NodeJS.Timeout | null = null;

    constructor(private readonly logger: Logger) {
        const addonPath = path.join(__dirname, '..', 'spotifyplus_bridge.node');
        this.logger.info(`Loading addon from ${addonPath}`);
        this.addon = require(addonPath) as NativeBridge;
    }

    send(packet: Packet): void {
        const json = stringify(packet);
        this.logger.info(`Sending packet ${packet.type}:${packet.name}`);
        this.addon.sendToJava(json);
    }

    startPolling(onPacket: (packet: Packet) => void, intervalMs = 50): void {
        if (this.pollingHandle) return;

        this.pollingHandle = setInterval(() => {
            const json = this.addon.pollFromJava();
            if (!json) return;

            try {
                const packet = parsePacket(json);
                onPacket(packet);
            } catch (error) {
                this.logger.error('Failed to parse packet from Java', error);
            }
        }, intervalMs);
    }

    stopPolling(): void {
        if (!this.pollingHandle) return;
        clearInterval(this.pollingHandle);
        this.pollingHandle = null;
    }
}