"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.ScriptLoader = void 0;
const fs_1 = __importDefault(require("fs"));
const path_1 = __importDefault(require("path"));
const vm_1 = __importDefault(require("vm"));
const module_1 = require("module");
const script_api_1 = require("./script-api");
const script_manifest_1 = require("./script-manifest");
class ScriptLoader {
    constructor(runtime, logger) {
        this.runtime = runtime;
        this.logger = logger;
        this.apiFactory = new script_api_1.ScriptApiFactory(runtime, logger.child('Api'));
    }
    loadFromRoots(roots) {
        for (const root of roots)
            this.loadFromRoot(root);
    }
    loadFromRoot(root) {
        if (!fs_1.default.existsSync(root)) {
            this.logger.warn(`Scripts root does not exist: ${root}`);
            return;
        }
        const entries = fs_1.default.readdirSync(root, { withFileTypes: true });
        for (const entry of entries) {
            if (!entry.isDirectory())
                continue;
            const scriptDirectory = path_1.default.join(root, entry.name);
            try {
                this.loadScript(scriptDirectory);
            }
            catch (error) {
                this.logger.error(`Failed to load script at ${scriptDirectory}`, error);
            }
        }
    }
    loadScript(scriptDirectory) {
        const manifest = this.readManifest(scriptDirectory);
        const entryPath = path_1.default.resolve(scriptDirectory, manifest.main);
        if (!fs_1.default.existsSync(entryPath))
            throw new Error(`Script entry not found: ${entryPath}`);
        const source = fs_1.default.readFileSync(entryPath, 'utf8');
        const globals = this.apiFactory.create(manifest.id);
        const localRequire = (0, module_1.createRequire)(entryPath);
        global.require = localRequire;
        globals.__filename = entryPath;
        global.__dirname = path_1.default.dirname(entryPath);
        globals.process = process;
        globals.Buffer = Buffer;
        const context = vm_1.default.createContext(globals, {
            name: `SpotifyPlusScript:${manifest.id}`,
            codeGeneration: {
                strings: true,
                wasm: false
            }
        });
        const script = new vm_1.default.Script(source, {
            filename: entryPath,
        });
        this.runtime.registry.registerScript({
            manifest,
            directoryPath: scriptDirectory
        });
        script.runInContext(context);
        this.logger.info(`Loaded script ${manifest.id} form ${entryPath}`);
    }
    readManifest(scriptDirectory) {
        const manifestPath = path_1.default.join(scriptDirectory, 'manifest.json');
        if (!fs_1.default.existsSync(manifestPath))
            throw new Error(`Missing manifest.json in ${scriptDirectory}`);
        const rawText = fs_1.default.readFileSync(manifestPath, 'utf8');
        return (0, script_manifest_1.parseManifest)(JSON.parse(rawText));
    }
}
exports.ScriptLoader = ScriptLoader;
