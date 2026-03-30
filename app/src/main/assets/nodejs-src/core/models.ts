export interface SpotifyTrackData {
    uri: string;
    title: string;
    artist: string;
    album: string;
    durationMs: number;
    artworkUrl: string;
}

export class SpotifyTrack {
    readonly uri: string;
    readonly title: string;
    readonly artist: string;
    readonly album: string;
    readonly durationMs: number;
    readonly artworkUrl: string;

    constructor(data: SpotifyTrackData) {
        this.uri = data.uri;
        this.title = data.title;
        this.artist = data.artist;
        this.album = data.album;
        this.durationMs = data.durationMs;
        this.artworkUrl = data.artworkUrl;
    }

    static from(data: SpotifyTrackData): SpotifyTrack {
        return new SpotifyTrack(data);
    }

    get displayName(): string {
        return `${this.title} - ${this.artist}`;
    }

    toJSON(): SpotifyTrackData {
        return {
            uri: this.uri,
            title: this.title,
            artist: this.artist,
            album: this.album,
            durationMs: this.durationMs,
            artworkUrl: this.artworkUrl
        };
    }
}

export interface MenuItemDefinition {
    id: string;
    title: string;
}

export interface MenuContext {
    type: string;
    track?: SpotifyTrackData;
    [key: string]: unknown;
}