SpotifyPlus.toast("Loading test script", "long");

const menuItem = new SpotifyPlus.ContextMenu('Test Item', () => {
    SpotifyPlus.toast('Clicked!');
}).register();

const sideDrawer = new SpotifyPlus.SideDrawer('Test Item', () => {
    SpotifyPlus.toast('Clicked!');
}).register();