<?php
/**
 * Kids Al-Arba'oon Al-Nawawiyyah — Personalized Certificate Generator
 * مولد شهادة إتمام الأربعين النووية للأطفال
 */
require_once __DIR__ . '/bootstrap.php';
nkLoadVendor();

function nkReshape(string $text): string {
    $text = trim($text);
    if ($text === '' || !preg_match('/[\x{0600}-\x{06FF}]/u', $text)) {
        return $text;
    }

    $toEnglishNumbers = static function (string $str): string {
        return str_replace(['٠','١','٢','٣','٤','٥','٦','٧','٨','٩'], ['0','1','2','3','4','5','6','7','8','9'], $str);
    };
    $toArabicNumbers = static function (string $str): string {
        return str_replace(['0','1','2','3','4','5','6','7','8','9'], ['٠','١','٢','٣','٤','٥','٦','٧','٨','٩'], $str);
    };

    if (class_exists('ArPHP\I18N\Arabic')) {
        try {
            $arabic = new \ArPHP\I18N\Arabic();
            $shaped = $arabic->utf8Glyphs($toEnglishNumbers($text));
            return $toArabicNumbers($shaped);
        } catch (Throwable $e) {
            // Fall through to ArabicGlyphs
        }
    }

    if (class_exists('ArabicGlyphs')) {
        return $toArabicNumbers(ArabicGlyphs::utf8Glyphs($toEnglishNumbers($text)));
    }

    return $text;
}

function nkCleanInput(string $val, int $maxLen = 55): string {
    $val = strip_tags($val);
    $val = preg_replace('/[\x00-\x1F\x7F]/u', '', $val);
    $val = preg_replace('/\s+/u', ' ', trim((string)$val));
    if (mb_strlen($val, 'UTF-8') > $maxLen) {
        $val = mb_substr($val, 0, $maxLen, 'UTF-8');
    }
    return $val;
}

function nkFitAndDrawCentered($im, string $fontPath, string $rawText, int $centerX, int $baselineY, int $maxWidth, int $maxSize, int $minSize, int $color, bool $reshapeText = true): void {
    $drawn = $reshapeText ? nkReshape($rawText) : $rawText;
    $size = $maxSize;
    $box = null;
    while ($size >= $minSize) {
        $box = imagettfbbox($size, 0, $fontPath, $drawn);
        $tw = abs($box[4] - $box[0]);
        if ($tw <= $maxWidth) {
            break;
        }
        $size--;
    }
    if ($box === null) {
        $box = imagettfbbox($size, 0, $fontPath, $drawn);
    }
    $x = (int)round($centerX - ($box[0] + $box[2]) / 2);
    imagettftext($im, $size, 0, $x, $baselineY, $color, $fontPath, $drawn);
}

$studentName = nkCleanInput((string)($_GET['name'] ?? $_POST['name'] ?? ''), 55);
if ($studentName === '') {
    $studentName = nkCleanInput(nkPrefillName(), 55);
}
if (mb_strlen($studentName, 'UTF-8') < 2) {
    $studentName = 'الطالب المتفوق';
}

$dateRaw = nkCleanInput((string)($_GET['date'] ?? date('Y/m/d')), 20);
if (!preg_match('/^[\d\/\-\.\s]{4,20}$/u', $dateRaw)) {
    $dateRaw = date('Y/m/d');
}

$templatePath = __DIR__ . '/assets/certificate_template.jpg';
if (!file_exists($templatePath) || !function_exists('imagecreatefromjpeg')) {
    http_response_code(500);
    header('Content-Type: text/plain; charset=utf-8');
    exit('Certificate generator unavailable.');
}

$im = @imagecreatefromjpeg($templatePath);
if (!$im) {
    http_response_code(500);
    exit('Failed to load certificate template.');
}

$white = imagecolorallocate($im, 255, 255, 255);
$inkName = imagecolorallocate($im, 24, 52, 74);     // Rich deep storybook navy
$inkDate = imagecolorallocate($im, 82, 54, 34);     // Warm dark umber
$inkSig  = imagecolorallocate($im, 15, 118, 102);   // Deep emerald-teal signature ink

// Mask existing dots inside the 3 pill boxes without touching golden borders or printed labels
imagefilledrectangle($im, 276, 392, 691, 428, $white); // Name box dots
imagefilledrectangle($im, 662, 646, 796, 678, $white); // Date box dots
imagefilledrectangle($im, 145, 646, 286, 678, $white); // Signature box dots

$amiriFont = __DIR__ . '/assets/fonts/Amiri-Bold.ttf';
$cairoFont = __DIR__ . '/assets/fonts/Cairo-Bold.ttf';
$nameFont  = file_exists($amiriFont) ? $amiriFont : $cairoFont;
$sigFont   = $nameFont;

// 1. Draw child's name centered in Name pill (276..691 -> width budget 386px)
nkFitAndDrawCentered($im, $nameFont, $studentName, 483, 417, 386, 24, 13, $inkName, true);

// 2. Draw date in Date pill (662..796 -> width budget 122px)
nkFitAndDrawCentered($im, $nameFont, $dateRaw, 729, 666, 122, 15, 10, $inkDate, false);

// 3. Draw academy / Sheikh signature in Signature pill (145..286 -> width budget 128px)
nkFitAndDrawCentered($im, $sigFont, 'أحمد الجوهري', 215, 666, 128, 17, 11, $inkSig, true);

$isDownload = isset($_GET['download']) && $_GET['download'] === '1';
header('Content-Type: image/png');
header('Cache-Control: no-store, no-cache, must-revalidate');
header('X-Content-Type-Options: nosniff');
if ($isDownload) {
    header('Content-Disposition: attachment; filename="nawawi-kids-certificate.png"');
} else {
    header('Content-Disposition: inline; filename="nawawi-kids-certificate.png"');
}

imagepng($im, null, 6);
imagedestroy($im);
