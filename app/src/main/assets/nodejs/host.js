"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const path_1 = __importDefault(require("path"));
const logger_1 = require("./core/logger");
const host_runtime_1 = require("./loader/host-runtime");
const script_loader_1 = require("./loader/script-loader");
const logger = new logger_1.Logger('SpotifyPlusHost');
function resolveScriptRoots() {
    const args = process.argv.slice(2);
    if (args.length > 0)
        return args.map(root => path_1.default.resolve(root));
    return [path_1.default.join(__dirname, 'scripts')];
}
async function main() {
    logger.info(`host.ts starting, __dirname=${__dirname}`);
    const runtime = new host_runtime_1.HostRuntime(logger.child('Runtime'));
    const loader = new script_loader_1.ScriptLoader(runtime, logger.child('Loader'));
    const scriptRoots = resolveScriptRoots();
    runtime.start();
    logger.info('Waiting for Spotify to connect');
    await runtime.waitForSpotifyConnecting();
    logger.info('Waiting for Spotify to be ready');
    const spotifyReady = await runtime.waitForSpotifyReady();
    if (!spotifyReady) {
        logger.warn('Spotify did not become ready in time');
        return;
    }
    logger.info('Spotify is ready!');
    loader.loadFromRoots(scriptRoots);
    runtime.sendEvent('hostReady', { scriptRoots });
    logger.info(`Host ready with ${scriptRoots.length} scripts`);
}
main().catch(error => {
    logger.error('Host crashed', error);
});
