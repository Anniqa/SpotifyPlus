import React from 'react';
import App from './app';

console.log('Loading bookmarks!');
const main = async () => {
    //@ts-expect-error
    const bookmarks: string[] = await SpotifyPlus.Platform.Storage.read<string[]>('bookmarkss.json') || [];

    //@ts-expect-error
    new SpotifyPlus.SideDrawer('Bookmarks', () => {
        //@ts-expect-error
        return <App bookmarks={bookmarks} SpotifyPlus={SpotifyPlus} />;
    }).register();

    //@ts-expect-error
    new SpotifyPlus.ContextMenu('Bookmark this song', (uri: string) => {
        console.log(`Bookmarking song: ${uri}`);
        bookmarks.push(uri);
        //@ts-expect-error
        SpotifyPlus.Platform.Storage.write('bookmarkss.json', bookmarks);
    }).register();
};

main();