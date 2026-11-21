<?php
/**
 * Nawawi Kids — Bootstrap
 * يعمل التطبيق بوضعين:
 *  1) مستقل (Standalone): على أي استضافة PHP بدون قاعدة بيانات.
 *  2) مدمج (Embedded): إذا وُضع داخل مجلد فرعي في أكاديمية الجوهري، يقرأ إعداداتها وجلسة الطالب.
 */

$nkEmbeddedConfig = __DIR__ . '/../config/config.php';
$nkEmbeddedFuncs  = __DIR__ . '/../includes/functions.php';
$nkEmbeddedSess   = __DIR__ . '/../classes/SessionManager.php';

if (is_file($nkEmbeddedConfig) && is_file($nkEmbeddedFuncs) && is_file($nkEmbeddedSess)) {
    require_once $nkEmbeddedConfig;
    require_once $nkEmbeddedFuncs;
    define('NK_EMBEDDED', true);
} else {
    define('NK_EMBEDDED', false);
}

if (!defined('SITE_NAME')) {
    define('SITE_NAME', getenv('NK_SITE_NAME') ?: 'الأربعون النووية للأطفال');
}

if (!function_exists('nkPrefillName')) {
    /** اسم الطالب المسجّل (في الوضع المدمج فقط) لتعبئة الشهادة تلقائياً */
    function nkPrefillName(): string
    {
        if (NK_EMBEDDED && function_exists('isLoggedIn') && isLoggedIn() && class_exists('SessionManager')) {
            return trim((string)(SessionManager::get('username') ?? ''));
        }
        return '';
    }
}

if (!function_exists('nkLoadVendor')) {
    /** تحميل مكتبة ArPHP إن وُجدت (محلياً أو من الأكاديمية) + بديل ArabicGlyphs المضمّن */
    function nkLoadVendor(): void
    {
        foreach ([__DIR__ . '/vendor/autoload.php', __DIR__ . '/../vendor/autoload.php'] as $autoload) {
            if (is_file($autoload)) {
                require_once $autoload;
                break;
            }
        }
        if (!class_exists('ArabicGlyphs')) {
            require_once __DIR__ . '/lib/ArabicGlyphs.php';
        }
    }
}
