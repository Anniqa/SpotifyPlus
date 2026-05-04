import path from 'path';
import { Logger } from './core/logger';
import { HostRuntime } from './loader/host-runtime';
import { ScriptLoader } from './loader/script-loader';
import { PlatformData } from './core/models';

const logger = new Logger('SpotifyPlusHost');

function resolveScriptRoots(): string[] {
    const args = process.argv.slice(2);
    if (args.length > 0) return args.map(root => path.resolve(root));
    return [path.join(__dirname, 'scripts')];
}

async function main(): Promise<void> {
    logger.info(`host.ts starting, __dirname=${__dirname}`);

    const runtime = new HostRuntime(logger.child('Runtime'));
    const loader = new ScriptLoader(runtime, logger.child('Loader'));
    const scriptRoots = resolveScriptRoots();

    runtime.start();
    runtime.log('Spotify is ready!');

    loader.loadFromRoots(scriptRoots);
    // runtime.sendEvent('hostReady', { scriptRoots });

    runtime.log(`Host ready with ${scriptRoots.length} scripts`);
}

main().catch(error => {
    console.error('Host crashed', error);
});