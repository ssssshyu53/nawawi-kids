<?php
/**
 * Kids Interactive Portal — Al-Arba'oon Al-Nawawiyyah
 * بوابة الأربعين النووية التفاعلية للأطفال (رابط مباشر خاص)
 */
require_once __DIR__ . '/bootstrap.php';

$prefillName = nkPrefillName();
?>
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>الأربعون النووية للأطفال — <?php echo htmlspecialchars(SITE_NAME); ?></title>
    <meta name="description" content="رحلة تفاعلية ممتعة للأطفال من ٦ إلى ١٢ سنة لحفظ وفهم الأربعين النووية مع قراءة صوتية متزامنة كلمة بكلمة وتدريبات وشهادة تقدير">
    <meta name="robots" content="noindex, nofollow">
    <link rel="icon" type="image/png" href="assets/img/book_guide.png">

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Amiri:wght@400;700&family=Cairo:wght@400;600;700;800;900&display=swap" rel="stylesheet">
    <style>
        @font-face {
            font-family: 'Cairo';
            src: url('assets/fonts/Cairo-Bold.ttf') format('truetype');
            font-weight: 700;
            font-display: swap;
        }
        @font-face {
            font-family: 'Amiri';
            src: url('assets/fonts/Amiri-Bold.ttf') format('truetype');
            font-weight: 700;
            font-display: swap;
        }
    </style>

    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    fontFamily: {
                        sans: ['Cairo', 'sans-serif'],
                        serif: ['Amiri', 'serif']
                    }
                }
            }
        };
    </script>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="assets/css/kids.css?v=20261120_5">
