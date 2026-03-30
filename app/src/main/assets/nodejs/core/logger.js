"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Logger = void 0;
class Logger {
    constructor(tag) {
        this.tag = tag;
    }
    info(message, ...args) {
        console.log(`[${this.tag}] ${message}`, ...args);
    }
    warn(message, ...args) {
        console.warn(`[${this.tag}] ${message}`, ...args);
    }
    error(message, ...args) {
        console.error(`[${this.tag}] ${message}`, ...args);
    }
    child(childTag) {
        return new Logger(`${this.tag}:${childTag}`);
    }
}
exports.Logger = Logger;
