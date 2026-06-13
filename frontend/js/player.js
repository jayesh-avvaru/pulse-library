(function (global) {
    'use strict';

    const audioPlayer = document.getElementById('previewAudio') || new Audio();
    audioPlayer.preload = 'none';

    let onPlayStateChange = null;
    let onEnded = null;

    function formatTime(secs) {
        if (!Number.isFinite(secs) || secs < 0) {
            return '0:00';
        }
        const m = Math.floor(secs / 60);
        const s = Math.floor(secs % 60)
            .toString()
            .padStart(2, '0');
        return `${m}:${s}`;
    }

    async function playSong(song) {
        if (!song) {
            return false;
        }
        if (!song.previewUrl) {
            return false;
        }

        if (audioPlayer.src !== song.previewUrl) {
            audioPlayer.src = song.previewUrl;
        }
        audioPlayer.currentTime = 0;
        await audioPlayer.play();
        return true;
    }

    function pause() {
        audioPlayer.pause();
    }

    async function resume() {
        await audioPlayer.play();
    }

    function toggle() {
        if (audioPlayer.paused) {
            return resume();
        }
        pause();
        return Promise.resolve();
    }

    function seekByPercent(percent) {
        if (!Number.isFinite(audioPlayer.duration) || audioPlayer.duration <= 0) {
            return;
        }
        audioPlayer.currentTime = Math.max(0, Math.min(1, percent)) * audioPlayer.duration;
    }

    function setVolume(value) {
        audioPlayer.volume = Math.max(0, Math.min(1, Number(value) || 0));
    }

    audioPlayer.addEventListener('play', () => {
        if (onPlayStateChange) onPlayStateChange(true);
    });
    audioPlayer.addEventListener('pause', () => {
        if (onPlayStateChange) onPlayStateChange(false);
    });
    audioPlayer.addEventListener('ended', () => {
        if (onEnded) onEnded();
    });
    audioPlayer.addEventListener('timeupdate', () => {
        if (global.PulsePlayer && global.PulsePlayer.onTimeUpdate) {
            global.PulsePlayer.onTimeUpdate(audioPlayer.currentTime, audioPlayer.duration);
        }
    });

    global.PulsePlayer = {
        audio: audioPlayer,
        formatTime,
        playSong,
        pause,
        resume,
        toggle,
        seekByPercent,
        setVolume,
        setOnPlayStateChange(fn) {
            onPlayStateChange = fn;
        },
        setOnEnded(fn) {
            onEnded = fn;
        },
        onTimeUpdate: null,
    };
})(window);
