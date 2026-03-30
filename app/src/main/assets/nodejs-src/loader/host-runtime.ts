import { randomUUID } from "crypto";
import { Bridge } from '../bridge/bridge';
import { Logger } from "../core/logger";
import { SpotifyTrack, SpotifyTrackData } from "../core/models";
import { ErrorPacket, Packet, ResponsePacket } from "../core/protocol";
import { ScriptRegistry } from "./script-registry";

interface PendingRequest {
    resolve: (payload: unknown) => void;
    reject: (error: Error) => void;
    name: string;
}

export class HostRuntime {
    readonly registry: ScriptRegistry;
    private readonly bridge: Bridge;
    private readonly pendingRequests = new Map<string, PendingRequest>();

    constructor(private readonly logger: Logger) {
        this.registry = new ScriptRegistry(logger.child('Registry'));
        this.bridge = new Bridge(logger.child('Bridge'));
    }

    start(): void {
        this.bridge.startPolling(packet => {
            void this.handleIncomingPacket(packet);
        });
    }

    sendEvent(name: string, payload: unknown = {}): void {
        this.bridge.send({ type: 'event', name, payload });
    }

    sendCommand(name: string, payload: unknown = {}): void {
        this.bridge.send({ type: 'command', name, payload });
    }

    async request<TPayload = unknown>(name: string, payload: unknown = {}): Promise<TPayload> {
        const id = randomUUID();

        return await new Promise<TPayload>((resolve, reject) => {
            this.pendingRequests.set(id, {
                resolve: value => resolve(value as TPayload),
                reject,
                name
            });

            this.bridge.send({ id, type: 'request', name, payload });
        });
    }

    async getCurrentTrack(): Promise<SpotifyTrack | null> {
        const payload = await this.request<SpotifyTrackData | null>('track.getCurrent', {});
        return payload ? SpotifyTrack.from(payload) : null;
    }

    private async handleIncomingPacket(packet: Packet): Promise<void> {
        this.logger.info(`Incoming ${packet.type}:${packet.name}}`);

        switch (packet.type) {
            case 'event':
            case 'command':
                await this.registry.emit(packet.name, packet.payload);
                break;

            case 'response':
                this.handleResponse(packet as ResponsePacket);
                break;

            case 'error':
                this.handleErrorPacket(packet as ErrorPacket<{ message?: string; stack?: string; code?: string }>);
                break;

            case 'request':
                this.logger.warn(`Unexpected request form Java: ${packet.name}`);
                break;
        }
    }

    private handleResponse(packet: ResponsePacket): void {
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

    private handleErrorPacket(packet: ErrorPacket<{ message?: string; stack?: string; code?: string }>): void {
        if (packet.id) {
            const pending = this.pendingRequests.get(packet.id);
            if (pending) {
                this.pendingRequests.delete(packet.id);
                const error = new Error(packet.payload?.message ?? `Request fialed: ${packet.name}`);

                if (packet.payload?.stack) error.stack = packet.payload.stack;
                pending.reject(error);
                return;
            }
        }

        this.logger.error(`Unhandled error packet ${packet.name}`, packet.payload);
    }
}