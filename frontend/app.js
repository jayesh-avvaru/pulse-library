(function () {
    'use strict';

    const MOODS = ['Uplift', 'Energy', 'Chill', 'Late Night', 'Focus', 'Workout'];
    const MOOD_PLAYLIST_MAP = {
        Uplift: 'happy',
        Energy: 'energetic',
        Chill: 'chill',
        'Late Night': 'romantic',
        Focus: 'focus',
        Workout: 'workout',
    };
    const PRESET_GENRES = ['Pop', 'Hip-Hop', 'Rock', 'Jazz', 'Classical', 'R&B', 'Electronic', 'Indie'];
    const MOOD_CHART_COLORS = {
        Uplift: '#ff7b7b',
        Energy: '#ff9f43',
        Chill: '#4dabf7',
        'Late Night': '#9775fa',
        Focus: '#20c997',
        Workout: '#f03e3e',
    };

    const GENRE_GRADIENTS = {
        Pop: 'linear-gradient(150deg, #ff6b7f, #f73755 80%)',
        Electronic: 'linear-gradient(150deg, #7a6bff, #5b3bff 80%)',
        Ambient: 'linear-gradient(150deg, #ffb55f, #ff8c42 80%)',
        Indie: 'linear-gradient(150deg, #4e8df5, #3bc9db 80%)',
        'R&B': 'linear-gradient(150deg, #8b5cf6, #7366f8 80%)',
        'Hip-Hop': 'linear-gradient(150deg, #ff6740, #e63946 80%)',
        Rock: 'linear-gradient(150deg, #d00000, #9b2226 80%)',
        default: 'linear-gradient(150deg, #ff6b7f, #1f2031 120%)',
    };

    const state = {
        tracks: [],
        genres: [],
        selectedMoods: new Set(),
        currentPlaylist: null,
        sessionPlaylists: [],
        savedPlaylists: [],
        playingTrack: null,
        isPlaying: false,
        playbackQueue: [],
        playbackIndex: -1,
        moodTracks: [],
        browseTracks: [],
        browseGenreFilter: null,
        charts: {},
        stats: {},
    };

    const moodOptions = document.getElementById('moodOptions');
    const moodSuggestions = document.getElementById('moodSuggestions');
    const moodLoading = document.getElementById('moodLoading');
    const moodHint = document.getElementById('moodHint');
    const generatorForm = document.getElementById('generatorForm');
    const genreSelect = document.getElementById('genreSelect');
    const moodSelect = document.getElementById('moodSelect');
    const limitInput = document.getElementById('limitInput');
    const trackList = document.getElementById('trackList');
    const playlistTitle = document.getElementById('playlistTitle');
    const playlistSummary = document.getElementById('playlistSummary');
    const saveButton = document.getElementById('saveButton');
    const savedList = document.getElementById('savedList');
    const sessionMixesList = document.getElementById('sessionMixesList');
    const browseByGenre = document.getElementById('browseByGenre');
    const browseFilterHint = document.getElementById('browseFilterHint');
    const overviewStats = document.getElementById('overviewStats');
    const sidebarNav = document.getElementById('sidebarNav');
    const mobileTabs = document.getElementById('mobileTabs');
    const appViews = Array.from(document.querySelectorAll('.app-view'));
    const themeToggle = document.getElementById('themeToggle');
    const toastStack = document.getElementById('toastStack');
    const nowPlayingBar = document.getElementById('nowPlayingBar');
    const nowPlayingTitle = document.getElementById('nowPlayingTitle');
    const nowPlayingArtist = document.getElementById('nowPlayingArtist');
    const nowPlayingMeta = document.getElementById('nowPlayingMeta');
    const nowPlayingDisc = document.getElementById('nowPlayingDisc');
    const playerEqualizer = document.getElementById('playerEqualizer');
    const playPauseButton = document.getElementById('playPauseButton');
    const prevButton = document.getElementById('prevButton');
    const nextButton = document.getElementById('nextButton');
    const progressBar = document.getElementById('progressBar');
    const progressTime = document.getElementById('progressTime');
    const previewAudio = document.getElementById('previewAudio');
    const volumeSlider = document.getElementById('volumeSlider');
    const heroArt = document.getElementById('heroArt');
    const heroDisc = document.getElementById('heroDisc');
    const heroEqualizer = document.getElementById('heroEqualizer');
    const heroPlaylistName = document.getElementById('heroPlaylistName');
    const heroPlaylistHint = document.getElementById('heroPlaylistHint');
    const healthDot = document.getElementById('healthDot');
    const connectionText = document.getElementById('connectionText');
    const mixesPlaylistTitle = document.getElementById('mixesPlaylistTitle');
    const mixesTrackList = document.getElementById('mixesTrackList');
    const radarPlaceholder = document.getElementById('radarPlaceholder');
    const radarChartWrap = document.getElementById('radarChartWrap');

    const sections = {
        listen: document.getElementById('listenSection'),
        browse: document.getElementById('browseSection'),
        mixes: document.getElementById('mixesSection'),
        saved: document.getElementById('savedSection'),
        stats: document.getElementById('statsSection'),
        mood: document.getElementById('moodPanel'),
        playlist: document.getElementById('playlistPanel'),
    };

    function showToast(message, type) {
        const toast = document.createElement('div');
        toast.className = `toast-item toast-${type || 'info'}`;
        const icon = type === 'success' ? '✓' : type === 'error' ? '✗' : 'ℹ';
        toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
        toastStack.appendChild(toast);
        requestAnimationFrame(() => toast.classList.add('show'));
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    function formatDurationMs(ms) {
        const total = Math.max(0, Math.floor((Number(ms) || 0) / 1000));
        return `${Math.floor(total / 60)}:${String(total % 60).padStart(2, '0')}`;
    }

    function normalizeSong(song) {
        return {
            id: String(song.id),
            title: song.title || 'Unknown',
            artist: song.artist || 'Unknown Artist',
            album: song.album || '',
            genre: song.genre || 'Music',
            artworkUrl: song.artworkUrl || '',
            previewUrl: song.previewUrl || null,
            duration: song.duration || 0,
            durationLabel: song.durationLabel || formatDurationMs(song.duration),
            moods: song.moods || [],
            energy: song.energy || 5,
            accent: '#f73755',
        };
    }

    function genreGradient(genre) {
        return GENRE_GRADIENTS[genre] || GENRE_GRADIENTS.default;
    }

    function moodBadgeClass(mood) {
        return `mood-badge mood-${mood.toLowerCase().replace(/\s+/g, '-')}`;
    }

    function trackCardHtml(track, index) {
        const moods = (track.moods || []).map((m) => `<span class="${moodBadgeClass(m)}">${m}</span>`).join('');
        const noPreview = !track.previewUrl ? '<span class="preview-badge">Preview unavailable</span>' : '';
        const art = track.artworkUrl
            ? `<img class="track-artwork" src="${track.artworkUrl}" alt="" onerror="this.style.display='none'">`
            : '';
        return `
            <article class="track-card fade-up-stagger" data-track-id="${track.id}"
                data-preview-url="${track.previewUrl || ''}"
                data-title="${track.title}"
                data-artist="${track.artist}"
                data-artwork="${track.artworkUrl || ''}"
                style="background:${genreGradient(track.genre)}; animation-delay:${index * 0.08}s">
                ${art}
                <button type="button" class="card-play-btn" data-play-id="${track.id}" aria-label="Play ${track.title}">▶</button>
                ${noPreview}
                <span class="genre-badge">${track.genre}</span>
                <strong>${track.title.length > 25 ? `${track.title.slice(0, 25)}…` : track.title}</strong>
                <p class="track-artist">${track.artist}</p>
                <div class="track-card-footer">
                    <span>${track.durationLabel}</span>
                    <div class="mood-badges">${moods}</div>
                </div>
            </article>
        `;
    }

    function filterTracksByMoods(tracks, moods) {
        if (!moods.size) return [];
        return tracks
            .filter((t) => (t.moods || []).some((m) => moods.has(m)))
            .slice(0, 6);
    }

    function renderMoodButtons() {
        moodOptions.innerHTML = MOODS.map(
            (mood) => `
                <button type="button" class="mood-option${state.selectedMoods.has(mood) ? ' active' : ''}" data-mood="${mood}">
                    ${mood}
                </button>
            `,
        ).join('');
    }

    async function renderMoodSuggestions() {
        if (!state.selectedMoods.size) {
            moodHint.textContent = 'Select one or more moods — tracks matching any selected mood appear below.';
            moodSuggestions.innerHTML =
                '<p class="mood-suggestions-empty">Choose a mood to see matching tracks from iTunes.</p>';
            return;
        }

        moodLoading.classList.remove('hidden');
        moodSuggestions.innerHTML = '';
        try {
            const moodKeys = [...state.selectedMoods].map((m) => MOOD_PLAYLIST_MAP[m] || m.toLowerCase());
            const results = await Promise.all(moodKeys.map((m) => API.playlistByMood(m, 6)));
            const seen = new Set();
            const merged = [];
            results.flat().forEach((raw) => {
                const song = normalizeSong(raw);
                if (!seen.has(song.id)) {
                    seen.add(song.id);
                    merged.push(song);
                }
            });
            state.moodTracks = merged.slice(0, 6);
            moodHint.textContent = `${[...state.selectedMoods].join(', ')} — ${state.moodTracks.length} live tracks from iTunes.`;
            if (!state.moodTracks.length) {
                moodSuggestions.innerHTML = '<p class="mood-suggestions-empty">No tracks found for these moods.</p>';
                return;
            }
            moodSuggestions.innerHTML = state.moodTracks.map((t, i) => trackCardHtml(t, i)).join('');
        } catch (err) {
            showToast(err.message, 'error');
        } finally {
            moodLoading.classList.add('hidden');
        }
    }

    async function toggleMood(mood) {
        if (state.selectedMoods.has(mood)) {
            state.selectedMoods.delete(mood);
        } else {
            state.selectedMoods.add(mood);
        }
        renderMoodButtons();
        await renderMoodSuggestions();
    }

    function renderGeneratedPlaylist(playlist, skeleton) {
        if (skeleton) {
            trackList.className = 'track-list skeleton-grid';
            trackList.innerHTML = Array.from({ length: 6 })
                .map(() => '<div class="skeleton-card"></div>')
                .join('');
            renderMixesPlaylist(null, true);
            return;
        }

        if (!playlist) {
            playlistTitle.textContent = 'No playlist yet';
            playlistSummary.textContent = 'Generate a playlist and tracks will appear here.';
            trackList.className = 'track-list empty-state';
            trackList.textContent = 'Generate your first mix to see recommended tracks.';
            saveButton.classList.add('is-muted');
            renderMixesPlaylist(null, false);
            return;
        }

        const tracks = playlist.tracks || playlist.songs || [];
        playlistTitle.textContent = playlist.title || playlist.name;
        playlistSummary.textContent = playlist.summary || `${tracks.length} tracks • ${playlist.totalDurationLabel || ''}`;
        saveButton.classList.remove('is-muted');
        trackList.className = 'track-list generated-grid';
        trackList.innerHTML = tracks.map((t, i) => trackCardHtml(t, i)).join('');
        renderMixesPlaylist(playlist, false);
        updateChartsForPlaylist(playlist);
    }

    function renderMixesPlaylist(playlist, skeleton) {
        if (!mixesPlaylistTitle || !mixesTrackList) {
            return;
        }

        if (skeleton) {
            mixesPlaylistTitle.textContent = 'Loading mix...';
            mixesTrackList.className = 'track-list skeleton-grid';
            mixesTrackList.innerHTML = Array.from({ length: 4 })
                .map(() => '<div class="skeleton-card"></div>')
                .join('');
            return;
        }

        if (!playlist) {
            mixesPlaylistTitle.textContent = 'No mix selected';
            mixesTrackList.className = 'track-list empty-state';
            mixesTrackList.textContent = 'Open a session mix or generate a new playlist.';
            return;
        }

        const tracks = playlist.tracks || playlist.songs || [];
        mixesPlaylistTitle.textContent = playlist.title || playlist.name;
        mixesTrackList.className = 'track-list generated-grid';
        mixesTrackList.innerHTML = tracks.map((t, i) => trackCardHtml(t, i)).join('');
    }

    function renderSessionMixes() {
        if (!state.sessionPlaylists.length) {
            sessionMixesList.className = 'saved-list empty-state';
            sessionMixesList.textContent = 'Playlists you generate this session appear here.';
            return;
        }
        sessionMixesList.className = 'saved-list';
        sessionMixesList.innerHTML = state.sessionPlaylists
            .map(
                (p) => `
                <article class="saved-card" data-playlist-id="${p.id}">
                    <strong>${p.title || p.name}</strong>
                    <span>${p.trackCount || (p.tracks || []).length} tracks • ${p.mood || 'Any'} • ${p.genre || 'Any'}</span>
                    <p>${p.summary || ''}</p>
                </article>
            `,
            )
            .join('');
    }

    function renderSavedShelf() {
        if (!state.savedPlaylists.length) {
            savedList.className = 'saved-list empty-state';
            savedList.textContent = 'Saved playlists will appear here.';
            return;
        }
        savedList.className = 'saved-list';
        savedList.innerHTML = state.savedPlaylists
            .map((p) => {
                const date = (p.savedAt || p.createdAt || '').split('T')[0] || 'Today';
                const count = p.trackCount || (p.tracks || p.songs || []).length;
                return `
                    <article class="saved-card shelf-card" data-playlist-id="${p.id}">
                        <div class="shelf-head">
                            <strong>${p.title || p.name}</strong>
                            <button type="button" class="delete-btn" data-delete-id="${p.id}" aria-label="Delete playlist">✕</button>
                        </div>
                        <span>${count} tracks • Saved ${date}</span>
                        <p>${p.summary || `${p.mood || 'Any'} • ${p.genre || 'Any'}`}</p>
                    </article>
                `;
            })
            .join('');
    }

    function renderBrowse() {
        if (!browseByGenre) return;
        if (state.browseGenreFilter) {
            browseFilterHint.textContent = `Showing ${state.browseGenreFilter} from iTunes`;
            const tracks = state.browseTracks.length ? state.browseTracks : state.tracks;
            browseByGenre.innerHTML = `
                <div class="browse-group" data-genre="${state.browseGenreFilter}">
                    <h4>${state.browseGenreFilter} <span>${tracks.length}</span></h4>
                    <div class="featured-grid">${tracks.map((t, i) => trackCardHtml(t, i)).join('')}</div>
                </div>`;
            return;
        }

        browseFilterHint.textContent = 'Choose a genre to load live tracks';
        browseByGenre.innerHTML = `
            <div class="genre-links">
                ${PRESET_GENRES.map(
                    (genre) => `<button type="button" class="mood-option genre-link" data-genre="${genre}">${genre}</button>`,
                ).join('')}
            </div>
            <div class="featured-grid" style="margin-top:16px">
                ${state.tracks.slice(0, 12).map((t, i) => trackCardHtml(t, i)).join('')}
            </div>`;
    }

    async function loadBrowseGenre(genre) {
        state.browseGenreFilter = genre;
        browseByGenre.innerHTML = '<div class="spinner-wrap"><div class="spinner"></div></div>';
        try {
            const songs = await API.byGenre(genre, 20);
            state.browseTracks = songs.map(normalizeSong);
            renderBrowse();
            setActiveView('browse', false);
        } catch (err) {
            showToast(err.message, 'error');
            state.browseGenreFilter = null;
            renderBrowse();
        }
    }

    function animateCount(el, target, suffix) {
        let current = 0;
        const step = Math.ceil(target / 30) || 1;
        const timer = setInterval(() => {
            current = Math.min(target, current + step);
            el.textContent = `${current}${suffix || ''}`;
            if (current >= target) clearInterval(timer);
        }, 30);
    }

    function renderOverviewStats(data) {
        const cards = [
            { label: 'Total Tracks', value: data.totalTracks, suffix: '' },
            { label: 'Total Genres', value: data.totalGenres, suffix: '' },
            { label: 'Total Moods', value: data.totalMoods, suffix: '' },
            { label: 'Avg Duration', value: 0, suffix: '', display: data.avgDurationLabel || '0:00' },
        ];
        overviewStats.innerHTML = cards
            .map(
                (c, i) => `
                <article class="overview-card fade-up-stagger" style="animation-delay:${i * 0.1}s">
                    <span>${c.label}</span>
                    <strong class="count-up" data-target="${c.value}" data-display="${c.display || ''}">0</strong>
                    <svg class="sparkline" viewBox="0 0 60 20"><polyline points="0,15 10,12 20,14 30,8 40,10 50,6 60,4"/></svg>
                </article>
            `,
            )
            .join('');
        overviewStats.querySelectorAll('.count-up').forEach((el) => {
            const display = el.dataset.display;
            if (display) {
                el.textContent = display;
                return;
            }
            animateCount(el, Number(el.dataset.target), '');
        });
    }

    function playlistMoodCounts(playlist) {
        const counts = {};
        MOODS.forEach((m) => (counts[m] = 0));
        (playlist?.tracks || playlist?.songs || []).forEach((t) => {
            (t.moods || []).forEach((m) => {
                if (counts[m] !== undefined) counts[m] += 1;
            });
        });
        return counts;
    }

    function playlistEnergyScores(playlist) {
        const scores = {};
        MOODS.forEach((m) => (scores[m] = 0));
        const tracks = playlist?.tracks || playlist?.songs || [];
        tracks.forEach((t) => {
            (t.moods || []).forEach((m) => {
                scores[m] = (scores[m] || 0) + (t.energy || 5);
            });
        });
        return scores;
    }

    function destroyChart(key) {
        if (state.charts[key]) {
            state.charts[key].destroy();
            delete state.charts[key];
        }
    }

    function initCharts() {
        const moodCtx = document.getElementById('moodDonutChart');
        const genreCtx = document.getElementById('genreBarChart');
        const durationCtx = document.getElementById('durationAreaChart');
        const radarCtx = document.getElementById('energyRadarChart');
        if (!window.Chart) return;

        destroyChart('mood');
        destroyChart('genre');
        destroyChart('duration');
        destroyChart('radar');

        const moodData = state.stats.mood || {};
        state.charts.mood = new Chart(moodCtx, {
            type: 'doughnut',
            data: {
                labels: Object.keys(moodData),
                datasets: [{
                    data: Object.values(moodData),
                    backgroundColor: Object.keys(moodData).map((m) => MOOD_CHART_COLORS[m] || '#f73755'),
                }],
            },
            options: { plugins: { legend: { position: 'right' } }, animation: { duration: 900 } },
        });

        const genreData = state.stats.genre || {};
        const genreLabels = Object.keys(genreData);
        state.charts.genre = new Chart(genreCtx, {
            type: 'bar',
            data: {
                labels: genreLabels,
                datasets: [{
                    label: 'Tracks',
                    data: genreLabels.map((g) => genreData[g]),
                    backgroundColor: genreLabels.map((_, i) => `rgba(247,55,85,${0.35 + i * 0.08})`),
                    borderRadius: 8,
                }],
            },
            options: {
                indexAxis: 'y',
                onClick: (_, elements) => {
                    if (!elements.length) return;
                    state.browseGenreFilter = genreLabels[elements[0].index];
                    renderBrowse();
                    setActiveView('browse', true);
                    showToast(`Browsing ${state.browseGenreFilter}`, 'info');
                },
                plugins: {
                    tooltip: {
                        callbacks: {
                            afterLabel: (ctx) => {
                                const g = genreLabels[ctx.dataIndex];
                                const names = state.tracks.filter((t) => t.genre === g).map((t) => t.title).slice(0, 4);
                                return names.join(', ');
                            },
                        },
                    },
                },
            },
        });

        const durationData = state.stats.duration || {};
        const dLabels = Object.keys(durationData);
        state.charts.duration = new Chart(durationCtx, {
            type: 'line',
            data: {
                labels: dLabels,
                datasets: [{
                    label: 'Tracks',
                    data: dLabels.map((k) => durationData[k]),
                    fill: true,
                    tension: 0.4,
                    borderColor: '#f73755',
                    backgroundColor: 'rgba(247,55,85,0.25)',
                }],
            },
            options: { animation: { duration: 1000 } },
        });

        state.charts.radar = new Chart(radarCtx, {
            type: 'radar',
            data: {
                labels: MOODS,
                datasets: [{
                    label: 'Energy balance',
                    data: MOODS.map((m) => state.stats.radar?.[m] || 0),
                    backgroundColor: 'rgba(247,55,85,0.35)',
                    borderColor: '#f73755',
                }],
            },
            options: { scales: { r: { beginAtZero: true } } },
        });
    }

    function updateChartsForPlaylist(playlist) {
        state.stats.mood = playlist ? playlistMoodCounts(playlist) : state.stats.libraryMood || {};
        state.stats.radar = playlist ? playlistEnergyScores(playlist) : {};
        if (radarPlaceholder && radarChartWrap) {
            const hasPlaylist = Boolean(playlist && (playlist.tracks || playlist.songs || []).length);
            radarPlaceholder.classList.toggle('hidden', hasPlaylist);
            radarChartWrap.classList.toggle('hidden', !hasPlaylist);
        }
        if (state.charts.mood) {
            state.charts.mood.data.labels = Object.keys(state.stats.mood);
            state.charts.mood.data.datasets[0].data = Object.values(state.stats.mood);
            state.charts.mood.data.datasets[0].backgroundColor = Object.keys(state.stats.mood).map(
                (m) => MOOD_CHART_COLORS[m] || '#f73755',
            );
            state.charts.mood.update();
        }
        if (state.charts.radar) {
            state.charts.radar.data.datasets[0].data = MOODS.map((m) => state.stats.radar[m] || 0);
            state.charts.radar.update();
        }
    }

    async function loadStats() {
        if (state.tracks.length) {
            computeStatsFromTracks(state.tracks);
        }
    }

    function setActiveView(view, scroll) {
        document.querySelectorAll('.nav-item, .mobile-tab').forEach((btn) => {
            btn.classList.toggle('active', btn.dataset.view === view);
        });
        appViews.forEach((viewEl) => {
            viewEl.classList.toggle('active', viewEl.id === `view-${view}`);
        });
        const target = document.getElementById(`view-${view}`) || sections[view] || sections.listen;
        if (scroll && target) {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    function clearTrackHighlights() {
        document.querySelectorAll('[data-track-id].is-playing').forEach((el) => el.classList.remove('is-playing'));
    }

    function formatTime(seconds) {
        const safe = Number.isFinite(seconds) ? Math.max(0, Math.floor(seconds)) : 0;
        return `${Math.floor(safe / 60)}:${String(safe % 60).padStart(2, '0')}`;
    }

    function updateProgressUi() {
        if (!progressBar || !progressTime) return;
        const audio = PulsePlayer.audio;
        const duration = Number.isFinite(audio.duration) ? audio.duration : 30;
        const currentTime = Number.isFinite(audio.currentTime) ? audio.currentTime : 0;
        const percent = duration > 0 ? (currentTime / duration) * 100 : 0;
        progressBar.style.width = `${Math.max(0, Math.min(percent, 100))}%`;
        progressTime.textContent = `${PulsePlayer.formatTime(currentTime)} / ${PulsePlayer.formatTime(duration)}`;
    }

    function syncPlaybackUi() {
        const activeTrack = state.playingTrack;
        const isPlaying = state.isPlaying && Boolean(activeTrack);

        if (nowPlayingBar) {
            nowPlayingBar.hidden = !activeTrack;
        }
        document.body.classList.toggle('has-player', Boolean(activeTrack));

        clearTrackHighlights();

        if (!activeTrack) {
            [nowPlayingDisc, heroArt, heroDisc].forEach((el) => {
                if (el) {
                    el.classList.remove('is-playing');
                }
            });
            [playerEqualizer, heroEqualizer].forEach((el) => {
                if (el) {
                    el.classList.remove('is-active');
                }
            });
            if (playPauseButton) {
                playPauseButton.textContent = '▶';
            }
            if (progressBar) {
                progressBar.style.width = '0%';
            }
            if (progressTime) {
                progressTime.textContent = '0:00 / 0:30';
            }
            return;
        }

        if (nowPlayingTitle) {
            nowPlayingTitle.textContent = activeTrack.title;
        }
        if (nowPlayingArtist) {
            nowPlayingArtist.textContent = activeTrack.artist;
        }
        if (nowPlayingMeta) {
            const previewLabel =
                activeTrack.previewTrackName && activeTrack.previewArtist
                    ? `Preview: ${activeTrack.previewTrackName} - ${activeTrack.previewArtist}`
                    : 'Preview sample';
            nowPlayingMeta.textContent = `${activeTrack.durationLabel} • ${activeTrack.genre} • ${previewLabel}`;
        }
        if (heroPlaylistName) {
            heroPlaylistName.textContent = activeTrack.title;
        }
        if (heroPlaylistHint) {
            heroPlaylistHint.textContent = isPlaying
                ? `Playing preview • ${activeTrack.artist}`
                : `Preview paused • ${activeTrack.artist}`;
        }
        if (playPauseButton) {
            playPauseButton.textContent = isPlaying ? '⏸' : '▶';
        }
        if (prevButton) {
            prevButton.disabled = PulsePlaylist.getIndex() <= 0;
        }
        if (nextButton) {
            nextButton.disabled = PulsePlaylist.getIndex() >= PulsePlaylist.getQueue().length - 1;
        }

        [nowPlayingDisc, heroArt, heroDisc].forEach((el) => {
            if (el) {
                el.classList.toggle('is-playing', isPlaying);
            }
        });
        [playerEqualizer, heroEqualizer].forEach((el) => {
            if (el) {
                el.classList.toggle('is-active', isPlaying);
            }
        });

        document.querySelectorAll(`[data-track-id="${activeTrack.id}"]`).forEach((el) => el.classList.add('is-playing'));
        updateProgressUi();
    }

    function findTrack(id) {
        const pools = [
            state.tracks,
            state.moodTracks,
            state.browseTracks,
            state.currentPlaylist?.tracks,
            state.currentPlaylist?.songs,
            PulsePlaylist.getQueue(),
        ];
        for (const pool of pools) {
            if (!pool) continue;
            const hit = pool.find((t) => String(t.id) === String(id));
            if (hit) return hit;
        }
        return null;
    }

    async function playTrack(track, queue) {
        if (!track) return;

        const playbackQueue = (Array.isArray(queue) && queue.length ? queue : [track]).map((t) =>
            t.previewUrl ? t : normalizeSong(t),
        );
        PulsePlaylist.setQueue(playbackQueue, playbackQueue.findIndex((item) => String(item.id) === String(track.id)));

        if (!track.previewUrl) {
            state.playingTrack = track;
            state.isPlaying = false;
            syncPlaybackUi();
            showToast('No 30-sec preview on iTunes for this track', 'error');
            return;
        }

        state.playingTrack = track;
        try {
            await PulsePlayer.playSong(track);
            state.isPlaying = true;
            syncPlaybackUi();
        } catch {
            state.isPlaying = false;
            syncPlaybackUi();
            showToast('Preview playback was blocked. Click play again.', 'error');
        }
    }

    async function togglePlay() {
        if (!state.playingTrack) return;
        try {
            await PulsePlayer.toggle();
            state.isPlaying = !PulsePlayer.audio.paused;
            syncPlaybackUi();
        } catch {
            showToast('Could not resume preview playback.', 'error');
        }
    }

    function playAdjacent(step) {
        const nextTrack = step > 0 ? PulsePlaylist.playNext() : PulsePlaylist.playPrev();
        if (nextTrack) {
            playTrack(nextTrack, PulsePlaylist.getQueue());
        }
    }

    async function loadTracks() {
        moodSuggestions.innerHTML = '<div class="spinner-wrap"><div class="spinner"></div></div>';
        try {
            const songs = await API.trending(20);
            state.tracks = songs.map(normalizeSong);
            state.genres = PRESET_GENRES;
            genreSelect.innerHTML = ['Any', ...PRESET_GENRES].map((g) => `<option value="${g}">${g}</option>`).join('');
            moodSelect.innerHTML = ['Any', ...MOODS].map((m) => `<option value="${m}">${m}</option>`).join('');
            renderBrowse();
            renderMoodButtons();
            await renderMoodSuggestions();
            computeStatsFromTracks(state.tracks);
        } catch (err) {
            showToast(err.message, 'error');
        }
    }

    function computeStatsFromTracks(tracks) {
        const genre = {};
        const duration = { '2-3min': 0, '3-4min': 0, '4-5min': 0, '5min+': 0 };
        tracks.forEach((t) => {
            genre[t.genre] = (genre[t.genre] || 0) + 1;
            const mins = Math.floor((t.duration || 0) / 60000);
            if (mins < 3) duration['2-3min'] += 1;
            else if (mins < 4) duration['3-4min'] += 1;
            else if (mins < 5) duration['4-5min'] += 1;
            else duration['5min+'] += 1;
        });
        state.stats.genre = genre;
        state.stats.duration = duration;
        state.stats.mood = state.stats.mood || {};
        renderOverviewStats({
            totalTracks: tracks.length,
            totalGenres: Object.keys(genre).length,
            totalMoods: MOODS.length,
            avgDurationLabel: tracks[0]?.durationLabel || '0:00',
        });
        initCharts();
    }

    async function loadSaved() {
        const data = await API.getSavedPlaylists();
        state.savedPlaylists = data.playlists || [];
        renderSavedShelf();
    }

    async function checkHealth() {
        try {
            const data = await API.health();
            healthDot.classList.add('live');
            connectionText.textContent = `${data.app} • Java ${data.runtime}`;
        } catch {
            healthDot.classList.remove('live');
            connectionText.textContent = 'Backend offline';
        }
    }

    generatorForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        renderGeneratedPlaylist(null, true);
        try {
            const moodLabel = moodSelect.value;
            const moodKey = moodLabel === 'Any' ? 'chill' : MOOD_PLAYLIST_MAP[moodLabel] || moodLabel.toLowerCase();
            const songs = await API.playlistByMood(moodKey, Number(limitInput.value) || 6);
            const tracks = songs.map(normalizeSong);
            const playlist = {
                id: `mix-${Date.now()}`,
                title: `${moodLabel === 'Any' ? 'Pulse' : moodLabel} Mix`,
                tracks,
                songs: tracks,
                trackCount: tracks.length,
                mood: moodLabel,
                genre: genreSelect.value,
                summary: `Live iTunes playlist • ${tracks.length} tracks`,
            };
            state.currentPlaylist = playlist;
            state.sessionPlaylists.unshift(playlist);
            renderGeneratedPlaylist(playlist, false);
            renderSessionMixes();
            updateChartsForPlaylist(playlist);
            PulsePlaylist.setQueue(tracks, 0);
            if (tracks[0]) await playTrack(tracks[0], tracks);
            setActiveView('mixes', false);
            showToast('Playlist generated from iTunes', 'success');
        } catch (err) {
            renderGeneratedPlaylist(null, false);
            showToast(err.message, 'error');
        }
    });

    saveButton.addEventListener('click', async () => {
        if (!state.currentPlaylist) {
            showToast('Generate a playlist first', 'error');
            return;
        }
        try {
            await API.savePlaylist({ id: state.currentPlaylist.id });
            await loadSaved();
            showToast('Playlist saved to your shelf ✓', 'success');
            setActiveView('saved', true);
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    savedList.addEventListener('click', async (e) => {
        const del = e.target.closest('[data-delete-id]');
        if (del) {
            e.stopPropagation();
            try {
                await API.deletePlaylist(del.dataset.deleteId);
                await loadSaved();
                showToast('Playlist deleted', 'info');
            } catch (err) {
                showToast(err.message, 'error');
            }
            return;
        }
        const card = e.target.closest('[data-playlist-id]');
        if (!card) return;
        const playlist = state.savedPlaylists.find((p) => p.id === card.dataset.playlistId);
        if (!playlist) return;
        state.currentPlaylist = playlist;
        renderGeneratedPlaylist(playlist, false);
        updateChartsForPlaylist(playlist);
        setActiveView('mixes', true);
        showToast('Loaded saved playlist', 'info');
    });

    sessionMixesList.addEventListener('click', (e) => {
        const card = e.target.closest('[data-playlist-id]');
        if (!card) return;
        const playlist = state.sessionPlaylists.find((p) => p.id === card.dataset.playlistId);
        if (!playlist) return;
        state.currentPlaylist = playlist;
        renderGeneratedPlaylist(playlist, false);
        updateChartsForPlaylist(playlist);
        setActiveView('mixes', true);
    });

    document.body.addEventListener('click', (e) => {
        const genreBtn = e.target.closest('[data-genre]');
        if (genreBtn && genreBtn.classList.contains('genre-link')) {
            loadBrowseGenre(genreBtn.dataset.genre);
            return;
        }

        const playBtn = e.target.closest('[data-play-id]');
        if (!playBtn) return;
        e.stopPropagation();
        const track = findTrack(playBtn.dataset.playId);
        if (!track) return;
        const queue = PulsePlaylist.getQueue().length ? PulsePlaylist.getQueue() : state.tracks;
        playTrack(track, queue);
    });

    function bindNav(container) {
        if (!container) {
            return;
        }
        container.addEventListener('click', (e) => {
            const btn = e.target.closest('[data-view]');
            if (!btn) return;
            setActiveView(btn.dataset.view, true);
        });
    }
    bindNav(sidebarNav);
    bindNav(mobileTabs);

    if (playPauseButton) {
        playPauseButton.addEventListener('click', () => {
            togglePlay();
        });
    }
    if (prevButton) {
        prevButton.addEventListener('click', () => playAdjacent(-1));
    }
    if (nextButton) {
        nextButton.addEventListener('click', () => playAdjacent(1));
    }

    moodOptions.addEventListener('click', async (e) => {
        const btn = e.target.closest('.mood-option');
        if (!btn) return;
        await toggleMood(btn.dataset.mood);
        const circle = document.createElement('span');
        circle.className = 'ripple';
        btn.appendChild(circle);
        const rect = btn.getBoundingClientRect();
        circle.style.left = `${e.clientX - rect.left}px`;
        circle.style.top = `${e.clientY - rect.top}px`;
        setTimeout(() => circle.remove(), 600);
    });

    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        let searchTimer;
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimer);
            const value = e.target.value.trim();
            if (value.length < 2) return;
            searchTimer = setTimeout(async () => {
                moodLoading.classList.remove('hidden');
                try {
                    const songs = await API.search(value, 20);
                    state.moodTracks = songs.map(normalizeSong);
                    moodSuggestions.innerHTML = state.moodTracks.map((t, i) => trackCardHtml(t, i)).join('');
                    moodHint.textContent = `Search results for "${value}"`;
                } catch (err) {
                    showToast(err.message, 'error');
                } finally {
                    moodLoading.classList.add('hidden');
                }
            }, 400);
        });
    }

    PulsePlayer.setOnEnded(() => {
        const next = PulsePlaylist.playNext();
        if (next) {
            playTrack(next, PulsePlaylist.getQueue());
            return;
        }
        state.isPlaying = false;
        syncPlaybackUi();
    });

    if (volumeSlider) {
        PulsePlayer.setVolume(volumeSlider.value || 0.85);
        volumeSlider.addEventListener('input', () => PulsePlayer.setVolume(volumeSlider.value));
    }

    const progressWrap = document.querySelector('.progress-wrap');
    if (progressWrap) {
        progressWrap.addEventListener('click', (e) => {
            const rect = progressWrap.getBoundingClientRect();
            PulsePlayer.seekByPercent((e.clientX - rect.left) / rect.width);
        });
    }

    Promise.all([checkHealth(), loadTracks(), loadSaved(), loadStats()]).catch((err) =>
        showToast(err.message, 'error'),
    );

    themeToggle.addEventListener('click', () => {
        document.body.classList.toggle('dark-mode');
        const dark = document.body.classList.contains('dark-mode');
        themeToggle.textContent = dark ? '☀️' : '🌙';
        localStorage.setItem('pulse-theme', dark ? 'dark' : 'light');
    });

    if (localStorage.getItem('pulse-theme') === 'dark') {
        document.body.classList.add('dark-mode');
        themeToggle.textContent = '☀️';
    }

    const chartObserver = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting && !state.charts.mood && window.Chart) {
                    initCharts();
                }
            });
        },
        { threshold: 0.2 },
    );
    const statsSection = document.getElementById('statsSection');
    if (statsSection) {
        chartObserver.observe(statsSection);
    }
})();
