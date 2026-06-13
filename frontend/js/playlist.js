(function (global) {
    'use strict';

    let queue = [];
    let queueIndex = 0;

    function setQueue(songs, startIndex) {
        queue = Array.isArray(songs) ? songs.slice() : [];
        queueIndex = Math.max(0, Math.min(startIndex || 0, Math.max(queue.length - 1, 0)));
    }

    function current() {
        return queue[queueIndex] || null;
    }

    function playNext() {
        if (queueIndex < queue.length - 1) {
            queueIndex += 1;
            return queue[queueIndex];
        }
        return null;
    }

    function playPrev() {
        if (queueIndex > 0) {
            queueIndex -= 1;
            return queue[queueIndex];
        }
        return null;
    }

    function shuffle() {
        for (let i = queue.length - 1; i > 0; i -= 1) {
            const j = Math.floor(Math.random() * (i + 1));
            [queue[i], queue[j]] = [queue[j], queue[i]];
        }
        queueIndex = 0;
        return queue.slice();
    }

    global.PulsePlaylist = {
        setQueue,
        current,
        playNext,
        playPrev,
        shuffle,
        getQueue() {
            return queue.slice();
        },
        getIndex() {
            return queueIndex;
        },
    };
})(window);
