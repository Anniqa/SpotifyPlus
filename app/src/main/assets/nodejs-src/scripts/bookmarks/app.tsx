import React, { useEffect, useState } from "react";
import { Text, VerticalStackLayout, View } from "../../ui/components";
import { SpotifyTrack } from "../../core/models";

interface Props {
    bookmarks: string[];
    SpotifyPlus: any,
    onOpenTrack?: (uri: string) => void;
    onDeleteTrack?: (uri: string) => void;
}

export default function App({ bookmarks, SpotifyPlus, onOpenTrack, onDeleteTrack }: Props) {
    const [tracks, setTracks] = useState<Record<string, SpotifyTrack>>({});

    useEffect(() => {
        let cancelled = false;

        (async () => {
            const entries = await Promise.all(bookmarks.map(async (uri) => [uri, await SpotifyPlus.Internal.getTrack(uri)] as const));
            if (cancelled) return;

            setTracks(Object.fromEntries(entries))
        })();

        return () => {
            cancelled = true;
        }
    }, [bookmarks]);

    return (
        <View backgroundColor={"#121212"} width={"match_parent"} height={"match_parent"} padding={16}>
            <VerticalStackLayout width={"match_parent"}>
                <Text fontSize={28} fontWeight={"bold"} color={"#FFFFFF"} paddingTop={40} paddingBottom={8}>
                    Bookmarks
                </Text>

                <Text fontSize={14} color={"#A0A0A0"} paddingBottom={16}>
                    Your saved tracks
                </Text>

                {bookmarks.length === 0 ? (
                    <View width={"match_parent"} backgroundColor={"#1E1E1E"} padding={16} borderRadius={12}>
                        <Text color={"#FFFFFF"} fontSize={16} fontWeight={"bold"}>
                            Nothing here yet
                        </Text>
                        <Text color={"#A0A0A0"} paddingTop={4}>
                            Save songs to see them here
                        </Text>
                    </View>
                ) : (
                    bookmarks.map((uri) => {
                        const track: SpotifyTrack = tracks[uri];
                        if (!track) return <Text key={uri} color={'#FFFFFF'}>Loading...</Text>

                        return (
                            <View key={uri} width={"match_parent"} backgroundColor={"#1E1E1E"} padding={14} marginBottom={10} borderRadius={12} flexDirection={"row"} alignItems={"center"}>
                                <View width={0} layoutWeight={1} paddingRight={12}>
                                    <Text color={"#FFFFFF"} fontSize={15} fontWeight={"500"}>
                                        {track.title}
                                    </Text>

                                    <Text color={"#A0A0A0"} fontSize={12} paddingTop={2}>
                                        {`${track.artist} • ${track.album.title}`}
                                    </Text>
                                </View>

                                <View width={"wrap_content"} flexDirection={"row"} alignItems={"center"}>
                                    <View width={"wrap_content"} backgroundColor={"#1DB954"} paddingHorizontal={12} paddingVertical={8} borderRadius={999} marginRight={8} onClick={() => onOpenTrack?.(uri)}>
                                        <Text color={"#FFFFFF"} fontSize={12} fontWeight={"bold"} onClick={() => SpotifyPlus.openUri(uri)}>
                                            Open
                                        </Text>
                                    </View>

                                    <View width={"wrap_content"} backgroundColor={"#2A2A2A"} paddingHorizontal={12} paddingVertical={8} borderRadius={999} onClick={() => onDeleteTrack?.(uri)}>
                                        <Text color={"#FF6B6B"} fontSize={12} fontWeight={"bold"}>
                                            Delete
                                        </Text>
                                    </View>
                                </View>
                            </View>
                        )
                    })
                )}
            </VerticalStackLayout>
        </View>
    );
}