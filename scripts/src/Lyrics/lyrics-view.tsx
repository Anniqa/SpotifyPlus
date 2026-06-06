import React, { useEffect, useMemo, useRef, useState } from 'react'
import { View, Text, ScrollView, NativeView, FlatList, Pressable, StyleSheet } from 'spotifyplus/react'
import { TransformedLyrics } from './lyric-utilities'
import { StaticSyncedLyrics, SyllableSyncedLyrics } from '../Types/lyrics-types';
import { SpotifyPlus } from 'spotifyplus';
import SyllableVocalLine from '../Entities/syllable-vocals';
import InterludeView from '../Entities/interlude';

interface Props {
    lyrics: TransformedLyrics | undefined;
}

// const AnimatedBackground = NativeView('AnimatedBackground');
const ACTIVE_LINE_POLL_MS = 250;

const LyricsView = ({ lyrics }: Props) => {
    const scroller = useRef<FlatList | null>(null);
    const lastActiveIndex = useRef<number | null>(null);

    const [activeIndex, setActiveIndex] = useState(-1);

    const lineRanges = useMemo(() => {
        if (lyrics?.Type !== 'Syllable') return [];

        return lyrics.Content.map(item => getLineRange(item));
    }, [lyrics]);

    useEffect(() => {
        if (lyrics?.Type !== 'Syllable' || lineRanges.length === 0) return;

        lastActiveIndex.current = null;
        let disposed = false;
        let inFlight = false;

        const findActiveIndex = (positionSeconds: number) => {
            let low = 0;
            let high = lineRanges.length - 1;

            while (low <= high) {
                const middle = Math.floor((low + high) / 2);
                const range = lineRanges[middle];

                if (positionSeconds < range.start) high = middle - 1;
                else if (positionSeconds > range.end) low = middle + 1;
                else return middle;
            }

            return -1;
        };

        const updateActiveLine = async () => {
            if (disposed || inFlight) return;
            inFlight = true;

            try {
                const progressMs = await SpotifyPlus.Player.getProgress();
                if (disposed || progressMs == null) return;

                const activeIndex = findActiveIndex(progressMs / 1000);
                setActiveIndex(activeIndex);
                if (activeIndex === -1 || activeIndex === lastActiveIndex.current) return;

                lastActiveIndex.current = activeIndex;
                scroller.current?.scrollToIndex({
                    index: activeIndex,
                    animated: true,
                    viewPosition: 0.35
                });
            } catch (error) {
                SpotifyPlus.error('Failed to update active lyric line', error);
            } finally {
                inFlight = false;
            }
        };

        updateActiveLine();
        const interval = setInterval(updateActiveLine, ACTIVE_LINE_POLL_MS);

        return () => {
            disposed = true;
            clearInterval(interval);
        };
    }, [lyrics, lineRanges]);

    if (lyrics?.Type === 'Line') {
        return (
            <ScrollView style={{ flex: 1 }}>
                {(lyrics as unknown as StaticSyncedLyrics).Lines.map((line, index) => (
                    <Text key={index}>{line.Text}</Text>
                ))}
            </ScrollView>
        )
    }

    if (lyrics?.Type === 'Syllable') {
        return (
            <View style={{ flex: 1 }}>
                <FlatList style={{ flex: 1, elevation: 10, rowGap: 42 }} ref={scroller} data={lyrics.Content} renderItem={({ item, index }) => {
                    if (item.Type === 'Interlude') {
                        if (index !== activeIndex) return null;

                        return <InterludeView metadata={item} />
                    }

                    return (
                        <Pressable style={({ pressed }) => [
                            styles.lyricsLine,
                            pressed && styles.lyricsPressed
                        ]} onPress={() => SpotifyPlus.Player.seek(item.Lead.StartTime * 1000)}>
                            <SyllableVocalLine key={index} metadata={item} style={{ paddingVertical: 2 }} />
                        </Pressable>
                    )
                }} />
            </View>
        )
    }
}

function getLineRange(item: SyllableSyncedLyrics['Content'][number]) {
    if (item.Type !== 'Vocal') {
        return {
            start: item.StartTime,
            end: item.EndTime
        }
    }

    const starts = [
        item.Lead.StartTime,
        ...(item.Background?.map(vocal => vocal.StartTime) ?? [])
    ];

    const ends = [
        item.Lead.EndTime,
        ...(item.Background?.map(vocal => vocal.EndTime) ?? [])
    ];

    return {
        start: Math.min(...starts),
        end: Math.max(...ends)
    };
}

const styles = StyleSheet.create({
    lyricsLine: {
        flex: 1,
        flexDirection: 'column',
        padding: 6,
        borderRadius: 22
    },
    lyricsPressed: {
        backgroundColor: '#29FFFFFF'
    }
})

export default LyricsView
