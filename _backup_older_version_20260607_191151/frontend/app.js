(function () {
    'use strict';

    function resolveApiOrigin() {
        const { protocol, hostname, port } = window.location;
        if (protocol === 'file:') {
            return 'http://127.0.0.1:8080';
        }

        const localHost =
            hostname === 'localhost' ||
            hostname === '127.0.0.1' ||
            hostname === '[::1]' ||
            hostname === '::1';

        if (localHost && String(port || '') !== '8080') {
            return `${protocol}//${hostname}:8080`;
        }

        return window.location.origin;
    }

    const API_ORIGIN = resolveApiOrigin();

    const state = {
        activeView: 'listen',
        user: null,
        playlist: null,
        library: null,
        savedPlaylists: [],
        insights: null,
    };

    const loginForm = document.getElementById('loginForm');
    const generatorForm = document.getElementById('generatorForm');
    const sidebarNav = document.getElementById('sidebarNav');
    const navButtons = Array.from(document.querySelectorAll('.nav-item'));
    const navDetailLabel = document.getElementById('navDetailLabel');
    const navDetailTitle = document.getElementById('navDetailTitle');
    const navDetailCopy = document.getElementById('navDetailCopy');
    const navDetailList = document.getElementById('navDetailList');
    const genreSelect = document.getElementById('genreSelect');
    const moodSelect = document.getElementById('moodSelect');
    const limitInput = document.getElementById('limitInput');
    const featuredGrid = document.getElementById('featuredGrid');
    const savedList = document.getElementById('savedList');
    const trackList = document.getElementById('trackList');
    const playlistTitle = document.getElementById('playlistTitle');
    const playlistSummary = document.getElementById('playlistSummary');
    const heroPlaylistName = document.getElementById('heroPlaylistName');
    const heroPlaylistHint = document.getElementById('heroPlaylistHint');
    const heroChips = document.getElementById('heroChips');
    const healthDot = document.getElementById('healthDot');
    const connectionText = document.getElementById('connectionText');
    const sessionUser = document.getElementById('sessionUser');
    const sessionPreference = document.getElementById('sessionPreference');
    const saveButton = document.getElementById('saveButton');
    const playlistPanel = document.getElementById('playlistPanel');
    const toast = document.getElementById('toast');
    const listenSection = document.getElementById('listenSection');
    const browseSection = document.getElementById('browseSection');
    const savedSection = document.getElementById('savedSection');

    const statSongs = document.getElementById('statSongs');
    const statSaved = document.getElementById('statSaved');
    const statGenerated = document.getElementById('statGenerated');
    const statHistory = document.getElementById('statHistory');

    function apiUrl(path, params) {
        const url = new URL(path, API_ORIGIN);
        if (params) {
            Object.entries(params).forEach(([key, value]) => {
                if (value !== undefined && value !== null && value !== '') {
                    url.searchParams.set(key, value);
                }
            });
        }
        return url.toString();
    }

    async function request(path, options, params) {
        const response = await fetch(apiUrl(path, params), options);
        const data = await response.json().catch(() => ({}));
        if (!response.ok) {
            throw new Error(data.error || 'Something went wrong.');
        }
        return data;
    }

    function showToast(message, isError) {
        toast.textContent = message;
        toast.style.background = isError ? 'rgba(180, 28, 53, 0.94)' : 'rgba(17, 17, 20, 0.9)';
        toast.classList.add('show');
        clearTimeout(showToast.timer);
        showToast.timer = window.setTimeout(() => toast.classList.remove('show'), 2600);
    }

    function encodeForm(values) {
        const params = new URLSearchParams();
        Object.entries(values).forEach(([key, value]) => {
            params.set(key, value);
        });
        return params;
    }

    function formatPlaylistMeta(playlist) {
        return `${playlist.trackCount} tracks • ${playlist.totalDurationLabel} • ${playlist.genre} • ${playlist.mood}`;
    }

    function targetForView(view) {
        if (view === 'browse') {
            return browseSection;
        }
        if (view === 'mixes') {
            return playlistPanel;
        }
        if (view === 'saved') {
            return savedSection;
        }
        return listenSection;
    }

    function getViewDetails(view) {
        const totalSongs = state.library ? state.library.totalSongs : 0;
        const genreCount = state.library ? state.library.genres.length : 0;
        const moodCount = state.library ? state.library.moods.length : 0;
        const savedCount = state.savedPlaylists.length;
        const generatedCount = state.insights ? state.insights.generatedPlaylists : 0;

        if (view === 'browse') {
            return {
                label: 'Browse',
                title: 'Explore the catalog.',
                copy: 'Move through the library and scan the available genres, moods, and featured tracks.',
                pills: [
                    `${totalSongs} songs loaded`,
                    `${genreCount} genres`,
                    `${moodCount} moods`,
                ],
            };
        }

        if (view === 'mixes') {
            if (state.playlist) {
                return {
                    label: 'Your Mixes',
                    title: state.playlist.name,
                    copy: state.playlist.summary,
                    pills: [
                        `${state.playlist.trackCount} tracks`,
                        state.playlist.totalDurationLabel,
                        `${generatedCount} mixes generated`,
                    ],
                };
            }

            return {
                label: 'Your Mixes',
                title: 'No mix generated yet.',
                copy: 'Choose a genre and mood, then create a playlist to fill this section instantly.',
                pills: ['Choose filters', 'Generate a mix', 'Save it when ready'],
            };
        }

        if (view === 'saved') {
            const savedNames = state.savedPlaylists.slice(0, 3).map((playlist) => playlist.name);
            return {
                label: 'Saved',
                title: savedCount ? 'Your shelf is ready.' : 'Nothing saved yet.',
                copy: savedCount
                    ? 'Your saved playlists stay here for quick replay and easy access.'
                    : 'Save a generated mix and it will appear here with full details.',
                pills: savedCount ? [`${savedCount} playlists saved`, ...savedNames] : ['Save a mix', 'Build a shelf'],
            };
        }

        return {
            label: 'Listen Now',
            title: state.playlist ? `Active highlight: ${state.playlist.name}` : 'Start with a clean home view.',
            copy: state.playlist
                ? 'Your latest mix is ready in the main stage with tracks, timings, and mood tags.'
                : 'Check the live library, sign in, and build a playlist in just a few clicks.',
            pills: [
                `${totalSongs} songs ready`,
                `${savedCount} saved`,
                `${generatedCount} generated`,
            ],
        };
    }

    function renderHeroState() {
        if (state.activeView === 'browse') {
            heroPlaylistName.textContent = 'Library In Motion';
            heroPlaylistHint.textContent = state.library
                ? `Explore ${state.library.genres.length} genres and ${state.library.moods.length} moods from one clean view.`
                : 'Loading the library...';
            return;
        }

        if (state.activeView === 'saved') {
            heroPlaylistName.textContent = state.savedPlaylists[0] ? state.savedPlaylists[0].name : 'Saved Shelf';
            heroPlaylistHint.textContent = state.savedPlaylists.length
                ? `${state.savedPlaylists.length} saved playlist${state.savedPlaylists.length === 1 ? '' : 's'} waiting for replay.`
                : 'Save a playlist to keep it on your shelf.';
            return;
        }

        if (state.activeView === 'mixes') {
            heroPlaylistName.textContent = state.playlist ? state.playlist.name : 'Your Next Mix';
            heroPlaylistHint.textContent = state.playlist
                ? `${state.playlist.trackCount} tracks ready to play or save.`
                : 'Generate a mix to see it appear here.';
            return;
        }

        heroPlaylistName.textContent = state.playlist ? state.playlist.name : 'Midnight Focus Mix';
        heroPlaylistHint.textContent = state.playlist
            ? state.playlist.summary
            : 'Choose a mood to generate your first playlist.';
    }

    function renderNavDetail() {
        const details = getViewDetails(state.activeView);
        navDetailLabel.textContent = details.label;
        navDetailTitle.textContent = details.title;
        navDetailCopy.textContent = details.copy;
        navDetailList.innerHTML = details.pills
            .map((pill) => `<span class="nav-detail-pill">${pill}</span>`)
            .join('');
        renderHeroState();
    }

    function setActiveView(view, shouldScroll) {
        state.activeView = view;
        navButtons.forEach((button) => {
            button.classList.toggle('active', button.dataset.view === view);
        });
        renderNavDetail();

        if (shouldScroll) {
            targetForView(view).scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    function setConnectionStatus(live, label) {
        healthDot.classList.toggle('live', live);
        connectionText.textContent = label;
    }

    function renderHeroChips() {
        if (!state.library) {
            heroChips.innerHTML = '';
            return;
        }

        const chips = [
            ...state.library.genres.slice(0, 3),
            ...state.library.moods.slice(0, 2),
        ];

        heroChips.innerHTML = chips
            .map((chip) => `<span class="hero-chip">${chip}</span>`)
            .join('');
    }

    function renderFeatured() {
        if (!state.library || !state.library.featuredSongs.length) {
            featuredGrid.innerHTML = '<div class="empty-state">No featured songs available yet.</div>';
            return;
        }

        featuredGrid.innerHTML = state.library.featuredSongs
            .map(
                (song) => `
                    <article class="song-card" style="--card-accent: ${song.accent}">
                        <span>${song.genre}</span>
                        <strong>${song.title}</strong>
                        <p>${song.artist}</p>
                        <p>${song.durationLabel} • ${song.moods.join(' / ')}</p>
                    </article>
                `,
            )
            .join('');
    }

    function renderPlaylist() {
        if (!state.playlist) {
            playlistTitle.textContent = 'No playlist yet';
            playlistSummary.textContent =
                'Generate a playlist after login and the recommended tracks will appear here.';
            trackList.className = 'track-list empty-state';
            trackList.textContent =
                'Generate your first mix to preview songs, durations, moods, and playlist details.';
            saveButton.classList.add('is-muted');
            playlistPanel.style.setProperty('--playlist-accent', '#f73755');
            renderNavDetail();
            return;
        }

        playlistTitle.textContent = state.playlist.name;
        playlistSummary.textContent = `${state.playlist.summary} ${formatPlaylistMeta(state.playlist)}`;
        playlistPanel.style.setProperty('--playlist-accent', state.playlist.accent || '#f73755');
        saveButton.classList.remove('is-muted');

        trackList.className = 'track-list';
        trackList.innerHTML = state.playlist.songs
            .map(
                (song, index) => `
                    <article class="track-row">
                        <span class="track-swatch" style="--song-accent: ${song.accent}"></span>
                        <div class="track-copy">
                            <strong>${index + 1}. ${song.title}</strong>
                            <p>${song.artist} • ${song.genre} • ${song.moods.join(' / ')}</p>
                        </div>
                        <div class="track-meta">${song.durationLabel}</div>
                    </article>
                `,
            )
            .join('');
        renderNavDetail();
    }

    function renderSaved() {
        if (!state.savedPlaylists.length) {
            savedList.className = 'saved-list empty-state';
            savedList.textContent = 'Saved playlists will appear here once you store a generated mix.';
            renderNavDetail();
            return;
        }

        savedList.className = 'saved-list';
        savedList.innerHTML = state.savedPlaylists
            .map(
                (playlist) => `
                    <article class="saved-card">
                        <strong>${playlist.name}</strong>
                        <span>${formatPlaylistMeta(playlist)}</span>
                        <p>${playlist.summary}</p>
                    </article>
                `,
            )
            .join('');
        renderNavDetail();
    }

    function renderStats() {
        const totalSongs = state.library ? state.library.totalSongs : 0;
        const saved = state.insights ? state.insights.savedPlaylists : 0;
        const generated = state.insights ? state.insights.generatedPlaylists : 0;
        const history = state.insights ? state.insights.history.length : 0;

        statSongs.textContent = `${totalSongs} songs`;
        statSaved.textContent = `${saved} playlists`;
        statGenerated.textContent = `${generated} mixes`;
        statHistory.textContent = `${history} sessions`;
    }

    function renderSession() {
        if (!state.user) {
            sessionUser.textContent = 'Not logged in';
            sessionPreference.innerHTML = 'Quick access: <strong>admin / 1234</strong>';
            renderNavDetail();
            return;
        }

        sessionUser.textContent = `${state.user.displayName} (${state.user.username})`;
        sessionPreference.textContent = `Favorite genres: ${state.user.favoriteGenres.join(', ')} • Favorite moods: ${state.user.favoriteMoods.join(', ')}`;
        renderNavDetail();
    }

    function populateFilters() {
        if (!state.library) {
            return;
        }

        const genres = ['Any', ...state.library.genres];
        const moods = ['Any', ...state.library.moods];

        genreSelect.innerHTML = genres.map((genre) => `<option value="${genre}">${genre}</option>`).join('');
        moodSelect.innerHTML = moods.map((mood) => `<option value="${mood}">${mood}</option>`).join('');

        genreSelect.value = state.user && state.user.favoriteGenres[0] ? state.user.favoriteGenres[0] : 'Any';
        moodSelect.value = state.user && state.user.favoriteMoods[0] ? state.user.favoriteMoods[0] : 'Any';
    }

    async function refreshUserData() {
        if (!state.user) {
            state.savedPlaylists = [];
            state.insights = null;
            renderSaved();
            renderStats();
            return;
        }

        const [savedData, insightsData] = await Promise.all([
            request(
                '/api/playlists',
                {
                    method: 'GET',
                    headers: { Accept: 'application/json' },
                },
                { username: state.user.username },
            ),
            request(
                '/api/insights',
                {
                    method: 'GET',
                    headers: { Accept: 'application/json' },
                },
                { username: state.user.username },
            ),
        ]);

        state.savedPlaylists = savedData.playlists;
        state.insights = insightsData;
        renderSaved();
        renderStats();
    }

    async function loadLibrary() {
        const data = await request('/api/library', {
            method: 'GET',
            headers: { Accept: 'application/json' },
        });
        state.library = data;
        populateFilters();
        renderHeroChips();
        renderFeatured();
        renderStats();
        renderNavDetail();
    }

    async function checkHealth() {
        try {
            const data = await request('/api/health', {
                method: 'GET',
                headers: { Accept: 'application/json' },
            });
            setConnectionStatus(true, `${data.app} live on Java ${data.runtime}`);
        } catch (error) {
            setConnectionStatus(false, 'Backend offline');
            showToast(error.message, true);
        }
    }

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        try {
            const username = document.getElementById('usernameInput').value.trim();
            const password = document.getElementById('passwordInput').value.trim();
            const data = await request('/api/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: encodeForm({ username, password }),
            });

            state.user = data.user;
            renderSession();
            populateFilters();
            await refreshUserData();
            setActiveView('listen', false);
            showToast('Login successful');
        } catch (error) {
            showToast(error.message, true);
        }
    });

    generatorForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        if (!state.user) {
            showToast('Please login first with admin / 1234.', true);
            return;
        }

        try {
            const data = await request('/api/recommendations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: encodeForm({
                    username: state.user.username,
                    genre: genreSelect.value,
                    mood: moodSelect.value,
                    limit: limitInput.value || '6',
                }),
            });

            state.playlist = data.playlist;
            renderPlaylist();
            await refreshUserData();
            setActiveView('mixes', true);
            showToast('Playlist generated');
        } catch (error) {
            showToast(error.message, true);
        }
    });

    sidebarNav.addEventListener('click', (event) => {
        const button = event.target.closest('.nav-item');
        if (!button) {
            return;
        }
        setActiveView(button.dataset.view || 'listen', true);
    });

    saveButton.addEventListener('click', async () => {
        if (!state.user) {
            setActiveView('listen', true);
            showToast('Please login before saving a playlist.', true);
            return;
        }

        if (!state.playlist) {
            setActiveView('mixes', true);
            showToast('Generate a playlist first so there is something to save.', true);
            return;
        }

        try {
            const data = await request('/api/playlists/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: encodeForm({
                    username: state.user.username,
                    playlistId: state.playlist.id,
                }),
            });

            await refreshUserData();
            setActiveView('saved', true);
            showToast(data.message || 'Playlist saved');
        } catch (error) {
            setActiveView('saved', true);
            showToast(error.message, true);
        }
    });

    Promise.all([checkHealth(), loadLibrary()])
        .then(() => {
            renderSession();
            renderPlaylist();
            renderNavDetail();
        })
        .catch((error) => {
            showToast(error.message, true);
        });
})();
