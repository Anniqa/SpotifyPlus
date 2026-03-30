import fs from 'fs';
import path from 'path';
import vm from 'vm';
import { createRequire } from 'module';
import { Logger } from '../core/logger';
import { ScriptApiFactory } from './script-api';
import { ScriptManifest, parseManifest } from './script-manifest';
import { HostRuntime } from './host-runtime';

export class ScriptLoader {
    private readonly apiFactory: ScriptApiFactory;

    constructor(private readonly runtime: HostRuntime, private readonly logger: Logger) {
        this.apiFactory = new ScriptApiFactory(runtime, logger.child('Api'));
    }

    loadFromRoots(roots: string[]): void {
        for (const root of roots) this.loadFromRoot(root);
    }

    loadFromRoot(root: string): void {
        if (!fs.existsSync(root)) {
            this.logger.warn(`Scripts root does not exist: ${root}`);
            return;
        }

        const entries = fs.readdirSync(root, { withFileTypes: true });
        for (const entry of entries) {
            if (!entry.isDirectory()) continue;
            const scriptDirectory = path.join(root, entry.name);
            try {
                this.loadScript(scriptDirectory);
            } catch (error) {
                this.logger.error(`Failed to load script at ${scriptDirectory}`, error);
            }
        }
    }

    private loadScript(scriptDirectory: string): void {
        const manifest = this.readManifest(scriptDirectory);
        const entryPath = path.resolve(scriptDirectory, manifest.main);

        if (!fs.existsSync(entryPath)) throw new Error(`Script entry not found: ${entryPath}`);

        const source = fs.readFileSync(entryPath, 'utf8');
        const globals = this.apiFactory.create(manifest.id) as unknown as Record<string, unknown>;
        const localRequire = createRequire(entryPath);

        global.require = localRequire;
        globals.__filename = entryPath;
        global.__dirname = path.dirname(entryPath);
        globals.process = process;
        globals.Buffer = Buffer;

        const context = vm.createContext(globals, {
            name: `SpotifyPlusScript:${manifest.id}`,
            codeGeneration: {
                strings: true,
                wasm: false
            }
        });

        const script = new vm.Script(source, {
            filename: entryPath,
        });

        this.runtime.registry.registerScript({
            manifest,
            directoryPath: scriptDirectory
        });

        script.runInContext(context);
        this.logger.info(`Loaded script ${manifest.id} form ${entryPath}`);
    }

    private readManifest(scriptDirectory: string): ScriptManifest {
        const manifestPath = path.join(scriptDirectory, 'manifest.json');
        if (!fs.existsSync(manifestPath)) throw new Error(`Missing manifest.json in ${scriptDirectory}`);

        const rawText = fs.readFileSync(manifestPath, 'utf8');
        return parseManifest(JSON.parse(rawText));
    }
}