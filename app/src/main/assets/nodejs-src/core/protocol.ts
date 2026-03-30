export type PacketType = 'event' | 'command' | 'request' | 'response' | 'error';

export interface PacketBase<TType extends PacketType = PacketType, TPayload = unknown> {
    id?: string;
    type: TType;
    name: string;
    payload: TPayload;
}

export type EventPacket<TPayload = unknown> = PacketBase<'event', TPayload>;
export type CommandPacket<TPayload = unknown> = PacketBase<'command', TPayload>;
export type RequestPacket<TPayload = unknown> = PacketBase<'request', TPayload>;
export type ResponsePacket<TPayload = unknown> = PacketBase<'response', TPayload>;
export type ErrorPacket<TPayload = unknown> = PacketBase<'error', TPayload>;

export type Packet<TPayload = unknown> = | EventPacket<TPayload> | CommandPacket<TPayload> | RequestPacket<TPayload> | ResponsePacket<TPayload> | ResponsePacket<TPayload> | ErrorPacket<TPayload>;

export interface ErrorPayload {
    message: string;
    stack?: string;
    code?: string;
}

export function isPacket(value: unknown): value is Packet {
    if (!value || typeof value !== 'object') return false;
    const packet = value as Record<string, unknown>;
    return typeof packet.type === 'string' && typeof packet.name === 'string' && 'payload' in packet;
}

export function parsePacket(json: string): Packet {
    const parsed = JSON.parse(json) as unknown;
    if (!isPacket(parsed)) throw new Error('Invalid packet');
    return parsed;
}

export function stringify(packet: Packet): string {
    return JSON.stringify(packet);
}