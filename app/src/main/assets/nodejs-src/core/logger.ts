export class Logger {
    constructor(private readonly tag: string) { }

    info(message: string, ...args: unknown[]): void {
        console.log(`[${this.tag}] ${message}`, ...args);
    }

    warn(message: string, ...args: unknown[]): void {
        console.warn(`[${this.tag}] ${message}`, ...args);
    }

    error(message: string, ...args: unknown[]): void {
        console.error(`[${this.tag}] ${message}`, ...args);
    }

    child(childTag: string): Logger {
        return new Logger(`${this.tag}:${childTag}`);
    }
}