import React, { useMemo } from 'react'
import { View, Text, CommonViewProps } from 'spotifyplus/react'
import { SyllableMetadata } from '../Types/lyrics-types'
import { usePlaybackTime } from './clock';
import LinearGradient from './linear-gradient';

interface Props extends CommonViewProps {
    syllable: SyllableMetadata;
    relativeTime: number;
    timeScale: number;
    relativeStart: number;
    relativeEnd: number;
    duration: number;
    startScale: number;
    durationScale: number;
    isBackground?: boolean;
}

const SyllableView = ({ syllable, relativeTime, timeScale, relativeStart, relativeEnd, duration, startScale, durationScale, isBackground = false }: Props) => {
    const isActive = relativeTime >= relativeStart && relativeTime < relativeEnd;
    const isPast = relativeTime >= relativeEnd;

    const timeScaleThing = Math.max(0, Math.min(relativeTime / duration));
    const syllableTimeScale = Math.max(0, Math.min((timeScaleThing - startScale) / durationScale, 1));
    const text = `${syllable.Text}${syllable.IsPartOfWord ? '' : ' '}`;
    const fontSize = isBackground ? 12 : 24;

    return (
        <View style={{ position: 'relative', alignSelf: 'flex-start' }}>
            <Text fontSize={fontSize} textColor={'#9b9b9b'}>{text}</Text>

            <View style={{
                position: 'absolute',
                left: 0,
                top: 0,
                bottom: 0,
                width: (isPast ? '100%' : isActive ? `${syllableTimeScale * 100}%` : 0),
                overflow: 'hidden'
            }}>
                <Text fontSize={fontSize} textColor={'#ffffff'}>{text}</Text>
            </View>
        </View>
    )
}

export default SyllableView