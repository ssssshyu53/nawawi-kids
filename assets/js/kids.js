/**
 * Kids Interactive Portal Controller — Al-Arba'oon Al-Nawawiyyah
 * التحكم التفاعلي ببوابة الأربعين النووية للأطفال
 */
(function () {
    'use strict';

    const STORAGE_KEY = 'algohari_nawawi_kids_v1';
    const AR_DIGITS = ['٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'];

    function toArDigits(val) {
        return String(val).replace(/\d/g, d => AR_DIGITS[Number(d)]);
    }

    function fmtClock(sec) {
        if (!Number.isFinite(sec) || sec < 0) sec = 0;
        const m = Math.floor(sec / 60);
        const s = Math.floor(sec % 60);
        return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');
    }

    const DEFAULT_STATE = {
        completedIds: [],
        unlockedUpTo: 1,
        currentId: 1,
        teacherMode: false,
        sfxMuted: false,
        fontSize: 'md',
        studentName: (window.NAWAWI_KIDS_CONFIG && window.NAWAWI_KIDS_CONFIG.defaultName) || ''
    };

    let state = loadState();
    let dataset = null;
    let hadithsById = {};
    let activeTab = 'listen';

    // Audio playback state
    let rafId = null;
    let activeWordIdx = -1;
    let pendingSeekTime = null;
    let speedOptions = [1.0, 0.75, 1.15];
    let speedIdx = 0;
    let loopMode = 0; // 0 = off, 3 = 3x, 999 = infinite
    let loopRemaining = 0;

    // Memorization state
    let memHidePct = 0;
    let memSeed = 1;
    let memHiddenSet = new Set();
    let memPeekedSet = new Set();

    // Voice recorder state
    let mediaRecorder = null;
    let recStream = null;
    let recChunks = [];
    let recBlobUrl = null;
    let recTimerInterval = null;
    let recSeconds = 0;

    // Quiz state for active hadith
    let quizCorrectSet = new Set();

    // Shared AudioContext for cheerful synthesized WebAudio sound effects
    let sfxCtx = null;

    function loadState() {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            if (!raw) return { ...DEFAULT_STATE };
            const parsed = JSON.parse(raw);
            return {
                ...DEFAULT_STATE,
                ...parsed,
                completedIds: Array.isArray(parsed.completedIds) ? parsed.completedIds : [],
                unlockedUpTo: Math.min(42, Math.max(1, Number(parsed.unlockedUpTo) || 1)),
                currentId: Math.min(42, Math.max(1, Number(parsed.currentId) || 1)),
                teacherMode: Boolean(parsed.teacherMode),
                sfxMuted: Boolean(parsed.sfxMuted),
                fontSize: ['sm', 'md', 'lg'].includes(parsed.fontSize) ? parsed.fontSize : 'md'
            };
        } catch (e) {
            return { ...DEFAULT_STATE };
        }
    }

    function saveState() {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
        } catch (e) {
            // Ignore storage quota errors
        }
    }

    function playSfx(kind) {
        if (state.sfxMuted) return;
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (!AudioCtx) return;
        try {
            if (!sfxCtx) sfxCtx = new AudioCtx();
            if (sfxCtx.state === 'suspended') sfxCtx.resume();
            const now = sfxCtx.currentTime;

            const playTone = (freq, startOffset, dur, vol, type) => {
                const osc = sfxCtx.createOscillator();
                const gain = sfxCtx.createGain();
                osc.type = type || 'sine';
                osc.frequency.setValueAtTime(freq, now + startOffset);
                gain.gain.setValueAtTime(0.0001, now + startOffset);
                gain.gain.exponentialRampToValueAtTime(vol, now + startOffset + 0.02);
                gain.gain.exponentialRampToValueAtTime(0.0001, now + startOffset + dur);
                osc.connect(gain);
                gain.connect(sfxCtx.destination);
                osc.start(now + startOffset);
                osc.stop(now + startOffset + dur + 0.02);
            };

            if (kind === 'pop') {
                playTone(580, 0, 0.09, 0.08, 'triangle');
            } else if (kind === 'correct') {
                playTone(523.25, 0, 0.14, 0.11, 'triangle');
                playTone(659.25, 0.09, 0.18, 0.11, 'triangle');
            } else if (kind === 'wrong') {
                playTone(260, 0, 0.16, 0.08, 'sine');
                playTone(215, 0.10, 0.18, 0.07, 'sine');
            } else if (kind === 'unlock') {
                playTone(523.25, 0, 0.14, 0.12, 'triangle');
                playTone(659.25, 0.09, 0.14, 0.12, 'triangle');
                playTone(783.99, 0.18, 0.16, 0.12, 'triangle');
                playTone(1046.5, 0.27, 0.35, 0.14, 'triangle');
            }
        } catch (e) {
            // Audio FX optional
        }
    }

    // DOM elements helper
    const $ = id => document.getElementById(id);

    // Stage illustrated scene artwork for Hadiths 14-42 (zero emojis!)
    function getStageSceneHtml(stageId) {
        const meta = {
            1: { title: 'عالم الإخلاص وأركان الإسلام', bg: 'bg-[#ccfbf1] text-[#0f766e] border-[#5eead4]' },
            2: { title: 'حديقة الأخلاق والرحمة', bg: 'bg-[#ffe4e6] text-[#9f1239] border-[#fda4af]' },
            3: { title: 'واحة الاستقامة وأبواب الخير', bg: 'bg-[#fef3c7] text-[#92400e] border-[#fcd34d]' },
            4: { title: 'سماء الآداب وسعة رحمة الله', bg: 'bg-[#e0f2fe] text-[#075985] border-[#7dd3fc]' }
        }[stageId] || { title: 'عالم الإخلاص وأركان الإسلام', bg: 'bg-[#ccfbf1] text-[#0f766e] border-[#5eead4]' };
        const safeStage = (stageId >= 1 && stageId <= 4) ? stageId : 1;
        return `
            <img src="assets/img/scene_${safeStage}.webp" alt="${meta.title}" class="w-full h-auto block rounded-t-xl">
            <div class="p-2.5 text-center border-t-2 ${meta.bg}">
                <p class="text-xs font-black leading-snug">${meta.title}</p>
            </div>`;
    }

    function escapeHtml(str) {
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function stripTashkeel(str) {
        return String(str).replace(/[\u064B-\u065F\u0670\u0640]/g, '');
    }

    function isUnlocked(id) {
        if (state.teacherMode) return true;
        return id <= state.unlockedUpTo || state.completedIds.includes(id);
    }

    function updateProgressUI() {
        const doneCount = state.completedIds.length;
        const pct = Math.round((doneCount / 42) * 100);
        $('nkHeaderDoneCount').textContent = toArDigits(doneCount);
        $('nkProgressFill').style.width = pct + '%';
        $('nkProgressPct').textContent = pct + '%';
        $('nkCertModalDoneCount').textContent = toArDigits(doneCount);
    }

    // ================= STORY JOURNEY MAP =================
    const CHAPTERS = {
        1: {
            ord: 'الفصل الأول', name: 'عالم الإخلاص', icon: 'fa-mosque',
            story: 'تبدأ حكايتنا هنا: نتعلم أركان ديننا الجميل، وأن نجعل كل أعمالنا خالصةً لله.',
            main: '#14b8a6', dark: '#0f766e', light: '#5eead4', ink: '#0f766e', bg: '#f0fdfa', border: '#99f6e4', shadow: '#5eead4', ring: 'rgba(20,184,166,0.35)'
        },
        2: {
            ord: 'الفصل الثاني', name: 'حديقة الأخلاق', icon: 'fa-seedling',
            story: 'ندخل الحديقة لنزرع الرحمة والصدق وحب الخير لكل الناس.',
            main: '#f43f5e', dark: '#be123c', light: '#fda4af', ink: '#9f1239', bg: '#fff1f2', border: '#fecdd3', shadow: '#fda4af', ring: 'rgba(244,63,94,0.3)'
        },
        3: {
            ord: 'الفصل الثالث', name: 'واحة الطاعات', icon: 'fa-sun',
            story: 'في الواحة نتعلم الاستقامة، ونكتشف أبواب الخير الكثيرة المفتوحة لنا.',
            main: '#f59e0b', dark: '#b45309', light: '#fcd34d', ink: '#92400e', bg: '#fffbeb', border: '#fde68a', shadow: '#fcd34d', ring: 'rgba(245,158,11,0.35)'
        },
        4: {
            ord: 'الفصل الرابع', name: 'سماء الآداب', icon: 'fa-moon',
            story: 'نطير إلى السماء الأخيرة لنتزيّن بآداب المسلم ونفرح بسعة رحمة الله.',
            main: '#0ea5e9', dark: '#0369a1', light: '#7dd3fc', ink: '#075985', bg: '#f0f9ff', border: '#bae6fd', shadow: '#7dd3fc', ring: 'rgba(14,165,233,0.35)'
        }
    };

    let lastEarnedId = null;
    let visitedSteps = new Set();

    function heroName() {
        const n = String(state.studentName || '').trim();
        return n.length >= 2 ? n : 'بطل';
    }

    function getFrontierId() {
        for (let i = 1; i <= 42; i++) {
            if (!state.completedIds.includes(i) && isUnlocked(i)) return i;
        }
        return state.completedIds.length >= 42 ? 42 : state.currentId;
    }

    function chapterVars(ch) {
        return `--ch-main:${ch.main};--ch-dark:${ch.dark};--ch-light:${ch.light};--ch-ink:${ch.ink};--ch-bg:${ch.bg};--ch-border:${ch.border};--ch-shadow:${ch.shadow};--ch-ring:${ch.ring};`;
    }

    function isJourneyVisible() {
        const v = $('nkJourneyView');
        return v && !v.classList.contains('hidden');
    }

    function renderJourneyHeader() {
        const done = state.completedIds.length;
        const frontier = getFrontierId();
        const nm = heroName();
        $('nkHeroGreeting').textContent = nm === 'بطل' ? 'أهلاً بك يا بطل!' : `أهلاً بك يا ${nm}!`;

        let label;
        if (done >= 42) {
            label = 'افتح صندوق الكنز الآن';
        } else if (done === 0 && frontier === 1) {
            label = 'ابدأ الرحلة من المحطة الأولى';
        } else {
            label = `تابع الرحلة: المحطة ${toArDigits(frontier)}`;
        }
        $('nkContinueLabel').textContent = label;

        const left = 42 - done;
        $('nkTreasureText').textContent = left <= 0
            ? `مبارك يا ${nm}! جمعت النجوم الـ ٤٢ كلها وانفتح صندوق الكنز. اضغط لتأخذ شهادتك!`
            : `بقي لك ${toArDigits(left)} نجمة لتفتح الصندوق وتنال شهادة البطل باسمك!`;

        $('nkChapterChips').innerHTML = [1, 2, 3, 4].map(cid => {
            const ch = CHAPTERS[cid];
            const ids = dataset.hadiths.filter(h => h.stage === cid).map(h => h.id);
            const got = ids.filter(id => state.completedIds.includes(id)).length;
            return `
                <button type="button" class="nk-chapter-chip rounded-2xl px-3 py-2.5 flex items-center gap-2.5 text-start" data-chapter="${cid}"
                    style="background:${ch.bg};border-color:${ch.border};border-bottom-color:${ch.main};color:${ch.ink};">
                    <span class="w-9 h-9 rounded-xl flex items-center justify-center text-white flex-shrink-0" style="background:${ch.main};"><i class="fas ${ch.icon}"></i></span>
                    <span class="min-w-0">
                        <span class="block text-[11px] font-black opacity-80">${ch.ord}</span>
                        <span class="block text-xs sm:text-sm font-black truncate">${ch.name}</span>
                        <span class="block text-[11px] font-black"><i class="fas fa-star text-[#f59e0b]"></i> ${toArDigits(got)} / ${toArDigits(ids.length)}</span>
                    </span>
                </button>`;
        }).join('');
        $('nkChapterChips').querySelectorAll('[data-chapter]').forEach(btn => {
            btn.addEventListener('click', () => {
                const el = $('nkChapter-' + btn.getAttribute('data-chapter'));
                if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
            });
        });
    }

    function renderMap() {
        if (!dataset || !isJourneyVisible()) return;
        renderJourneyHeader();

        const pathEl = $('nkJourneyPath');
        const frontier = getFrontierId();
        const avail = Math.max(264, pathEl.clientWidth - 20);
        const W = Math.min(620, avail);
        const amp = Math.max(52, Math.min(165, (W - 144) / 2));
        const wave = [0, 0.65, 1, 0.65, 0, -0.65, -1, -0.65];
        const GAP = 168;
        const TOP = 90;
        const decoImgs = ['assets/img/boy_guide.png', 'assets/img/book_guide.png', 'assets/img/girl_guide.png'];

        pathEl.innerHTML = [1, 2, 3, 4].map(cid => {
            const ch = CHAPTERS[cid];
            const items = dataset.hadiths.filter(h => h.stage === cid);
            const got = items.filter(h => state.completedIds.includes(h.id)).length;
            const pct = Math.round((got / items.length) * 100);
            const chapterOpen = isUnlocked(items[0].id);
            const H = TOP + (items.length - 1) * GAP + 110;

            const pts = items.map((h, i) => ({ h, x: W / 2 + wave[(h.id - 1) % wave.length] * amp, y: TOP + i * GAP }));

            // Double-layered winding storybook road (outlined cobblestone bed + dashed trail / vibrant completed track)
            const roadPaths = pts.slice(0, -1).map((p, i) => {
                const q = pts[i + 1];
                return {
                    d: `M${p.x.toFixed(1)} ${p.y} C${p.x.toFixed(1)} ${p.y + GAP / 2}, ${q.x.toFixed(1)} ${q.y - GAP / 2}, ${q.x.toFixed(1)} ${q.y}`,
                    done: state.completedIds.includes(p.h.id)
                };
            });
            const roadOutline = roadPaths.map(r => `<path class="nk-road-outline" d="${r.d}"/>`).join('');
            const roadBed = roadPaths.map(r => `<path class="nk-road-bed" d="${r.d}"/>`).join('');
            const roadTop = roadPaths.map(r => r.done
                ? `<path class="nk-road-done" d="${r.d}"/><path class="nk-road-done-shine" d="${r.d}"/>`
                : `<path class="nk-road-dash" d="${r.d}"/>`
            ).join('');

            let decos = '';
            if (W >= 420) {
                pts.forEach((p, i) => {
                    const f = wave[(p.h.id - 1) % wave.length];
                    if (Math.abs(f) === 1 && i % 2 === 0) {
                        const dx = W / 2 - f * amp * 0.82 - 34;
                        decos += `<img class="nk-track-deco nk-float-slow" src="${decoImgs[(p.h.id + cid) % decoImgs.length]}" alt="" style="left:${dx.toFixed(1)}px;top:${p.y - 34}px;">`;
                    }
                });
            }

            const nodes = pts.map(p => {
                const h = p.h;
                const done = state.completedIds.includes(h.id);
                const unlocked = isUnlocked(h.id);
                const isFrontier = h.id === frontier && !done;
                let cls = 'is-open';
                let inner = toArDigits(h.id);
                if (!unlocked) { cls = 'is-locked'; inner = '<i class="fas fa-lock text-lg"></i>'; }
                else if (isFrontier) { cls = 'is-current'; }
                else if (done) { cls = 'is-done'; inner = '<i class="fas fa-star text-2xl"></i><span class="nk-node-star"><i class="fas fa-check"></i></span>'; }
                if (lastEarnedId === h.id) cls += ' is-just-earned';
                const half = isFrontier ? 40 : 34;
                const bubble = isFrontier ? `<span class="nk-here-bubble">${state.completedIds.length === 0 ? 'ابدأ من هنا!' : 'محطتك التالية!'}</span>` : '';
                return `
                    <div class="nk-node-wrap" style="left:${(p.x - 65).toFixed(1)}px;top:${p.y - half}px;">
                        ${bubble}
                        <button type="button" class="nk-node ${cls}" data-hid="${h.id}" ${!unlocked ? 'disabled' : ''}
                            aria-label="المحطة ${toArDigits(h.id)}: ${escapeHtml(h.title)}${!unlocked ? ' (مغلقة)' : ''}"
                            title="${!unlocked ? 'أكمل المحطة السابقة لتفتح هذه' : escapeHtml(h.title)}">${inner}</button>
                        <span class="nk-node-label ${!unlocked ? 'is-locked' : ''}">${escapeHtml(h.title)}</span>
                    </div>`;
            }).join('');

            return `
                <section id="nkChapter-${cid}" class="nk-chapter" style="${chapterVars(ch)}">
                    <div class="nk-chapter-head p-4 sm:p-5">
                        <div class="flex flex-col sm:flex-row items-stretch sm:items-center gap-4">
                            <div class="nk-chapter-art rounded-2xl w-full sm:w-52 md:w-60 h-44 sm:h-40 flex-shrink-0">
                                <img src="assets/img/scene_${cid}.webp" alt="${ch.name}" loading="lazy">
                            </div>
                            <div class="flex-1 min-w-0 space-y-2.5">
                                <div class="flex flex-wrap items-center justify-between gap-2">
                                    <div class="flex flex-wrap items-center gap-2">
                                        <span class="inline-flex items-center gap-1.5 text-xs font-black px-3.5 py-1 rounded-full text-white shadow-xs" style="background:${ch.main};">
                                            <i class="fas ${ch.icon}"></i>
                                            <span>${ch.ord}</span>
                                        </span>
                                        <h3 class="text-xl sm:text-2xl font-black leading-tight" style="color:${ch.ink};">${ch.name}</h3>
                                    </div>
                                    <span class="flex-shrink-0 text-xs sm:text-sm font-black bg-white px-3 py-1 rounded-full border-2 shadow-xs" style="color:${ch.ink};border-color:${ch.border};">
                                        <i class="fas fa-star text-[#f59e0b]"></i> ${toArDigits(got)} / ${toArDigits(items.length)}
                                    </span>
                                </div>
                                <div class="flex items-start gap-2.5 rounded-2xl p-3 border-2 bg-white/95" style="border-color:${ch.border};">
                                    <img src="assets/img/${cid % 2 === 1 ? 'boy' : 'girl'}_guide.png" alt="" class="w-10 h-10 rounded-full flex-shrink-0 shadow-xs">
                                    <p class="text-xs sm:text-sm font-extrabold leading-relaxed" style="color:${ch.ink};">${ch.story}${chapterOpen ? '' : ' <b class="opacity-85">(يُفتح هذا الفصل بعد إكمال الفصل السابق)</b>'}</p>
                                </div>
                                <div class="h-3.5 bg-white rounded-full overflow-hidden border-2 p-0.5" style="border-color:${ch.border};">
                                    <div class="h-full rounded-full transition-all duration-500" style="width:${pct}%;background:${ch.main};"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="nk-track" dir="ltr" style="width:${W}px;height:${H}px;">
                        <svg class="nk-track-svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}" aria-hidden="true">${roadOutline}${roadBed}${roadTop}</svg>
                        ${decos}
                        ${nodes}
                    </div>
                </section>`;
        }).join('');

        pathEl.querySelectorAll('button[data-hid]').forEach(btn => {
            btn.addEventListener('click', () => {
                const hid = Number(btn.getAttribute('data-hid'));
                if (!isUnlocked(hid)) return;
                playSfx('pop');
                selectHadith(hid);
                showStationView();
            });
        });
        lastEarnedId = null;
    }

    function showJourneyView(scrollToFrontier) {
        stopAudioPlayback();
        $('nkWorkspace').classList.add('hidden');
        $('nkJourneyView').classList.remove('hidden');
        renderMap();
        const node = document.querySelector(`#nkJourneyPath .nk-node[data-hid="${getFrontierId()}"]`) ||
            document.querySelector('#nkJourneyPath .nk-node.is-just-earned');
        if (scrollToFrontier && node) {
            node.scrollIntoView({ behavior: 'smooth', block: 'center' });
        } else {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }
    }

    function showStationView() {
        $('nkJourneyView').classList.add('hidden');
        $('nkWorkspace').classList.remove('hidden');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function stopAudioPlayback() {
        const audio = $('nkAudio');
        audio.pause();
        $('nkPlayIcon').className = 'fas fa-play';
        if (rafId) {
            cancelAnimationFrame(rafId);
            rafId = null;
        }
        $('nkHadithStarBadge').classList.remove('nk-star-active');
        clearWordHighlights();
    }

    function clearWordHighlights() {
        activeWordIdx = -1;
        $('nkMatnWordsContainer').querySelectorAll('.nk-word').forEach(el => {
            el.classList.remove('is-active', 'is-spoken');
        });
    }

    function updateSyncHighlight() {
        const audio = $('nkAudio');
        const h = hadithsById[state.currentId];
        if (!h) return;

        const t = audio.currentTime || 0;
        const dur = audio.duration || h.duration || 0;
        $('nkTimeCur').textContent = fmtClock(t);
        $('nkScrubber').max = dur > 0 ? dur : 100;
        $('nkScrubber').value = t;

        // Highlight the star medallion while Qari announces "الحديث ..."
        const hdr = h.hdr_time || [0, 1.5];
        if (!audio.paused && t >= Math.max(0, hdr[0] - 0.08) && t <= hdr[1] + 0.12) {
            $('nkHadithStarBadge').classList.add('nk-star-active');
        } else {
            $('nkHadithStarBadge').classList.remove('nk-star-active');
        }

        // Binary search active word in h.words
        const words = h.words;
        let found = -1;
        let lo = 0, hi = words.length - 1;
        while (lo <= hi) {
            const mid = (lo + hi) >> 1;
            if (t < words[mid].s) {
                hi = mid - 1;
            } else if (t > words[mid].e) {
                lo = mid + 1;
            } else {
                found = mid;
                break;
            }
        }
        // Soft hold on closest preceding word inside tiny inter-word breath gaps (<0.35s)
        if (found === -1 && hi >= 0 && t - words[hi].e < 0.35) {
            found = hi;
        }

        if (found !== activeWordIdx) {
            activeWordIdx = found;
            const spans = $('nkMatnWordsContainer').querySelectorAll('.nk-word');
            spans.forEach((el, idx) => {
                el.classList.toggle('is-active', idx === found);
                el.classList.toggle('is-spoken', found > -1 && idx < found);
            });
            if (found >= 0 && spans[found] && !audio.paused) {
                const rect = spans[found].getBoundingClientRect();
                if (rect.top < 110 || rect.bottom > window.innerHeight - 60) {
                    spans[found].scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        }

        if (!audio.paused) {
            rafId = requestAnimationFrame(updateSyncHighlight);
        }
    }

    function renderMatnWords(h) {
        const container = $('nkMatnWordsContainer');
        // Group words by paragraph index `p`
        const byPara = {};
        h.words.forEach((w, idx) => {
            if (!byPara[w.p]) byPara[w.p] = [];
            byPara[w.p].push({ ...w, idx });
        });

        const paraKeys = Object.keys(byPara).sort((a, b) => Number(a) - Number(b));
        container.innerHTML = paraKeys.map(pk => {
            const wordHtml = byPara[pk].map(w =>
                `<button type="button" class="nk-word" data-widx="${w.idx}" data-start="${w.s}">${escapeHtml(w.t)}</button>`
            ).join(' ');
            return `<p class="leading-[2.15]">${wordHtml}</p>`;
        }).join('');

        container.querySelectorAll('.nk-word').forEach(btn => {
            btn.addEventListener('click', () => {
                const s = parseFloat(btn.getAttribute('data-start'));
                const audio = $('nkAudio');
                if (!Number.isFinite(s)) return;
                if (audio.readyState >= 1) {
                    audio.currentTime = s;
                    updateSyncHighlight();
                    audio.play().catch(() => {});
                } else {
                    pendingSeekTime = s;
                    audio.play().catch(() => {});
                }
            });
        });

        applyMatnFontSize();
    }

    function applyMatnFontSize() {
        const targets = [$('nkMatnWordsContainer'), $('nkMemMatnBox')];
        targets.forEach(el => {
            el.classList.remove('nk-size-sm', 'nk-size-md', 'nk-size-lg');
            el.classList.add('nk-size-' + state.fontSize);
        });
        document.querySelectorAll('.nk-font-btn').forEach(btn => {
            const active = btn.getAttribute('data-font') === state.fontSize;
            btn.className = 'nk-font-btn px-2.5 py-0.5 rounded-lg text-[11px] font-black transition ' +
                (active ? 'bg-[#14b8a6] text-white shadow-xs' : 'text-[#78350f]');
        });
    }

    function renderLearnTab(h) {
        $('nkGeneralMeaning').textContent = h.meaning;
        $('nkSourceText').textContent = h.source;

        const ribbonStyles = [
            'bg-[#ffe4e6] border-[#fb7185] border-b-[#e11d48] text-[#881337]',
            'bg-[#fef3c7] border-[#fbbf24] border-b-[#d97706] text-[#78350f]',
            'bg-[#ccfbf1] border-[#2dd4bf] border-b-[#0d9488] text-[#115e59]',
            'bg-[#e0f2fe] border-[#38bdf8] border-b-[#0284c7] text-[#075985]'
        ];
        $('nkVocabList').innerHTML = h.ghareeb.map((g, idx) => {
            const st = ribbonStyles[idx % ribbonStyles.length];
            return `
                <div class="nk-vocab-flag rounded-2xl border-2 border-b-4 px-5 py-3 ${st} flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-2 shadow-xs">
                    <span class="font-black text-sm sm:text-base whitespace-nowrap">${escapeHtml(g.w)} :</span>
                    <span class="text-xs sm:text-sm font-extrabold leading-relaxed">${escapeHtml(g.m)}</span>
                </div>`;
        }).join('');

        const lessonColors = [
            { bg: 'bg-[#dcfce7] border-[#4ade80] border-b-[#16a34a] text-[#14532d]', badge: 'bg-[#16a34a] text-white' },
            { bg: 'bg-[#fef3c7] border-[#fbbf24] border-b-[#d97706] text-[#78350f]', badge: 'bg-[#d97706] text-white' },
            { bg: 'bg-[#ffe4e6] border-[#fb7185] border-b-[#e11d48] text-[#881337]', badge: 'bg-[#e11d48] text-white' }
        ];
        $('nkLessonsList').innerHTML = h.lessons.map((l, idx) => {
            const lc = lessonColors[idx % lessonColors.length];
            return `
                <div class="rounded-2xl border-2 border-b-4 p-3.5 ${lc.bg} flex items-center gap-3 shadow-xs">
                    <span class="w-10 h-10 rounded-full ${lc.badge} flex items-center justify-center font-black text-base flex-shrink-0 shadow-xs">${toArDigits(idx + 1)}</span>
                    <p class="font-black text-sm sm:text-base leading-snug">${escapeHtml(l)}</p>
                </div>`;
        }).join('');
    }

    function computeHiddenWords(h) {
        memHiddenSet.clear();
        memPeekedSet.clear();
        if (memHidePct <= 0) return;
        const n = h.words.length;
        if (memHidePct >= 100) {
            for (let i = 0; i < n; i++) memHiddenSet.add(i);
            return;
        }
        // Deterministic pseudo-random shuffle seeded by hadith id + memSeed
        const indices = Array.from({ length: n }, (_, i) => i);
        let seed = h.id * 997 + memSeed * 131 + memHidePct;
        const rand = () => {
            seed = (seed * 1664525 + 1013904223) % 4294967296;
            return seed / 4294967296;
        };
        for (let i = n - 1; i > 0; i--) {
            const j = Math.floor(rand() * (i + 1));
            [indices[i], indices[j]] = [indices[j], indices[i]];
        }
        const target = Math.max(1, Math.round((n * memHidePct) / 100));
        for (let i = 0; i < target; i++) {
            memHiddenSet.add(indices[i]);
        }
    }

    function renderMemMatn(h) {
        const box = $('nkMemMatnBox');
        const showFirstLetter = $('nkFirstLetterHint').checked;

        const byPara = {};
        h.words.forEach((w, idx) => {
            if (!byPara[w.p]) byPara[w.p] = [];
            byPara[w.p].push({ ...w, idx });
        });

        const paraKeys = Object.keys(byPara).sort((a, b) => Number(a) - Number(b));
        box.innerHTML = paraKeys.map(pk => {
            const row = byPara[pk].map(w => {
                const isMasked = memHiddenSet.has(w.idx);
                const isPeeked = memPeekedSet.has(w.idx);
                if (!isMasked) {
                    return `<span class="nk-mem-word">${escapeHtml(w.t)}</span>`;
                }
                if (isPeeked) {
                    return `<button type="button" class="nk-mem-word is-peeked" data-midx="${w.idx}" title="اضغط لإخفائها مجدداً">${escapeHtml(w.t)}</button>`;
                }
                let hintTxt = '.....';
                if (showFirstLetter) {
                    const bare = stripTashkeel(w.t).replace(/[«»﴿﴾\(\)\.\،؛:!؟\-\s]/g, '');
                    hintTxt = (bare.charAt(0) || '.') + ' ...';
                }
                return `<button type="button" class="nk-mem-word is-hidden" data-midx="${w.idx}" title="اضغط لكشف الكلمة">${escapeHtml(hintTxt)}</button>`;
            }).join(' ');
            return `<p class="leading-[2.2]">${row}</p>`;
        }).join('');

        box.querySelectorAll('button[data-midx]').forEach(btn => {
            btn.addEventListener('click', () => {
                const idx = Number(btn.getAttribute('data-midx'));
                if (memPeekedSet.has(idx)) {
                    memPeekedSet.delete(idx);
                } else {
                    memPeekedSet.add(idx);
                    playSfx('pop');
                }
                renderMemMatn(h);
            });
        });

        const totalHidden = memHiddenSet.size;
        const peeked = memPeekedSet.size;
        if (totalHidden === 0) {
            $('nkMemRevealedCount').textContent = 'النص ظاهر بالكامل للقراءة والمراجعة';
        } else {
            $('nkMemRevealedCount').textContent = `مخفي ${toArDigits(totalHidden - peeked)} كلمة — اضغط على أي مربع مخفي للتحقق`;
        }
        applyMatnFontSize();
    }

    function renderQuizTab(h) {
        quizCorrectSet.clear();
        const isAlreadyDone = state.completedIds.includes(h.id);
        const container = $('nkExercisesContainer');
        const letters = ['أ', 'ب', 'ج', 'د'];
        const qCardBorders = [
            'border-[#7dd3fc] bg-[#f0f9ff]/60',
            'border-[#fcd34d] bg-[#fffbeb]/80',
            'border-[#c4b5fd] bg-[#f5f3ff]/70'
        ];

        container.innerHTML = h.exercises.map((ex, qIdx) => {
            const optsHtml = ex.options.map((opt, oIdx) => `
                <button type="button" class="nk-opt-btn w-full text-start px-4 py-3.5 rounded-2xl text-xs sm:text-sm font-black text-[#2b1d12] flex items-center gap-3" data-qidx="${qIdx}" data-oidx="${oIdx}">
                    <span class="w-8 h-8 rounded-full bg-[#fef08a] border-2 border-[#eab308] text-[#713f12] font-black text-sm flex items-center justify-center flex-shrink-0 shadow-xs">${letters[oIdx] || toArDigits(oIdx + 1)}</span>
                    <span class="leading-relaxed">${escapeHtml(opt)}</span>
                </button>`).join('');

            return `
                <div class="rounded-3xl border-3 p-4 sm:p-5 space-y-3 shadow-xs ${qCardBorders[qIdx % qCardBorders.length]}" data-qblock="${qIdx}">
                    <div class="flex flex-wrap items-center justify-between gap-2">
                        <span class="px-3.5 py-1 rounded-full text-xs font-black bg-white text-[#0369a1] border-2 border-[#38bdf8] shadow-xs">التدريب ${toArDigits(qIdx + 1)}: ${escapeHtml(ex.badge)}</span>
                        <span class="nk-q-status text-xs font-black text-[#92400e] bg-[#fef3c7] px-3 py-1 rounded-full border border-[#fcd34d]">اختر الإجابة</span>
                    </div>
                    <p class="font-black text-sm sm:text-lg text-[#2b1d12] leading-relaxed">${escapeHtml(ex.q)}</p>
                    <div class="space-y-2.5">${optsHtml}</div>
                    <div class="nk-q-feedback hidden rounded-2xl px-4 py-3 text-xs sm:text-sm font-black"></div>
                </div>`;
        }).join('');

        $('nkQuizScoreBadge').textContent = isAlreadyDone ? '٣ / ٣ صحيح (نجمة مكتسبة)' : '٠ / ٣ صحيح';
        updateUnlockBanner(h, isAlreadyDone);

        container.querySelectorAll('.nk-opt-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const qIdx = Number(btn.getAttribute('data-qidx'));
                const oIdx = Number(btn.getAttribute('data-oidx'));
                const ex = h.exercises[qIdx];
                const block = container.querySelector(`[data-qblock="${qIdx}"]`);
                const statusEl = block.querySelector('.nk-q-status');
                const fbEl = block.querySelector('.nk-q-feedback');

                if (quizCorrectSet.has(qIdx)) return;

                if (oIdx === ex.correct) {
                    quizCorrectSet.add(qIdx);
                    playSfx('correct');
                    btn.classList.add('is-correct');
                    block.querySelectorAll('.nk-opt-btn').forEach(b => { b.disabled = true; });
                    statusEl.innerHTML = '<span class="text-emerald-700"><i class="fas fa-check-circle ml-1"></i> أحسنت!</span>';
                    fbEl.className = 'nk-q-feedback rounded-2xl px-4 py-3 text-xs sm:text-sm font-black bg-emerald-100 border-2 border-emerald-400 text-emerald-900';
                    fbEl.textContent = 'ممتاز يا بطل! ' + ex.feedback;

                    $('nkQuizScoreBadge').textContent = `${toArDigits(quizCorrectSet.size)} / ٣ صحيح`;

                    if (quizCorrectSet.size === h.exercises.length) {
                        markHadithCompleted(h.id);
                    }
                } else {
                    playSfx('wrong');
                    btn.classList.add('is-wrong');
                    btn.disabled = true;
                    statusEl.innerHTML = '<span class="text-rose-700">حاول اختياراً آخر</span>';
                    fbEl.className = 'nk-q-feedback rounded-2xl px-4 py-3 text-xs sm:text-sm font-black bg-amber-100 border-2 border-amber-400 text-amber-950';
                    fbEl.textContent = 'قريبة! راجع «الشرح والكلمات» أو «استمع واقرأ» ثم جرّب إجابة أخرى.';
                }
            });
        });
    }

    function markHadithCompleted(hid) {
        const wasNew = !state.completedIds.includes(hid);
        if (wasNew) {
            state.completedIds.push(hid);
            lastEarnedId = hid;
        }
        const nextUnlock = Math.min(42, hid + 1);
        if (nextUnlock > state.unlockedUpTo) {
            state.unlockedUpTo = nextUnlock;
        }
        saveState();
        updateProgressUI();
        renderMap();
        $('nkCompletedBadge').classList.remove('hidden');
        document.querySelectorAll('.nk-tab-btn').forEach(b => b.classList.add('is-done'));

        playSfx('unlock');
        updateUnlockBanner(hadithsById[hid], true);
        updateNavArrows(hid);
    }

    function updateUnlockBanner(h, show) {
        const banner = $('nkUnlockBanner');
        const nextBtn = $('nkUnlockNextBtn');
        const certBtn = $('nkUnlockCertBtn');
        if (!show) {
            banner.classList.add('hidden');
            return;
        }
        banner.classList.remove('hidden');
        const allComplete = state.completedIds.length >= 42;
        if (h.id < 42) {
            $('nkUnlockHeading').textContent = `أحسنت يا ${heroName()}! ربحت نجمة هذه المحطة`;
            $('nkUnlockSub').textContent = `انفتحت المحطة التالية في الحكاية: ${hadithsById[h.id + 1].number_ar} «${hadithsById[h.id + 1].title}»`;
            nextBtn.classList.remove('hidden');
        } else {
            $('nkUnlockHeading').textContent = `مبارك يا ${heroName()}! وصلت إلى آخر محطة في الحكاية`;
            $('nkUnlockSub').textContent = allComplete
                ? 'لقد أتممت جميع الأحاديث الـ ٤٢! يمكنك الآن كتابة اسمك وإصدار شهادة التقدير.'
                : 'بعض الأحاديث السابقة لم تُنجز تدريباتها بعد — أكملها من الخريطة لإصدار الشهادة.';
            nextBtn.classList.add('hidden');
        }
        certBtn.classList.toggle('hidden', !allComplete);
    }

    function updateNavArrows(id) {
        $('nkPrevHadithBtn').disabled = id <= 1 || !isUnlocked(id - 1);
        $('nkNextHadithBtn').disabled = id >= 42 || !isUnlocked(id + 1);
    }

    function switchTab(tabName) {
        activeTab = tabName;
        const map = {
            listen: 'nkTabListen',
            learn: 'nkTabLearn',
            memorize: 'nkTabMemorize',
            quiz: 'nkTabQuiz'
        };
        Object.keys(map).forEach(k => {
            $(map[k]).classList.toggle('hidden', k !== tabName);
        });

        const isDoneHadith = state.completedIds.includes(state.currentId);
        visitedSteps.add(tabName);
        document.querySelectorAll('.nk-tab-btn').forEach(btn => {
            const t = btn.getAttribute('data-tab');
            btn.classList.toggle('is-active', t === tabName);
            btn.classList.toggle('is-done', isDoneHadith || visitedSteps.has(t));
            btn.setAttribute('aria-current', t === tabName ? 'step' : 'false');
        });

        // Update companion avatar & speech bubble to match the current story step
        const nm = heroName();
        const companionData = {
            listen: {
                img: 'assets/img/boy_guide.png',
                msg: `الخطوة الأولى يا ${nm}: هيا نستمع للحديث معاً! اضغط على أي كلمة في السحابة البيضاء لتسمع نطقها.`
            },
            learn: {
                img: 'assets/img/girl_guide.png',
                msg: `الخطوة الثانية: تعال يا ${nm} نكتشف معاني الكلمات الصعبة والدروس الجميلة في هذا الحديث.`
            },
            memorize: {
                img: 'assets/img/boy_guide.png',
                msg: `الخطوة الثالثة: وقت التحدّي يا ${nm}! أخفِ بعض الكلمات واقرأ من حفظك أو سجّل صوتك الرائع.`
            },
            quiz: {
                img: 'assets/img/girl_guide.png',
                msg: `الخطوة الأخيرة: أجب على ٣ أسئلة مسلّية يا ${nm} لتربح النجمة الذهبية وتنفتح المحطة التالية!`
            }
        };
        const c = companionData[tabName] || companionData.listen;
        if ($('nkCompanionAvatar')) $('nkCompanionAvatar').src = c.img;
        if ($('nkCompanionSpeech')) $('nkCompanionSpeech').textContent = c.msg;
    }

    function selectHadith(id) {
        const h = hadithsById[id];
        if (!h) return;
        stopAudioPlayback();
        cleanupRecorder(true);

        state.currentId = id;
        saveState();

        // Update header badges
        $('nkStarNumber').textContent = toArDigits(h.id);
        $('nkHadithHeaderBadge').textContent = h.header;
        $('nkHadithTitle').textContent = h.title;
        const stg = dataset.meta.stages.find(s => s.id === h.stage);
        $('nkStageBadge').textContent = stg ? stg.title : '';
        const chp = CHAPTERS[h.stage];
        $('nkStationCounter').textContent = `${chp ? chp.ord + ': ' + chp.name + ' — ' : ''}المحطة ${toArDigits(h.id)} من ٤٢`;
        visitedSteps = new Set();
        $('nkCompletedBadge').classList.toggle('hidden', !state.completedIds.includes(h.id));
        $('nkSideSummary').textContent = h.meaning;

        // Visual Companion / Poster
        if (h.poster) {
            $('nkOpenPosterBtn').classList.remove('hidden');
            $('nkPosterImg').src = h.poster;
            $('nkPosterImg').classList.remove('hidden');
            $('nkPosterZoomHint').classList.remove('hidden');
            $('nkFallbackArtBox').classList.add('hidden');
            $('nkVisualCardTitle').textContent = 'اللوحة المصورة للحديث';
            $('nkPosterModalImg').src = h.poster;
            $('nkPosterModalTitle').textContent = `${h.header} — ${h.title}`;
        } else {
            $('nkOpenPosterBtn').classList.add('hidden');
            $('nkPosterImg').classList.add('hidden');
            $('nkPosterZoomHint').classList.add('hidden');
            $('nkFallbackArtBox').innerHTML = getStageSceneHtml(h.stage);
            $('nkFallbackArtBox').classList.remove('hidden');
            $('nkVisualCardTitle').textContent = 'لوحة المرحلة المصورة';
        }

        // Setup audio track via Byte-Range audio endpoint for exact word seeking
        pendingSeekTime = null;
        const audio = $('nkAudio');
        audio.src = `audio.php?h=${h.id}`;
        audio.playbackRate = speedOptions[speedIdx];
        $('nkTimeCur').textContent = '00:00';
        $('nkTimeDur').textContent = fmtClock(h.duration);
        $('nkScrubber').max = h.duration;
        $('nkScrubber').value = 0;

        // Render all 4 tabs cleanly
        renderMatnWords(h);
        renderLearnTab(h);

        memHidePct = 0;
        updateMemLevelButtons();
        computeHiddenWords(h);
        renderMemMatn(h);

        renderQuizTab(h);
        updateNavArrows(h.id);
        renderMap();
        switchTab('listen');
    }

    function updateMemLevelButtons() {
        document.querySelectorAll('.nk-mem-lvl-btn').forEach(btn => {
            const lvl = Number(btn.getAttribute('data-level'));
            const active = lvl === memHidePct;
            btn.className = 'nk-mem-lvl-btn nk-candy-btn px-3 py-2.5 rounded-2xl text-xs font-black border-2 transition ' +
                (active ? 'bg-[#8b5cf6] text-white border-[#6d28d9] border-b-[#4c1d95] shadow-xs' : 'bg-[#f5f3ff] text-[#5b21b6] border-[#c4b5fd] border-b-[#8b5cf6]');
        });
    }

    // Voice Recorder helpers
    function cleanupRecorder(clearPlayback) {
        if (recTimerInterval) {
            clearInterval(recTimerInterval);
            recTimerInterval = null;
        }
        if (mediaRecorder && mediaRecorder.state === 'recording') {
            try { mediaRecorder.stop(); } catch (e) {}
        }
        if (recStream) {
            recStream.getTracks().forEach(t => t.stop());
            recStream = null;
        }
        $('nkRecordStartBtn').classList.remove('hidden');
        $('nkRecordStopBtn').classList.add('hidden');
        $('nkRecWaveBox').classList.add('hidden');
        if (clearPlayback) {
            $('nkRecPlaybackBox').classList.add('hidden');
            $('nkUserRecAudio').removeAttribute('src');
            if (recBlobUrl) {
                URL.revokeObjectURL(recBlobUrl);
                recBlobUrl = null;
            }
        }
    }

    async function startSelfRecording() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia || typeof MediaRecorder === 'undefined') {
            $('nkRecStatusText').textContent = 'المتصفح الحالي لا يدعم تسجيل الميكروفون؛ يمكنك استخدام أزرار إخفاء الكلمات وكشفها للتحقق من حفظك.';
            return;
        }
        try {
            recStream = await navigator.mediaDevices.getUserMedia({ audio: true });
            const mimeCandidates = ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4', 'audio/ogg'];
            const chosenMime = mimeCandidates.find(m => MediaRecorder.isTypeSupported(m)) || '';
            mediaRecorder = chosenMime ? new MediaRecorder(recStream, { mimeType: chosenMime }) : new MediaRecorder(recStream);
            recChunks = [];
            recSeconds = 0;

            mediaRecorder.ondataavailable = e => {
                if (e.data && e.data.size > 0) recChunks.push(e.data);
            };
            mediaRecorder.onstop = () => {
                if (recChunks.length > 0) {
                    if (recBlobUrl) URL.revokeObjectURL(recBlobUrl);
                    const blob = new Blob(recChunks, { type: mediaRecorder.mimeType || 'audio/webm' });
                    recBlobUrl = URL.createObjectURL(blob);
                    $('nkUserRecAudio').src = recBlobUrl;
                    $('nkRecPlaybackBox').classList.remove('hidden');

                    // Reveal full Matn automatically so child checks accuracy cleanly!
                    const h = hadithsById[state.currentId];
                    if (h) {
                        for (const idx of memHiddenSet) memPeekedSet.add(idx);
                        renderMemMatn(h);
                    }
                    $('nkRecStatusText').textContent = 'تم حفظ تسجيلك! استمع الآن وقارن تسميعك بالحديث الكامل الظاهر أعلاه.';
                }
                if (recStream) {
                    recStream.getTracks().forEach(t => t.stop());
                    recStream = null;
                }
            };

            // Hide all text automatically if user currently left it visible, for genuine recitation!
            const h = hadithsById[state.currentId];
            if (h && memHidePct === 0) {
                memHidePct = 100;
                updateMemLevelButtons();
                computeHiddenWords(h);
                renderMemMatn(h);
            }

            mediaRecorder.start();
            $('nkRecordStartBtn').classList.add('hidden');
            $('nkRecordStopBtn').classList.remove('hidden');
            $('nkRecWaveBox').classList.remove('hidden');
            $('nkRecPlaybackBox').classList.add('hidden');
            $('nkRecTimer').textContent = '00:00';
            recTimerInterval = setInterval(() => {
                recSeconds++;
                $('nkRecTimer').textContent = fmtClock(recSeconds);
                if (recSeconds >= 180) {
                    stopSelfRecording();
                }
            }, 1000);
        } catch (err) {
            $('nkRecStatusText').textContent = 'يرجى السماح بصلاحية الميكروفون لتسجيل التسميع، أو استخدم أداة إخفاء الكلمات مباشرة.';
        }
    }

    function stopSelfRecording() {
        if (recTimerInterval) {
            clearInterval(recTimerInterval);
            recTimerInterval = null;
        }
        $('nkRecordStartBtn').classList.remove('hidden');
        $('nkRecordStopBtn').classList.add('hidden');
        $('nkRecWaveBox').classList.add('hidden');
        if (mediaRecorder && mediaRecorder.state === 'recording') {
            try { mediaRecorder.stop(); } catch (e) {}
        }
    }

    // Certificate Modal
    function openCertModal() {
        const modal = $('nkCertModal');
        modal.classList.remove('hidden');
        modal.classList.add('flex');
        const allCompleted = state.completedIds.length >= 42 || state.teacherMode;
        $('nkCertLockedNotice').classList.toggle('hidden', allCompleted);
        $('nkCertUnlockedArea').classList.toggle('hidden', !allCompleted);
        $('nkCertModalDoneCount').textContent = toArDigits(state.completedIds.length);
        if (allCompleted) {
            if (state.studentName && !$('nkCertNameInput').value.trim()) {
                $('nkCertNameInput').value = state.studentName;
            }
            refreshCertificatePreview();
        }
    }

    function refreshCertificatePreview() {
        let name = $('nkCertNameInput').value.trim();
        if (name.length < 2) name = 'الطالب المتفوق';
        state.studentName = name;
        saveState();
        const now = new Date();
        const dateStr = `${now.getFullYear()}/${String(now.getMonth() + 1).padStart(2, '0')}/${String(now.getDate()).padStart(2, '0')}`;
        const url = `certificate.php?name=${encodeURIComponent(name)}&date=${encodeURIComponent(dateStr)}`;
        $('nkCertPreviewImg').src = url;
        $('nkCertDownloadLink').href = url + '&download=1';
    }

    function bindGlobalEvents() {
        // Journey navigation
        $('nkHeaderHomeBtn').addEventListener('click', () => {
            if (!isJourneyVisible()) {
                playSfx('pop');
                showJourneyView(true);
            } else {
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
        });
        $('nkOpenMapBtn').addEventListener('click', () => { playSfx('pop'); showJourneyView(true); });
        $('nkBannerMapBtn').addEventListener('click', () => { playSfx('pop'); showJourneyView(true); });
        $('nkJourneyCertBtn').addEventListener('click', openCertModal);
        $('nkContinueJourneyBtn').addEventListener('click', () => {
            playSfx('pop');
            if (state.completedIds.length >= 42) {
                openCertModal();
                return;
            }
            selectHadith(getFrontierId());
            showStationView();
        });

        const heroInput = $('nkHeroNameInput');
        heroInput.value = state.studentName || '';
        heroInput.addEventListener('input', () => {
            state.studentName = heroInput.value.trim();
            saveState();
            if (dataset) renderJourneyHeader();
        });

        let resizeTimer = null;
        let lastW = window.innerWidth;
        window.addEventListener('resize', () => {
            if (window.innerWidth === lastW) return;
            lastW = window.innerWidth;
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(renderMap, 180);
        });

        const teacherChk = $('nkTeacherModeToggle');
        teacherChk.checked = state.teacherMode;
        teacherChk.addEventListener('change', () => {
            state.teacherMode = teacherChk.checked;
            saveState();
            renderMap();
            updateNavArrows(state.currentId);
        });

        $('nkResetProgressBtn').addEventListener('click', () => {
            if (!window.confirm('هل تريد إعادة بدء رحلة الحفظ والتدريبات من الحديث الأول؟')) return;
            state.completedIds = [];
            state.unlockedUpTo = 1;
            state.currentId = 1;
            saveState();
            updateProgressUI();
            selectHadith(1);
            showJourneyView(false);
        });

        $('nkSfxToggleBtn').addEventListener('click', () => {
            state.sfxMuted = !state.sfxMuted;
            saveState();
            $('nkSfxIcon').className = state.sfxMuted ? 'fas fa-volume-mute text-sm text-rose-600' : 'fas fa-volume-up text-sm';
        });
        $('nkSfxIcon').className = state.sfxMuted ? 'fas fa-volume-mute text-sm text-rose-600' : 'fas fa-volume-up text-sm';

        document.querySelectorAll('.nk-tab-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                playSfx('pop');
                switchTab(btn.getAttribute('data-tab'));
            });
        });
        document.querySelectorAll('.nk-goto-tab').forEach(btn => {
            btn.addEventListener('click', () => {
                playSfx('pop');
                switchTab(btn.getAttribute('data-target'));
                window.scrollTo({ top: 0, behavior: 'smooth' });
            });
        });

        $('nkPrevHadithBtn').addEventListener('click', () => {
            if (state.currentId > 1 && isUnlocked(state.currentId - 1)) selectHadith(state.currentId - 1);
        });
        $('nkNextHadithBtn').addEventListener('click', () => {
            if (state.currentId < 42 && isUnlocked(state.currentId + 1)) selectHadith(state.currentId + 1);
        });
        $('nkUnlockNextBtn').addEventListener('click', () => {
            if (state.currentId < 42 && isUnlocked(state.currentId + 1)) {
                selectHadith(state.currentId + 1);
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
        });
        $('nkUnlockCertBtn').addEventListener('click', openCertModal);

        // Audio player bindings
        const audio = $('nkAudio');
        $('nkPlayBtn').addEventListener('click', () => {
            if (audio.paused) {
                audio.play().catch(() => {});
            } else {
                audio.pause();
            }
        });
        audio.addEventListener('play', () => {
            $('nkPlayIcon').className = 'fas fa-pause';
            rafId = requestAnimationFrame(updateSyncHighlight);
        });
        audio.addEventListener('pause', () => {
            $('nkPlayIcon').className = 'fas fa-play';
            if (rafId) { cancelAnimationFrame(rafId); rafId = null; }
            $('nkHadithStarBadge').classList.remove('nk-star-active');
        });
        audio.addEventListener('ended', () => {
            if (loopMode === 999 || (loopMode === 3 && loopRemaining > 1)) {
                if (loopMode === 3) {
                    loopRemaining--;
                    $('nkLoopLabel').textContent = `تكرار: ${toArDigits(loopRemaining)}`;
                }
                audio.currentTime = 0;
                audio.play().catch(() => {});
            } else {
                if (loopMode === 3) {
                    loopMode = 0;
                    $('nkLoopLabel').textContent = 'تكرار: لا';
                }
                clearWordHighlights();
            }
        });
        const applyPendingSeek = () => {
            if (audio.duration && Number.isFinite(audio.duration)) {
                $('nkTimeDur').textContent = fmtClock(audio.duration);
                $('nkScrubber').max = audio.duration;
            }
            if (pendingSeekTime !== null && audio.readyState >= 1) {
                audio.currentTime = pendingSeekTime;
                pendingSeekTime = null;
                updateSyncHighlight();
            }
        };
        audio.addEventListener('loadedmetadata', applyPendingSeek);
        audio.addEventListener('canplay', applyPendingSeek);
        $('nkScrubber').addEventListener('input', e => {
            const target = parseFloat(e.target.value) || 0;
            if (audio.readyState >= 1) {
                audio.currentTime = target;
                updateSyncHighlight();
            } else {
                pendingSeekTime = target;
            }
        });
        $('nkBack5Btn').addEventListener('click', () => {
            audio.currentTime = Math.max(0, (audio.currentTime || 0) - 5);
            updateSyncHighlight();
        });
        $('nkSpeedBtn').addEventListener('click', () => {
            speedIdx = (speedIdx + 1) % speedOptions.length;
            const rate = speedOptions[speedIdx];
            audio.playbackRate = rate;
            $('nkSpeedBtn').textContent = rate === 0.75 ? 'السرعة: بطيء (٠.٧٥x)' : (rate === 1.15 ? 'السرعة: سريع (١.١٥x)' : 'السرعة: عادي (١x)');
        });
        $('nkLoopBtn').addEventListener('click', () => {
            if (loopMode === 0) {
                loopMode = 3;
                loopRemaining = 3;
                $('nkLoopLabel').textContent = 'تكرار: ٣';
            } else if (loopMode === 3) {
                loopMode = 999;
                $('nkLoopLabel').textContent = 'تكرار: دائم';
            } else {
                loopMode = 0;
                $('nkLoopLabel').textContent = 'تكرار: لا';
            }
        });

        // Font size buttons
        document.querySelectorAll('.nk-font-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                state.fontSize = btn.getAttribute('data-font');
                saveState();
                applyMatnFontSize();
            });
        });

        // Memorization controls
        document.querySelectorAll('.nk-mem-lvl-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                memHidePct = Number(btn.getAttribute('data-level'));
                updateMemLevelButtons();
                const h = hadithsById[state.currentId];
                if (h) {
                    computeHiddenWords(h);
                    renderMemMatn(h);
                }
                playSfx('pop');
            });
        });
        $('nkFirstLetterHint').addEventListener('change', () => {
            const h = hadithsById[state.currentId];
            if (h) renderMemMatn(h);
        });
        $('nkReshuffleHideBtn').addEventListener('click', () => {
            memSeed++;
            const h = hadithsById[state.currentId];
            if (h) {
                if (memHidePct === 0) {
                    memHidePct = 35;
                    updateMemLevelButtons();
                }
                computeHiddenWords(h);
                renderMemMatn(h);
                playSfx('pop');
            }
        });
        $('nkRevealNextWordBtn').addEventListener('click', () => {
            const h = hadithsById[state.currentId];
            if (!h) return;
            for (let i = 0; i < h.words.length; i++) {
                if (memHiddenSet.has(i) && !memPeekedSet.has(i)) {
                    memPeekedSet.add(i);
                    playSfx('pop');
                    renderMemMatn(h);
                    return;
                }
            }
        });
        $('nkRevealAllBtn').addEventListener('click', () => {
            const h = hadithsById[state.currentId];
            if (!h) return;
            for (const idx of memHiddenSet) memPeekedSet.add(idx);
            renderMemMatn(h);
            playSfx('pop');
        });

        // Voice recorder controls
        $('nkRecordStartBtn').addEventListener('click', startSelfRecording);
        $('nkRecordStopBtn').addEventListener('click', stopSelfRecording);
        $('nkCompareQariBtn').addEventListener('click', () => {
            const audio = $('nkAudio');
            audio.currentTime = 0;
            audio.play().catch(() => {});
        });

        // Poster Lightbox controls
        const openPoster = () => {
            const h = hadithsById[state.currentId];
            if (!h || !h.poster) return;
            const modal = $('nkPosterModal');
            modal.classList.remove('hidden');
            modal.classList.add('flex');
        };
        $('nkOpenPosterBtn').addEventListener('click', openPoster);
        $('nkPosterPreviewBox').addEventListener('click', openPoster);
        $('nkClosePosterBtn').addEventListener('click', () => {
            $('nkPosterModal').classList.add('hidden');
            $('nkPosterModal').classList.remove('flex');
        });
        $('nkPosterModal').addEventListener('click', e => {
            if (e.target === $('nkPosterModal')) {
                $('nkPosterModal').classList.add('hidden');
                $('nkPosterModal').classList.remove('flex');
            }
        });

        // Certificate controls
        $('nkOpenCertBtn').addEventListener('click', openCertModal);
        $('nkCloseCertBtn').addEventListener('click', () => {
            $('nkCertModal').classList.add('hidden');
            $('nkCertModal').classList.remove('flex');
        });
        $('nkCertForm').addEventListener('submit', e => {
            e.preventDefault();
            refreshCertificatePreview();
            playSfx('correct');
        });
        $('nkCertPrintBtn').addEventListener('click', () => window.print());
    }

    async function init() {
        bindGlobalEvents();
        try {
            const res = await fetch('data/hadiths.json?v=20261120');
            if (!res.ok) throw new Error('HTTP ' + res.status);
            dataset = await res.json();
            dataset.hadiths.forEach(h => { hadithsById[h.id] = h; });

            // Optional direct URL param ?h=5 (honors lock unless teacherMode is active) opens the station directly
            const params = new URLSearchParams(window.location.search);
            const requested = Number(params.get('h'));
            let openStation = false;
            if (requested >= 1 && requested <= 42 && isUnlocked(requested)) {
                state.currentId = requested;
                openStation = true;
            } else if (!isUnlocked(state.currentId)) {
                state.currentId = state.unlockedUpTo;
            }

            updateProgressUI();
            $('nkLoadingState').classList.add('hidden');
            selectHadith(state.currentId);
            if (openStation) {
                showStationView();
            } else {
                showJourneyView(state.completedIds.length > 0);
            }
        } catch (err) {
            $('nkLoadingState').innerHTML = `
                <div class="w-14 h-14 mx-auto mb-3 rounded-2xl bg-rose-100 text-rose-600 flex items-center justify-center">
                    <i class="fas fa-exclamation-triangle text-xl"></i>
                </div>
                <p class="font-black text-base text-[#2c1e14]">تعذر تحميل بيانات الأحاديث</p>
                <p class="text-xs text-[#6f5236] mt-1">يرجى تحديث الصفحة والمحاولة مرة أخرى.</p>`;
        }
    }

    document.addEventListener('DOMContentLoaded', init);
})();
