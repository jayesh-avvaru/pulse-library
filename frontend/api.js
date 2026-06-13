(function (global) {
    'use strict';

    function resolveApiBase() {
        const { protocol, hostname, port } = window.location;
        if (protocol === 'file:') {
            return 'http://localhost:8080/api';
        }
        const local =
            hostname === 'localhost' ||
            hostname === '127.0.0.1' ||
            hostname === '[::1]' ||
            hostname === '::1';
        if (local && String(port || '') !== '8080') {
            return `${protocol}//${hostname}:8080/api`;
        }
        return `${window.location.origin}/api`;
    }

    const API_BASE = resolveApiBase();

    async function request(path, options) {
        const response = await fetch(`${API_BASE}${path}`, options);
        const data = await response.json().catch(() => null);
        if (!response.ok) {
            const message = data && data.error ? data.error : 'Request failed';
            throw new Error(message);
        }
        return data;
    }

    async function requestSongs(path) {
        const data = await request(path, {
            method: 'GET',
            headers: { Accept: 'application/json' },
        });
        return Array.isArray(data) ? data : data?.tracks || data?.songs || [];
    }

    const API = {
        search: (q, limit = 20) =>
            requestSongs(`/search?q=${encodeURIComponent(q)}&limit=${limit}`),

        byGenre: (name, limit = 20) =>
            requestSongs(`/genre?name=${encodeURIComponent(name)}&limit=${limit}`),

        playlistByMood: (mood, limit = 15) =>
            requestSongs(`/playlist?mood=${encodeURIComponent(mood)}&limit=${limit}`),

        recommend: (artist, limit = 10) =>
            requestSongs(`/recommend?artist=${encodeURIComponent(artist)}&limit=${limit}`),

        trending: (limit = 20) =>
            requestSongs(`/trending?limit=${limit}`),

        getGenres: () =>
            request('/genres', {
                method: 'GET',
                headers: { Accept: 'application/json' },
            }),

        generatePlaylist: (genre, mood, count) =>
            request('/playlist/generate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    genre: genre || 'Any',
                    mood: mood || 'Any',
                    trackCount: count,
                }),
            }),

        savePlaylist: (playlist) =>
            request('/playlist/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(playlist),
            }),

        getSavedPlaylists: () =>
            request('/playlist/saved', {
                method: 'GET',
                headers: { Accept: 'application/json' },
            }),

        deletePlaylist: (id) =>
            request(`/playlist/delete/${encodeURIComponent(id)}`, {
                method: 'DELETE',
                headers: { Accept: 'application/json' },
            }),

        getMoodStats: () =>
            request('/stats/mood-distribution', {
                method: 'GET',
                headers: { Accept: 'application/json' },
            }),

        getGenreStats: () =>
            request('/stats/genre-count', {
                method: 'GET',
                headers: { Accept: 'application/json' },
            }),

        getDurationStats: () =>
            request('/stats/duration-distribution', {
                method: 'GET',
                headers: { Accept: 'application/json' },
            }),

        getOverviewStats: () =>
            request('/stats/overview', {
                method: 'GET',
                headers: { Accept: 'application/json' },
            }),

        health: () =>
            request('/health', {
                method: 'GET',
                headers: { Accept: 'application/json' },
            }),
    };

    global.API = API;
    global.API_BASE = API_BASE;
})(window);
