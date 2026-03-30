"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.isPacket = isPacket;
exports.parsePacket = parsePacket;
exports.stringify = stringify;
function isPacket(value) {
    if (!value || typeof value !== 'object')
        return false;
    const packet = value;
    return typeof packet.type === 'string' && typeof packet.name === 'string' && 'payload' in packet;
}
function parsePacket(json) {
    const parsed = JSON.parse(json);
    if (!isPacket(parsed))
        throw new Error('Invalid packet');
    return parsed;
}
function stringify(packet) {
    return JSON.stringify(packet);
}
