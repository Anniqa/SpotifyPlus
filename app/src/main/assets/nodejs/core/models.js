"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.SpotifyTrack = void 0;
class SpotifyTrack {
    constructor(data) {
        this.uri = data.uri;
        this.title = data.title;
        this.artist = data.artist;
        this.album = data.album;
        this.durationMs = data.durationMs;
        this.artworkUrl = data.artworkUrl;
    }
    static from(data) {
        return new SpotifyTrack(data);
    }
    get displayName() {
        return `${this.title} - ${this.artist}`;
    }
    toJSON() {
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
exports.SpotifyTrack = SpotifyTrack;
