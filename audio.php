<?php
/**
 * Kids Al-Arba'oon Al-Nawawiyyah — Byte-Range Audio Streamer
 * يدعم التقديم والترجيع والضغط على الكلمات بدقة (HTTP 206 Partial Content)
 */
$id = isset($_GET['h']) ? (int)$_GET['h'] : 0;
if ($id < 1 || $id > 42) {
    http_response_code(400);
    exit('Invalid hadith audio ID');
}

$filePath = __DIR__ . sprintf('/assets/audio/h%02d.mp3', $id);
if (!is_file($filePath) || !is_readable($filePath)) {
    http_response_code(404);
    exit('Audio not found');
}

// Clear any buffers so binary streaming stays pristine
while (ob_get_level() > 0) {
    ob_end_clean();
}

$size = filesize($filePath);
$mtime = filemtime($filePath);
$etag = '"' . md5($id . ':' . $size . ':' . $mtime) . '"';

header('Content-Type: audio/mpeg');
header('Accept-Ranges: bytes');
header('Cache-Control: public, max-age=2592000');
header('ETag: ' . $etag);
header('Last-Modified: ' . gmdate('D, d M Y H:i:s', $mtime) . ' GMT');
header('X-Content-Type-Options: nosniff');

$start = 0;
$end = $size - 1;
$length = $size;

if (isset($_SERVER['HTTP_RANGE']) && preg_match('/bytes=(\d*)-(\d*)/i', $_SERVER['HTTP_RANGE'], $m)) {
    $rStart = $m[1] === '' ? null : (int)$m[1];
    $rEnd = $m[2] === '' ? null : (int)$m[2];

    if ($rStart === null && $rEnd !== null) {
        // Suffix range: bytes=-500
        $start = max(0, $size - $rEnd);
        $end = $size - 1;
    } elseif ($rStart !== null) {
        $start = $rStart;
        $end = ($rEnd !== null && $rEnd < $size) ? $rEnd : ($size - 1);
    }

    if ($start > $end || $start >= $size || $end < 0) {
        http_response_code(416);
        header("Content-Range: bytes */$size");
        exit;
    }

    $length = $end - $start + 1;
    http_response_code(206);
    header("Content-Range: bytes $start-$end/$size");
}

header("Content-Length: $length");

$fp = fopen($filePath, 'rb');
if (!$fp) {
    http_response_code(500);
    exit;
}

fseek($fp, $start);
$remaining = $length;
$chunkSize = 16384;
while ($remaining > 0 && !feof($fp)) {
    $read = min($chunkSize, $remaining);
    $buf = fread($fp, $read);
    if ($buf === false || $buf === '') {
        break;
    }
    echo $buf;
    flush();
    $remaining -= strlen($buf);
}
fclose($fp);