</head>
<body class="nk-body nk-pattern min-h-screen flex flex-col">

    <!-- Colorful Islamic Arch & Sky Storybook Top Bar -->
    <header class="relative bg-gradient-to-b from-[#bae6fd] via-[#e0f2fe] to-[#fff9eb] border-b-4 border-[#eab308] shadow-sm overflow-hidden">
        <!-- Decorative Clouds & Stars SVG Backdrop -->
        <svg class="absolute inset-x-0 top-0 w-full h-full pointer-events-none opacity-80" preserveAspectRatio="none" viewBox="0 0 1200 110" aria-hidden="true">
            <!-- Fluffy Clouds -->
            <path d="M40 70 Q55 45 85 52 Q105 32 135 50 Q160 48 170 70 Z" fill="#ffffff" opacity="0.85"/>
            <path d="M1010 68 Q1030 40 1065 48 Q1090 25 1125 46 Q1152 44 1165 68 Z" fill="#ffffff" opacity="0.85"/>
            <!-- Twinkling Stars -->
            <path d="M250 18 L254 28 L265 30 L257 37 L259 47 L250 42 L241 47 L243 37 L235 30 L246 28 Z" fill="#fbbf24" opacity="0.75"/>
            <path d="M920 14 L923 22 L932 23 L925 29 L927 38 L920 34 L913 38 L915 29 L908 23 L917 22 Z" fill="#f59e0b" opacity="0.75"/>
        </svg>

        <div class="relative z-10 max-w-6xl mx-auto px-3 sm:px-6 py-2.5 sm:py-3.5 flex items-center justify-between gap-2 sm:gap-4">
            <!-- Friendly Boy/Girl Guide & Ribbon Crest (Clicks back to Story Map) -->
            <button type="button" id="nkHeaderHomeBtn" class="flex items-center gap-2.5 sm:gap-3.5 min-w-0 text-start cursor-pointer bg-transparent border-0 p-0 hover:opacity-95 transition" title="العودة إلى خريطة الحكاية">
                <div class="flex items-center -space-x-2 rtl:space-x-reverse flex-shrink-0">
                    <img src="assets/img/boy_guide.png" alt="بطل الأربعين النووية" class="w-11 h-11 sm:w-14 sm:h-14 rounded-full shadow-md bg-white object-cover">
                    <img src="assets/img/girl_guide.png" alt="بطلة الأربعين النووية" class="hidden sm:block w-12 h-12 sm:w-14 sm:h-14 rounded-full shadow-md bg-white object-cover">
                </div>
                <div class="min-w-0">
                    <div class="flex items-center gap-2 flex-wrap">
                        <h1 class="font-black text-base sm:text-xl md:text-2xl text-[#1e3a5f] truncate drop-shadow-xs">سلسلة الأربعين النووية للأطفال</h1>
                        <span class="hidden md:inline-flex items-center px-3 py-0.5 rounded-full text-xs font-black bg-[#fef08a] text-[#854d0e] border-2 border-[#eab308]">
                            أبطال من ٦ إلى ١٢ سنة
                        </span>
                    </div>
                    <p class="text-[11px] sm:text-xs text-[#0f766e] truncate font-extrabold">العب، استمع، احفظ، واجمع النجوم الـ ٤٢ لتنال شهادتك!</p>
                </div>
            </button>

            <!-- Joyful Action Controls -->
            <div class="flex items-center gap-1.5 sm:gap-2.5 flex-shrink-0">
                <!-- Collectible Star Counter Pill -->
                <div class="flex items-center gap-1.5 bg-white border-2 border-[#f59e0b] px-2.5 sm:px-3.5 py-1.5 rounded-2xl shadow-[0_3px_0_#d97706]">
                    <svg viewBox="0 0 24 24" class="w-5 h-5 text-[#f59e0b] flex-shrink-0" fill="currentColor" aria-hidden="true">
                        <path d="M12 2l2.9 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l7.1-1.01L12 2z"/>
                    </svg>
                    <span class="text-xs sm:text-sm font-black text-[#78350f]">
                        <span id="nkHeaderDoneCount">٠</span> / ٤٢
                    </span>
                </div>

                <!-- Sound FX Button -->
                <button type="button" id="nkSfxToggleBtn" class="nk-candy-btn w-10 h-10 rounded-2xl bg-white border-2 border-[#38bdf8] border-b-[#0284c7] text-[#0369a1] flex items-center justify-center shadow-xs" title="تشغيل أو كتم مؤثرات التشجيع">
                    <i id="nkSfxIcon" class="fas fa-volume-up text-sm"></i>
                </button>

                <!-- Certificate Reward Button -->
                <button type="button" id="nkOpenCertBtn" class="nk-candy-btn inline-flex items-center gap-1.5 px-3 sm:px-4 py-2 rounded-2xl bg-gradient-to-b from-[#fb7185] to-[#e11d48] border-2 border-[#be123c] border-b-[#881337] text-white text-xs sm:text-sm font-black shadow-sm">
                    <i class="fas fa-award text-amber-200"></i>
                    <span class="hidden sm:inline">شهادة البطل</span>
                    <span class="sm:hidden">الشهادة</span>
                </button>
            </div>
        </div>

        <!-- Rainbow Star Journey Progress Bar -->
        <div class="relative z-10 bg-[#fef3c7]/90 border-t-2 border-[#fde68a] px-3 sm:px-6 py-2">
            <div class="max-w-6xl mx-auto flex items-center gap-2.5 sm:gap-3">
                <span class="text-[11px] sm:text-xs font-black text-[#92400e] whitespace-nowrap flex items-center gap-1">
                    <i class="fas fa-route text-[#14b8a6]"></i> مسار الرحلة:
                </span>
                <div class="flex-1 h-4 bg-white rounded-full overflow-hidden border-2 border-[#eab308] p-0.5 shadow-inner">
                    <div id="nkProgressFill" class="h-full bg-gradient-to-l from-[#14b8a6] via-[#22c55e] to-[#f59e0b] rounded-full transition-all duration-500" style="width: 0%;"></div>
                </div>
                <span id="nkProgressPct" class="text-xs font-black text-[#0f766e] bg-white px-2 py-0.5 rounded-full border border-[#5eead4] whitespace-nowrap">0%</span>
            </div>
        </div>
    </header>

    <!-- Full-Width Centered Kid Adventure Stage -->
    <main class="flex-1 max-w-5xl w-full mx-auto px-3 sm:px-6 py-4 sm:py-6 space-y-5">

        <!-- Loading Skeleton -->
        <div id="nkLoadingState" class="nk-toy-card rounded-3xl p-8 text-center">
            <img src="assets/img/book_guide.png" alt="" class="w-20 h-20 mx-auto mb-3 rounded-full nk-float-slow shadow-md">
            <p class="font-black text-lg text-[#2b1d12]">نفتح كتاب الأربعين النووية للأبطال...</p>
            <p class="text-xs font-bold text-[#7c5826] mt-1">لحظات وتبدأ المغامرة الممتعة</p>
        </div>

        <!-- ================= STORY JOURNEY MAP (HOME) ================= -->
        <section id="nkJourneyView" class="hidden space-y-6">

            <!-- Story Intro Page -->
            <div class="nk-story-intro rounded-[2rem] p-5 sm:p-7 relative overflow-hidden">
                <svg class="absolute -top-2 left-4 w-12 h-20 pointer-events-none opacity-90" viewBox="0 0 60 110" aria-hidden="true">
                    <line x1="30" y1="0" x2="30" y2="30" stroke="#92400e" stroke-width="2.5"/>
                    <path d="M16 30 L44 30 L48 40 L12 40 Z" fill="#f59e0b" stroke="#78350f" stroke-width="2"/>
                    <rect x="14" y="40" width="32" height="38" rx="6" fill="#fef08a" stroke="#b45309" stroke-width="2"/>
                    <path d="M16 78 L44 78 L30 92 Z" fill="#f59e0b" stroke="#78350f" stroke-width="2"/>
                </svg>
                <div class="flex flex-col sm:flex-row items-center gap-4 sm:gap-6">
                    <div class="flex items-end -space-x-4 rtl:space-x-reverse flex-shrink-0">
                        <img src="assets/img/boy_guide.png" alt="عمر صديق الرحلة" class="w-20 h-20 sm:w-28 sm:h-28 rounded-full border-4 border-white shadow-lg bg-white nk-float-slow">
                        <img src="assets/img/girl_guide.png" alt="مريم صديقة الرحلة" class="w-20 h-20 sm:w-28 sm:h-28 rounded-full border-4 border-white shadow-lg bg-white nk-float-slow" style="animation-delay: .7s;">
                    </div>
                    <div class="flex-1 text-center sm:text-start space-y-2.5">
                        <span class="nk-ribbon text-xs sm:text-sm font-black">حكاية رحلة النجوم الأربعين</span>
                        <h2 id="nkHeroGreeting" class="text-2xl sm:text-3xl font-black text-[#1e3a5f]">أهلاً بك يا بطل!</h2>
                        <p class="text-sm sm:text-base font-bold text-[#334155] leading-loose">
                            أنا <b class="text-[#0f766e]">عمر</b> وهذه أختي <b class="text-[#be185d]">مريم</b>، وسنسافر معك عبر
                            <b class="text-[#b45309]">أربعة عوالم</b> عجيبة. في كل محطة حديثٌ نبويٌّ جميل: نستمع له، ثم نفهمه، ثم نحفظه، ثم نلعب لنربح
                            <b class="text-[#b45309]">نجمة ذهبية</b>. اجمع النجوم الـ ٤٢ لتفتح <b class="text-[#be123c]">صندوق الكنز</b> وتنال شهادة البطل!
                        </p>
                    </div>
                </div>

                <div class="mt-5 grid grid-cols-1 sm:grid-cols-[1fr_auto] gap-3 items-end">
                    <label class="block">
                        <span class="text-xs sm:text-sm font-black text-[#0369a1] mb-1.5 block">ما اسمك يا بطل؟ (سيظهر في الحكاية وعلى الشهادة)</span>
                        <input type="text" id="nkHeroNameInput" maxlength="40" placeholder="اكتب اسمك هنا" class="w-full px-4 py-3 rounded-2xl border-[3px] border-[#7dd3fc] bg-white text-base font-black text-[#1e3a5f] focus:border-[#14b8a6] outline-none">
                    </label>
                    <button type="button" id="nkContinueJourneyBtn" class="nk-candy-btn inline-flex items-center justify-center gap-2.5 px-6 py-3.5 rounded-2xl bg-gradient-to-b from-[#34d399] to-[#059669] border-2 border-[#047857] border-b-[#064e3b] text-white text-sm sm:text-base font-black shadow-md">
                        <i class="fas fa-shoe-prints text-[#fef08a]"></i>
                        <span id="nkContinueLabel">ابدأ الرحلة من المحطة الأولى</span>
                    </button>
                </div>
            </div>

            <!-- Journey Stars Summary -->
            <div class="grid grid-cols-2 sm:grid-cols-4 gap-2.5" id="nkChapterChips"></div>

            <!-- Winding Story Path (rendered by kids.js) -->
            <div id="nkJourneyPath" class="space-y-6"></div>

            <!-- Final Treasure Chest -->
            <div class="nk-treasure-end rounded-[2rem] p-6 sm:p-8 text-center space-y-3 relative overflow-hidden">
                <svg viewBox="0 0 120 100" class="w-28 h-24 mx-auto drop-shadow-lg" aria-hidden="true">
                    <path d="M14 44 Q14 16 60 16 Q106 16 106 44 Z" fill="#b45309" stroke="#78350f" stroke-width="3"/>
                    <rect x="14" y="44" width="92" height="46" rx="6" fill="#d97706" stroke="#78350f" stroke-width="3"/>
                    <rect x="14" y="40" width="92" height="9" fill="#facc15" stroke="#78350f" stroke-width="2.5"/>
                    <rect x="52" y="44" width="16" height="22" rx="3" fill="#facc15" stroke="#78350f" stroke-width="2.5"/>
                    <circle cx="60" cy="56" r="3" fill="#78350f"/>
                    <path d="M30 14 L33 6 L36 14 L44 15 L38 20 L40 28 L33 24 L26 28 L28 20 L22 15 Z" fill="#fde047" stroke="#ca8a04" stroke-width="1.5"/>
                    <path d="M88 12 L90 6 L92 12 L98 13 L93 17 L95 23 L90 20 L85 23 L87 17 L82 13 Z" fill="#fde047" stroke="#ca8a04" stroke-width="1.5"/>
                </svg>
                <span class="nk-ribbon text-sm sm:text-base font-black">نهاية الحكاية: صندوق الكنز</span>
                <p id="nkTreasureText" class="text-sm sm:text-base font-extrabold text-[#78350f]">بقي لك ٤٢ نجمة لتفتح الصندوق وتنال شهادة البطل باسمك!</p>
                <button type="button" id="nkJourneyCertBtn" class="nk-candy-btn inline-flex items-center gap-2 px-6 py-3 rounded-2xl bg-gradient-to-b from-[#fb7185] to-[#e11d48] border-2 border-[#be123c] border-b-[#881337] text-white text-sm sm:text-base font-black shadow-md">
                    <i class="fas fa-award text-amber-200"></i>
                    <span>افتح صندوق الكنز (الشهادة)</span>
                </button>
            </div>

            <!-- Parent / Teacher Tools -->
            <div class="flex flex-wrap items-center justify-between gap-3 bg-white/80 rounded-3xl px-4 py-3 border-2 border-[#fde68a]">
                <label class="flex items-center gap-2.5 cursor-pointer">
                    <input type="checkbox" id="nkTeacherModeToggle" class="w-4 h-4 accent-[#0284c7] rounded">
                    <span class="text-xs font-black text-[#78350f]">
                        <i class="fas fa-chalkboard-teacher text-[#0284c7] ml-1"></i>
                        وضع المعلم / ولي الأمر (فتح كل المحطات للاستعراض)
                    </span>
                </label>
                <button type="button" id="nkResetProgressBtn" class="text-xs text-[#92400e] hover:text-rose-700 font-black px-3 py-1.5 rounded-xl hover:bg-rose-50 transition">
                    <i class="fas fa-undo-alt ml-1"></i> إعادة بدء الحكاية من البداية
                </button>
            </div>
        </section>

        <!-- ================= STATION VIEW (ONE HADITH = ONE STORY STATION) ================= -->
        <div id="nkWorkspace" class="hidden space-y-5">

            <!-- Station Top Bar -->
            <div class="flex flex-wrap items-center justify-between gap-2.5 bg-white/85 rounded-3xl p-2.5 sm:p-3 border-2 border-[#e7c892] shadow-sm">
                <button type="button" id="nkOpenMapBtn" class="nk-candy-btn inline-flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-gradient-to-b from-[#38bdf8] to-[#0284c7] border-2 border-[#0369a1] border-b-[#075985] text-white text-xs sm:text-sm font-black shadow-sm">
                    <i class="fas fa-map-marked-alt text-amber-200 text-base"></i>
                    <span>خريطة الرحلة</span>
                </button>
                <span id="nkStationCounter" class="order-last sm:order-none w-full sm:w-auto text-center text-xs sm:text-sm font-black text-[#92400e] bg-[#fef3c7] px-3 py-1.5 rounded-full border-2 border-[#fcd34d]">المحطة ١ من ٤٢</span>
                <div class="flex items-center gap-2">
                    <button type="button" id="nkPrevHadithBtn" class="nk-candy-btn w-10 h-10 sm:w-auto sm:px-4 rounded-2xl bg-[#fef3c7] border-2 border-[#f59e0b] border-b-[#b45309] text-[#78350f] text-xs sm:text-sm font-black flex items-center justify-center gap-1.5 disabled:opacity-40" title="المحطة السابقة">
                        <i class="fas fa-arrow-right"></i>
                        <span class="hidden sm:inline">السابقة</span>
                    </button>
                    <button type="button" id="nkNextHadithBtn" class="nk-candy-btn w-10 h-10 sm:w-auto sm:px-4 rounded-2xl bg-[#ccfbf1] border-2 border-[#14b8a6] border-b-[#0f766e] text-[#115e59] text-xs sm:text-sm font-black flex items-center justify-center gap-1.5 disabled:opacity-40" title="المحطة التالية">
                        <span class="hidden sm:inline">التالية</span>
                        <i class="fas fa-arrow-left"></i>
                    </button>
                </div>
            </div>

            <!-- 1. Illustrated Storybook Hadith Title Banner + 4 Colorful Toy Tabs -->
            <div class="nk-toy-card rounded-[2rem] p-4 sm:p-6 relative overflow-hidden">
                <!-- Decorative Hanging Golden Lanterns (Top Left Corner) -->
                <svg class="hidden sm:block absolute top-0 left-5 w-14 h-24 pointer-events-none" viewBox="0 0 60 110" aria-hidden="true">
                    <line x1="30" y1="0" x2="30" y2="30" stroke="#92400e" stroke-width="2.5"/>
                    <path d="M16 30 L44 30 L48 40 L12 40 Z" fill="#f59e0b" stroke="#78350f" stroke-width="2"/>
                    <rect x="14" y="40" width="32" height="38" rx="6" fill="#fef08a" stroke="#b45309" stroke-width="2"/>
                    <circle cx="30" cy="59" r="7" fill="#f59e0b" opacity="0.5"/>
                    <path d="M16 78 L44 78 L30 92 Z" fill="#f59e0b" stroke="#78350f" stroke-width="2"/>
                </svg>

                <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                    <div class="flex items-center gap-3.5 sm:gap-5 min-w-0">
                        <!-- Giant Golden Collectible Star Medallion -->
                        <div id="nkHadithStarBadge" class="relative w-18 h-18 sm:w-22 sm:h-22 flex-shrink-0 flex items-center justify-center" style="width: 78px; height: 78px;">
                            <svg viewBox="0 0 80 80" class="w-full h-full drop-shadow-md" aria-hidden="true">
                                <path d="M40 3 L50.5 24.5 L74 28 L57 44.5 L61 68 L40 57 L19 68 L23 44.5 L6 28 L29.5 24.5 Z" fill="#facc15" stroke="#b45309" stroke-width="3.5" stroke-linejoin="round"/>
                                <path d="M40 11 L47 26 L64 28.5 L51.5 40.5 L54.5 57 L40 49 L25.5 57 L28.5 40.5 L16 28.5 L33 26 Z" fill="#fef08a" opacity="0.65"/>
                            </svg>
                            <span id="nkStarNumber" class="absolute inset-0 flex items-center justify-center font-black text-xl sm:text-2xl text-[#5c2c06] pt-1">١</span>
                        </div>

                        <div class="min-w-0">
                            <div class="flex flex-wrap items-center gap-2 mb-1.5">
                                <span id="nkHadithHeaderBadge" class="nk-ribbon text-xs sm:text-sm font-black py-1 px-4">الحديث الأول</span>
                                <span id="nkStageBadge" class="inline-flex items-center px-3 py-1 rounded-full text-xs font-extrabold bg-[#e0f2fe] text-[#0369a1] border-2 border-[#7dd3fc]">المرحلة الأولى</span>
                                <span id="nkCompletedBadge" class="hidden inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-black bg-[#dcfce7] text-[#15803d] border-2 border-[#4ade80]">
                                    <i class="fas fa-check-circle"></i> نجمة مكتسبة!
                                </span>
                            </div>
                            <h2 id="nkHadithTitle" class="text-xl sm:text-2xl md:text-3xl font-black text-[#2b1d12] leading-snug break-words">الأعمال بالنيات</h2>
                        </div>
                    </div>

                    <!-- Illustrated Poster Preview Button -->
                    <div class="flex items-center gap-2 flex-shrink-0 sm:pl-16">
                        <button type="button" id="nkOpenPosterBtn" class="nk-candy-btn hidden inline-flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-[#fef08a] border-2 border-[#eab308] border-b-[#a16207] text-[#713f12] text-xs sm:text-sm font-black shadow-xs">
                            <i class="fas fa-image text-base"></i>
                            <span>شاهد اللوحة الكاملة</span>
                        </button>
                    </div>
                </div>

                <!-- Companion Guide Cheer Speech Bubble -->
                <div class="mt-4 bg-gradient-to-l from-[#f0f9ff] to-[#fffbeb] rounded-2xl p-3 border-2 border-[#bae6fd] flex items-center gap-3">
                    <img id="nkCompanionAvatar" src="assets/img/boy_guide.png" alt="مرشد الرحلة" class="w-11 h-11 sm:w-12 sm:h-12 rounded-full flex-shrink-0 shadow-xs bg-white">
                    <div class="min-w-0">
                        <span class="text-[11px] font-black text-[#0284c7] block">صديقك في الرحلة يقول:</span>
                        <p id="nkCompanionSpeech" class="text-xs sm:text-sm font-extrabold text-[#1e3a5f] leading-snug">هيا نستمع للحديث معاً! اضغط على أي كلمة في السحابة لتسمع نطقها.</p>
                    </div>
                </div>

                <!-- 4 Story Steps of the Station (ordered journey inside each hadith) -->
                <div class="mt-4 pt-4 border-t-2 border-[#f6e3b6]">
                    <p class="text-center text-xs sm:text-sm font-black text-[#92400e] mb-3">
                        <i class="fas fa-shoe-prints text-[#14b8a6] ml-1"></i>
                        خطوات المحطة الأربع — امشِ معنا خطوة خطوة لتربح النجمة
                    </p>
                    <div class="nk-steps relative grid grid-cols-4 gap-1.5 sm:gap-3">
                        <span class="nk-steps-line" aria-hidden="true"></span>
                        <button type="button" data-tab="listen" class="nk-tab-btn nk-step is-active">
                            <span class="nk-step-dot"><i class="fas fa-headphones-alt"></i><span class="nk-step-num">١</span></span>
                            <span class="nk-step-label">استمع</span>
                        </button>
                        <button type="button" data-tab="learn" class="nk-tab-btn nk-step">
                            <span class="nk-step-dot"><i class="fas fa-lightbulb"></i><span class="nk-step-num">٢</span></span>
                            <span class="nk-step-label">افهم</span>
                        </button>
                        <button type="button" data-tab="memorize" class="nk-tab-btn nk-step">
                            <span class="nk-step-dot"><i class="fas fa-microphone-alt"></i><span class="nk-step-num">٣</span></span>
                            <span class="nk-step-label">احفظ</span>
                        </button>
                        <button type="button" data-tab="quiz" class="nk-tab-btn nk-step">
                            <span class="nk-step-dot"><i class="fas fa-star"></i><span class="nk-step-num">٤</span></span>
                            <span class="nk-step-label">العب<span class="hidden sm:inline"> واربح</span></span>
                        </button>
                    </div>
                </div>
            </div>

            <!-- TAB 1: LISTEN & SCALLOPED CLOUD WORD-BY-WORD MATN -->
            <div id="nkTabListen" class="nk-tab-panel space-y-5">
                <!-- Colorful Capsule Audio Player -->
                <div class="rounded-[2rem] p-4 sm:p-5 bg-gradient-to-l from-[#0d9488] via-[#14b8a6] to-[#0284c7] text-white border-3 border-[#0f766e] shadow-[0_7px_0_#0f766e]">
                    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-3.5">
                        <div class="flex items-center gap-3.5">
                            <button type="button" id="nkPlayBtn" class="nk-candy-btn w-15 h-15 sm:w-16 sm:h-16 rounded-full bg-gradient-to-b from-[#fde047] to-[#f59e0b] border-3 border-[#ffffff] border-b-[#b45309] text-[#5c2c06] shadow-lg flex items-center justify-center text-2xl flex-shrink-0" style="width: 60px; height: 60px;" aria-label="تشغيل التلاوة">
                                <i id="nkPlayIcon" class="fas fa-play"></i>
                            </button>
                            <div>
                                <p class="font-black text-base sm:text-lg text-white drop-shadow-xs">مشغّل القراءة والتتبع كلمةً بكلمة</p>
                                <p class="text-xs sm:text-sm text-teal-50 font-bold">اضغط على أي كلمة في السحابة البيضاء لسماعها فوراً!</p>
                            </div>
                        </div>

                        <div class="flex flex-wrap items-center gap-2">
                            <button type="button" id="nkBack5Btn" class="nk-candy-btn px-3 py-1.5 rounded-xl bg-white text-[#0f766e] border-2 border-[#99f6e4] border-b-[#115e59] text-xs font-black">
                                <i class="fas fa-undo ml-1"></i> ارجع ٥ث
                            </button>
                            <button type="button" id="nkSpeedBtn" class="nk-candy-btn px-3 py-1.5 rounded-xl bg-[#fef08a] text-[#713f12] border-2 border-[#fde047] border-b-[#b45309] text-xs font-black">
                                السرعة: عادي (١x)
                            </button>
                            <button type="button" id="nkLoopBtn" class="nk-candy-btn px-3 py-1.5 rounded-xl bg-white text-[#0369a1] border-2 border-[#bae6fd] border-b-[#0369a1] text-xs font-black">
                                <i class="fas fa-sync-alt ml-1"></i> <span id="nkLoopLabel">تكرار: لا</span>
                            </button>
                        </div>
                    </div>

                    <div class="flex items-center gap-3 bg-white/15 rounded-2xl px-3 py-2" dir="ltr">
                        <span id="nkTimeCur" class="text-xs font-black font-mono text-[#fef08a] w-11 text-center">00:00</span>
                        <input type="range" id="nkScrubber" class="nk-range flex-1" min="0" max="100" step="0.05" value="0" aria-label="شريط تقدم الصوت">
                        <span id="nkTimeDur" class="text-xs font-black font-mono text-white w-11 text-center">00:00</span>
                    </div>
                    <audio id="nkAudio" preload="metadata" class="hidden"></audio>
                </div>

                <!-- Scalloped Cloud Matn Box + Storybook Picture Scene -->
                <div class="grid grid-cols-1 lg:grid-cols-12 gap-5 items-start">
                    <!-- Main Scalloped Matn Cloud -->
                    <div id="nkMatnColumn" class="lg:col-span-8 nk-cloud-matn rounded-[2rem] p-5 sm:p-8">
                        <div class="flex flex-wrap items-center justify-between gap-2 pb-3 mb-4 border-b-2 border-[#fde68a]">
                            <div class="flex items-center gap-2">
                                <img src="assets/img/book_guide.png" alt="" class="w-9 h-9 rounded-full shadow-xs">
                                <span class="text-xs sm:text-sm font-black text-[#92400e]">سحابة الحديث الشريف — اضغط أي كلمة لتسمعها</span>
                            </div>
                            <div class="flex items-center gap-1 bg-[#fef3c7] p-1 rounded-xl border-2 border-[#fcd34d]">
                                <button type="button" data-font="sm" class="nk-font-btn px-2.5 py-0.5 rounded-lg text-[11px] font-black text-[#78350f]">صغير</button>
                                <button type="button" data-font="md" class="nk-font-btn px-2.5 py-0.5 rounded-lg text-[11px] font-black bg-[#14b8a6] text-white">متوسط</button>
                                <button type="button" data-font="lg" class="nk-font-btn px-2.5 py-0.5 rounded-lg text-[11px] font-black text-[#78350f]">كبير</button>
                            </div>
                        </div>

                        <div id="nkMatnWordsContainer" class="nk-matn-font nk-size-md text-[#23180f] space-y-3 select-text">
                            <!-- Word spans -->
                        </div>

                        <div class="mt-6 pt-4 border-t-2 border-[#fde68a] flex justify-center sm:justify-end">
                            <button type="button" class="nk-goto-tab nk-candy-btn inline-flex items-center gap-2 px-6 py-3 rounded-2xl bg-gradient-to-b from-[#fde047] to-[#f59e0b] border-2 border-[#d97706] border-b-[#92400e] text-[#5c2c06] text-sm sm:text-base font-black shadow-sm" data-target="learn">
                                <span>الخطوة ٢: هيا نفهم الحديث</span>
                                <i class="fas fa-arrow-left"></i>
                            </button>
                        </div>
                    </div>

                    <!-- Right Storybook Illustration Card -->
                    <div class="lg:col-span-4 nk-toy-card rounded-[2rem] p-4 space-y-3">
                        <h3 class="font-black text-sm sm:text-base text-[#1e3a5f] flex items-center gap-2">
                            <span class="w-8 h-8 rounded-xl bg-[#ffe4e6] text-[#e11d48] flex items-center justify-center">
                                <i class="fas fa-palette"></i>
                            </span>
                            <span id="nkVisualCardTitle">اللوحة المصورة للحديث</span>
                        </h3>
                        <div id="nkPosterPreviewBox" class="rounded-2xl overflow-hidden border-3 border-[#f59e0b] bg-[#fffbeb] relative group cursor-pointer shadow-sm">
                            <img id="nkPosterImg" src="" alt="لوحة الحديث المصورة" class="w-full h-auto object-cover hidden">
                            <div id="nkPosterZoomHint" class="hidden absolute bottom-2 left-2 right-2 bg-[#1e3a5f]/85 text-white text-[11px] font-black px-3 py-1.5 rounded-xl text-center">
                                <i class="fas fa-search-plus ml-1"></i> اضغط لتكبير اللوحة
                            </div>
                            <div id="nkFallbackArtBox" class="hidden">
                                <!-- Clean storybook illustration scene for stages 1-4 -->
                            </div>
                        </div>
                        <div class="bg-[#f0f9ff] rounded-2xl p-3.5 border-2 border-[#bae6fd]">
                            <p class="text-xs font-black text-[#0369a1] mb-1">الفكرة الجميلة للحديث:</p>
                            <p id="nkSideSummary" class="text-xs sm:text-sm font-bold text-[#1e3a5f] leading-relaxed"></p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- TAB 2: POSTER-STYLE EXPLANATION, VOCABULARY RIBBONS & LESSONS -->
            <div id="nkTabLearn" class="nk-tab-panel hidden space-y-5">
                <div class="nk-parchment-scroll rounded-[2rem] p-5 sm:p-7 text-center">
                    <div class="inline-flex items-center gap-2 px-5 py-1.5 rounded-full bg-[#78350f] text-[#fef08a] text-xs sm:text-sm font-black mb-3 shadow-xs">
                        <i class="fas fa-scroll"></i>
                        <span>الْمَعْنَى الْعَامُّ لِلْحَدِيثِ</span>
                    </div>
                    <p id="nkGeneralMeaning" class="nk-matn-font text-xl sm:text-2xl font-bold text-[#3f2009] leading-relaxed max-w-3xl mx-auto"></p>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
                    <!-- Vocabulary Ribbons -->
                    <div class="nk-toy-card rounded-[2rem] p-5 space-y-3.5">
                        <div class="text-center mb-2">
                            <span class="nk-ribbon text-sm sm:text-base font-black">مَعَانِي الْكَلِمَاتِ</span>
                        </div>
                        <div id="nkVocabList" class="space-y-3"></div>
                    </div>

                    <!-- 3 Lessons -->
                    <div class="nk-toy-card rounded-[2rem] p-5 space-y-3.5">
                        <div class="text-center mb-2">
                            <span class="inline-flex items-center justify-center px-5 py-2 rounded-2xl bg-[#14b8a6] border-2 border-[#0f766e] text-white font-black text-sm sm:text-base shadow-[0_4px_0_#0f766e]">
                                مَاذَا نَتَعَلَّمُ مِنَ الْحَدِيثِ؟
                            </span>
                        </div>
                        <div id="nkLessonsList" class="space-y-3"></div>
                    </div>
                </div>

                <div class="nk-toy-card rounded-[2rem] p-4 sm:p-5 bg-[#fffbeb]">
                    <div class="flex items-start gap-3">
                        <img src="assets/img/book_guide.png" alt="" class="w-11 h-11 rounded-full flex-shrink-0">
                        <div>
                            <h4 class="font-black text-sm text-[#78350f] mb-1">من كتاب الأربعين النووية (طبعة دار المنهاج)</h4>
                            <p id="nkSourceText" class="text-xs sm:text-sm text-[#5c3d1e] leading-relaxed font-bold"></p>
                        </div>
                    </div>
                </div>

                <div class="flex justify-center sm:justify-end">
                    <button type="button" class="nk-goto-tab nk-candy-btn inline-flex items-center gap-2 px-6 py-3 rounded-2xl bg-[#8b5cf6] border-2 border-[#7c3aed] border-b-[#5b21b6] text-white text-sm sm:text-base font-black shadow-sm" data-target="memorize">
                        <span>الخطوة ٣: هيا نحفظ الحديث</span>
                        <i class="fas fa-arrow-left"></i>
                    </button>
                </div>
            </div>

            <!-- TAB 3: INTERACTIVE MEMORIZATION & SELF-RECITATION STUDIO -->
            <div id="nkTabMemorize" class="nk-tab-panel hidden space-y-5">
                <div class="nk-toy-card rounded-[2rem] p-4 sm:p-6 space-y-4">
                    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                        <div>
                            <h3 class="font-black text-base sm:text-lg text-[#4c1d95] flex items-center gap-2">
                                <i class="fas fa-magic text-[#8b5cf6]"></i>
                                <span>تحدّي الحفظ والتسميع الذكي</span>
                            </h3>
                            <p class="text-xs sm:text-sm text-[#6b4d2b] font-bold mt-0.5">أخفِ كلمات الحديث بالتدريج، واقرأ من حفظك أو سجّل صوتك، ثم اضغط على أي بطاقة صفراء للتأكد!</p>
                        </div>
                        <button type="button" id="nkRevealAllBtn" class="nk-candy-btn px-4 py-2 rounded-2xl bg-[#fef08a] border-2 border-[#eab308] border-b-[#a16207] text-[#713f12] text-xs font-black flex-shrink-0">
                            <i class="fas fa-eye ml-1"></i> اكشف النص كاملاً للمراجعة
                        </button>
                    </div>

                    <div class="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                        <button type="button" data-level="0" class="nk-mem-lvl-btn nk-candy-btn px-3 py-2.5 rounded-2xl text-xs font-black border-2 bg-[#8b5cf6] text-white border-[#6d28d9] border-b-[#4c1d95]">١. ظاهر كاملاً</button>
                        <button type="button" data-level="35" class="nk-mem-lvl-btn nk-candy-btn px-3 py-2.5 rounded-2xl text-xs font-black border-2 bg-[#f5f3ff] text-[#5b21b6] border-[#c4b5fd] border-b-[#8b5cf6]">٢. تحدّي ٣٥٪</button>
                        <button type="button" data-level="70" class="nk-mem-lvl-btn nk-candy-btn px-3 py-2.5 rounded-2xl text-xs font-black border-2 bg-[#f5f3ff] text-[#5b21b6] border-[#c4b5fd] border-b-[#8b5cf6]">٣. تحدّي ٧٠٪</button>
                        <button type="button" data-level="100" class="nk-mem-lvl-btn nk-candy-btn px-3 py-2.5 rounded-2xl text-xs font-black border-2 bg-[#f5f3ff] text-[#5b21b6] border-[#c4b5fd] border-b-[#8b5cf6]">٤. تسميع كامل ١٠٠٪</button>
                    </div>

                    <div class="flex flex-wrap items-center justify-between gap-2 pt-2 border-t-2 border-[#f3e8ff]">
                        <div class="flex flex-wrap items-center gap-2">
                            <label class="inline-flex items-center gap-2 cursor-pointer bg-[#f5f3ff] px-3.5 py-2 rounded-xl border-2 border-[#ddd6fe] text-xs font-extrabold text-[#4c1d95]">
                                <input type="checkbox" id="nkFirstLetterHint" class="w-4 h-4 accent-[#7c3aed]">
                                <span>ساعدني بأول حرف</span>
                            </label>
                            <button type="button" id="nkRevealNextWordBtn" class="nk-candy-btn px-3.5 py-2 rounded-xl bg-[#e0f2fe] border-2 border-[#7dd3fc] border-b-[#0284c7] text-[#0369a1] text-xs font-black">
                                <i class="fas fa-step-backward ml-1"></i> اكشف كلمة
                            </button>
                            <button type="button" id="nkReshuffleHideBtn" class="nk-candy-btn px-3.5 py-2 rounded-xl bg-[#fef3c7] border-2 border-[#fcd34d] border-b-[#d97706] text-[#92400e] text-xs font-black">
                                <i class="fas fa-random ml-1"></i> غيّر الكلمات المخفية
                            </button>
                        </div>
                        <span id="nkMemRevealedCount" class="text-xs font-black text-[#6d28d9]">اضغط على أي بطاقة صفراء لكشفها</span>
                    </div>
                </div>

                <!-- Scalloped Cloud Box for Memorization -->
                <div class="nk-cloud-matn rounded-[2rem] p-5 sm:p-8">
                    <div id="nkMemMatnBox" class="nk-matn-font nk-size-md text-[#23180f] space-y-3"></div>
                </div>

                <!-- Kid Voice Self-Recitation Studio -->
                <div class="nk-toy-card rounded-[2rem] p-4 sm:p-6 bg-gradient-to-l from-[#fff1f2] via-[#ffffff] to-[#fffbeb]">
                    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                        <div class="flex items-center gap-3.5">
                            <img src="assets/img/girl_guide.png" alt="" class="w-12 h-12 sm:w-14 sm:h-14 rounded-full flex-shrink-0 shadow-sm">
                            <div class="space-y-0.5">
                                <h4 class="font-black text-sm sm:text-base text-[#9f1239] flex items-center gap-2">
                                    <i class="fas fa-microphone text-[#f43f5e]"></i>
                                    <span>استوديو التسميع الصوتي والمقارنة</span>
                                </h4>
                                <p id="nkRecStatusText" class="text-xs text-[#6f5236] font-bold">اضغط «سجّل تسميعي»، اقرأ الحديث غيباً، ثم قارن صوتك بصوت القارئ!</p>
                            </div>
                        </div>

                        <div class="flex flex-wrap items-center gap-2 flex-shrink-0">
                            <button type="button" id="nkRecordStartBtn" class="nk-candy-btn px-5 py-2.5 rounded-2xl bg-[#f43f5e] border-2 border-[#e11d48] border-b-[#9f1239] text-white text-xs sm:text-sm font-black shadow-sm flex items-center gap-2">
                                <i class="fas fa-microphone"></i>
                                <span>سجّل تسميعي الآن</span>
                            </button>
                            <button type="button" id="nkRecordStopBtn" class="hidden nk-candy-btn px-5 py-2.5 rounded-2xl bg-[#991b1b] border-2 border-[#7f1d1d] border-b-[#450a0a] text-white text-xs sm:text-sm font-black shadow-sm flex items-center gap-2">
                                <i class="fas fa-stop-circle"></i>
                                <span>أنهِ التسجيل وراجع (<span id="nkRecTimer">00:00</span>)</span>
                            </button>
                        </div>
                    </div>

                    <div id="nkRecWaveBox" class="hidden mt-4 p-3 rounded-2xl bg-rose-50 border-2 border-rose-200 flex items-center justify-center gap-1.5 h-14" aria-hidden="true">
                        <span class="w-2 h-7 bg-[#f43f5e] rounded-full nk-rec-bar"></span>
                        <span class="w-2 h-10 bg-[#f43f5e] rounded-full nk-rec-bar" style="animation-delay:0.1s"></span>
                        <span class="w-2 h-6 bg-[#f43f5e] rounded-full nk-rec-bar" style="animation-delay:0.2s"></span>
                        <span class="w-2 h-9 bg-[#f43f5e] rounded-full nk-rec-bar" style="animation-delay:0.3s"></span>
                        <span class="w-2 h-7 bg-[#f43f5e] rounded-full nk-rec-bar" style="animation-delay:0.15s"></span>
                        <span class="text-xs font-black text-rose-700 mr-2">أنت تسجّل الآن... أحسنت واصل التلاوة!</span>
                    </div>

                    <div id="nkRecPlaybackBox" class="hidden mt-4 p-4 rounded-2xl bg-[#f0fdfa] border-2 border-[#5eead4] space-y-3">
                        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                            <span class="text-xs sm:text-sm font-black text-[#0f766e] flex items-center gap-1.5">
                                <i class="fas fa-check-circle"></i> أحسنت! اسمع تسميعك الآن وقارنه بصوت القارئ:
                            </span>
                            <button type="button" id="nkCompareQariBtn" class="nk-candy-btn px-4 py-1.5 rounded-xl bg-[#14b8a6] border-2 border-[#0f766e] border-b-[#115e59] text-white text-xs font-black">
                                <i class="fas fa-play ml-1"></i> شغّل صوت القارئ للمقارنة
                            </button>
                        </div>
                        <audio id="nkUserRecAudio" controls class="w-full h-10"></audio>
                    </div>
                </div>

                <div class="flex justify-center sm:justify-end">
                    <button type="button" class="nk-goto-tab nk-candy-btn inline-flex items-center gap-2 px-6 py-3 rounded-2xl bg-gradient-to-b from-[#fb7185] to-[#e11d48] border-2 border-[#be123c] border-b-[#881337] text-white text-sm sm:text-base font-black shadow-sm" data-target="quiz">
                        <span>الخطوة الأخيرة: العب واربح النجمة</span>
                        <i class="fas fa-star text-amber-200"></i>
                    </button>
                </div>
            </div>

            <!-- TAB 4: PLAYFUL EXERCISES & LEVEL UNLOCK -->
            <div id="nkTabQuiz" class="nk-tab-panel hidden space-y-5">
                <div class="nk-toy-card rounded-[2rem] p-4 sm:p-6">
                    <div class="flex flex-wrap items-center justify-between gap-2 pb-3 mb-4 border-b-2 border-[#f6e3b6]">
                        <div>
                            <h3 class="font-black text-base sm:text-xl text-[#9f1239] flex items-center gap-2">
                                <i class="fas fa-puzzle-piece text-[#f43f5e]"></i>
                                <span>تحدّي الأبطال لفتح النجمة التالية</span>
                            </h3>
                            <p class="text-xs sm:text-sm text-[#6f5236] font-bold mt-0.5">أجب على الأسئلة الثلاثة لتفوز بنجمة هذا الحديث ويُفتح الحديث التالي!</p>
                        </div>
                        <span id="nkQuizScoreBadge" class="px-4 py-1.5 rounded-full text-xs sm:text-sm font-black bg-[#fef08a] text-[#854d0e] border-2 border-[#eab308]">
                            ٠ / ٣ صحيح
                        </span>
                    </div>

                    <div id="nkExercisesContainer" class="space-y-5"></div>

                    <!-- Confetti & Golden Star Celebration Banner -->
                    <div id="nkUnlockBanner" class="hidden nk-pop-in mt-6 rounded-3xl p-6 bg-gradient-to-l from-[#d1fae5] via-[#ecfdf5] to-[#fef08a] border-3 border-[#22c55e] text-center space-y-3 shadow-lg relative overflow-hidden">
                        <div class="w-20 h-20 mx-auto rounded-full bg-gradient-to-b from-[#22c55e] to-[#16a34a] border-4 border-white text-white flex items-center justify-center shadow-lg">
                            <svg viewBox="0 0 40 40" class="w-12 h-12" fill="none" aria-hidden="true">
                                <path d="M20 4 L24.5 14 L35 15.5 L27.5 23 L29.2 33.5 L20 28.5 L10.8 33.5 L12.5 23 L5 15.5 L15.5 14 Z" fill="#fde047" stroke="#ffffff" stroke-width="2" stroke-linejoin="round"/>
                            </svg>
                        </div>
                        <h4 id="nkUnlockHeading" class="text-xl sm:text-2xl font-black text-[#14532d]">أحسنت يا بطل! فزت بنجمة الحديث!</h4>
                        <p id="nkUnlockSub" class="text-xs sm:text-base font-extrabold text-[#166534]">تم فتح الحديث التالي في رحلتك بنجاح!</p>
                        <div class="pt-2 flex flex-wrap items-center justify-center gap-3">
                            <button type="button" id="nkBannerMapBtn" class="nk-candy-btn px-6 py-3.5 rounded-2xl bg-gradient-to-b from-[#38bdf8] to-[#0284c7] border-2 border-[#0369a1] border-b-[#075985] text-white font-black text-sm sm:text-base shadow-md inline-flex items-center gap-2">
                                <i class="fas fa-map-marked-alt text-amber-200"></i>
                                <span>شاهد نجمتك على الخريطة</span>
                            </button>
                            <button type="button" id="nkUnlockNextBtn" class="nk-candy-btn px-7 py-3.5 rounded-2xl bg-[#14b8a6] border-2 border-[#0f766e] border-b-[#115e59] text-white font-black text-sm sm:text-base shadow-md inline-flex items-center gap-2">
                                <span>انطلق إلى الحديث التالي</span>
                                <i class="fas fa-arrow-left"></i>
                            </button>
                            <button type="button" id="nkUnlockCertBtn" class="hidden nk-candy-btn px-7 py-3.5 rounded-2xl bg-gradient-to-b from-[#fb7185] to-[#e11d48] border-2 border-[#be123c] border-b-[#881337] text-white font-black text-sm sm:text-base shadow-md inline-flex items-center gap-2">
                                <i class="fas fa-award text-amber-200"></i>
                                <span>أصدر شهادة إتمام الأربعين باسمك الآن</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <!-- Poster Lightbox Modal -->
    <div id="nkPosterModal" class="fixed inset-0 z-50 hidden items-center justify-center bg-slate-950/85 p-3 sm:p-6 overflow-y-auto">
        <div class="relative w-full max-w-2xl bg-[#fffaf0] rounded-3xl p-3 sm:p-4 border-4 border-[#f59e0b] shadow-2xl my-auto">
            <div class="flex items-center justify-between gap-2 pb-2 mb-2 border-b-2 border-[#fde68a]">
                <span id="nkPosterModalTitle" class="font-black text-sm sm:text-base text-[#2c1e14] truncate">اللوحة المصورة</span>
                <button type="button" id="nkClosePosterBtn" class="w-9 h-9 rounded-xl bg-[#fef3c7] hover:bg-[#fde68a] text-[#78350f] flex items-center justify-center">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="max-h-[78vh] overflow-y-auto rounded-2xl">
                <img id="nkPosterModalImg" src="" alt="لوحة الحديث المصورة" class="w-full h-auto rounded-xl">
            </div>
        </div>
    </div>

    <!-- Final Completion Certificate Modal -->
    <div id="nkCertModal" class="fixed inset-0 z-50 hidden items-center justify-center bg-slate-950/80 backdrop-blur-xs p-3 sm:p-5 overflow-y-auto">
        <div class="w-full max-w-3xl bg-[#fffaf0] rounded-[2rem] border-4 border-[#eab308] shadow-2xl overflow-hidden my-auto max-h-[94vh] flex flex-col">
            <div class="px-5 py-4 bg-gradient-to-l from-[#14b8a6] to-[#0284c7] text-white flex items-center justify-between gap-3 flex-shrink-0">
                <div class="flex items-center gap-2.5 min-w-0">
                    <i class="fas fa-award text-amber-200 text-2xl"></i>
                    <h3 class="font-black text-base sm:text-lg truncate">شهادة تقدير وإنجاز الأربعين النووية للأطفال</h3>
                </div>
                <button type="button" id="nkCloseCertBtn" class="w-9 h-9 rounded-xl bg-white/20 hover:bg-white/30 text-white flex items-center justify-center flex-shrink-0">
                    <i class="fas fa-times"></i>
                </button>
            </div>

            <div class="p-4 sm:p-6 overflow-y-auto space-y-5">
                <div id="nkCertLockedNotice" class="hidden rounded-3xl p-5 sm:p-6 bg-[#fef3c7] border-3 border-[#f59e0b] text-center space-y-2.5">
                    <img src="assets/img/boy_guide.png" alt="" class="w-16 h-16 mx-auto rounded-full shadow-sm">
                    <h4 class="font-black text-lg text-[#78350f]">تُفتح الشهادة الذهبية بعد جمع النجوم الـ ٤٢!</h4>
                    <p class="text-xs sm:text-sm font-bold text-[#92400e]">
                        جمعت حتى الآن <strong id="nkCertModalDoneCount">٠</strong> من أصل <strong>٤٢</strong> نجمة. واصل إكمال التدريبات وستحصل على شهادتك باسمك!
                    </p>
                </div>

                <div id="nkCertUnlockedArea" class="hidden space-y-4">
                    <form id="nkCertForm" class="bg-[#fef9c3] rounded-2xl p-4 border-2 border-[#eab308] flex flex-col sm:flex-row items-stretch sm:items-end gap-3">
                        <div class="flex-1">
                            <label for="nkCertNameInput" class="block text-xs sm:text-sm font-black text-[#713f12] mb-1.5">اكتب اسمك الكامل ليُطبع على الشهادة:</label>
                            <input type="text" id="nkCertNameInput" required minlength="2" maxlength="55" value="<?php echo htmlspecialchars($prefillName, ENT_QUOTES, 'UTF-8'); ?>" placeholder="مثال: يوسف عبد الرحمن محمد" class="w-full px-4 py-2.5 rounded-xl bg-white border-2 border-[#eab308] focus:border-[#14b8a6] outline-none text-sm font-black text-[#2c1e14]">
                        </div>
                        <button type="submit" class="nk-candy-btn px-5 py-2.5 rounded-xl bg-[#14b8a6] border-2 border-[#0f766e] border-b-[#115e59] text-white font-black text-sm shadow-sm whitespace-nowrap">
                            <i class="fas fa-magic ml-1"></i> أصدر الشهادة
                        </button>
                    </form>

                    <div id="nkCertPrintArea" class="rounded-2xl overflow-hidden border-3 border-[#eab308] bg-white shadow-md">
                        <img id="nkCertPreviewImg" src="" alt="شهادة تقدير وإنجاز الأربعين النووية للأطفال" class="w-full h-auto block">
                    </div>

                    <div class="flex flex-wrap items-center justify-center gap-3">
                        <a id="nkCertDownloadLink" href="#" download="nawawi-kids-certificate.png" class="nk-candy-btn px-6 py-3 rounded-2xl bg-gradient-to-b from-[#fb7185] to-[#e11d48] border-2 border-[#be123c] border-b-[#881337] text-white font-black text-sm shadow-md inline-flex items-center gap-2">
                            <i class="fas fa-download"></i>
                            <span>تحميل الشهادة (صورة عالية الدقة)</span>
                        </a>
                        <button type="button" id="nkCertPrintBtn" class="nk-candy-btn px-5 py-3 rounded-2xl bg-[#fef3c7] border-2 border-[#eab308] border-b-[#b45309] text-[#78350f] font-black text-sm inline-flex items-center gap-2">
                            <i class="fas fa-print"></i>
                            <span>طباعة</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        window.NAWAWI_KIDS_CONFIG = {
            embedded: <?php echo NK_EMBEDDED ? 'true' : 'false'; ?>,
            defaultName: <?php echo json_encode($prefillName, JSON_UNESCAPED_UNICODE); ?>
        };
    </script>
    <script src="assets/js/kids.js?v=20261120_6"></script>
</body>
</html>
