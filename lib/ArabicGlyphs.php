<?php
class ArabicGlyphs {
    private static $glyphs = [
        'ء' => ['ء', 'ء', 'ء', 'ء'],
        'آ' => ['آ', 'آ', 'آ', 'آ'],
        'أ' => ['أ', 'أ', 'أ', 'أ'],
        'ؤ' => ['ؤ', 'ؤ', 'ؤ', 'ؤ'],
        'إ' => ['إ', 'إ', 'إ', 'إ'],
        'ئ' => ['ئ', 'ئ', 'ئ', 'ئ'],
        'ا' => ['ا', 'ا', 'ا', 'ا'],
        'ب' => ['ب', 'ب', 'ب', 'ب'],
        'ة' => ['ة', 'ة', 'ة', 'ة'],
        'ت' => ['ت', 'ت', 'ت', 'ت'],
        'ث' => ['ث', 'ث', 'ث', 'ث'],
        'ج' => ['ج', 'ج', 'ج', 'ج'],
        'ح' => ['ح', 'ح', 'ح', 'ح'],
        'خ' => ['خ', 'خ', 'خ', 'خ'],
        'د' => ['د', 'د', 'د', 'د'],
        'ذ' => ['ذ', 'ذ', 'ذ', 'ذ'],
        'ر' => ['ر', 'ر', 'ر', 'ر'],
        'ز' => ['ز', 'ز', 'ز', 'ز'],
        'س' => ['س', 'س', 'س', 'س'],
        'ش' => ['ش', 'ش', 'ش', 'ش'],
        'ص' => ['ص', 'ص', 'ص', 'ص'],
        'ض' => ['ض', 'ض', 'ض', 'ض'],
        'ط' => ['ط', 'ط', 'ط', 'ط'],
        'ظ' => ['ظ', 'ظ', 'ظ', 'ظ'],
        'ع' => ['ع', 'ع', 'ع', 'ع'],
        'غ' => ['غ', 'غ', 'غ', 'غ'],
        'ف' => ['ف', 'ف', 'ف', 'ف'],
        'ق' => ['ق', 'ق', 'ق', 'ق'],
        'k' => ['ك', 'ك', 'ك', 'ك'],
        'ل' => ['ل', 'ل', 'ل', 'ل'],
        'م' => ['م', 'م', 'م', 'م'],
        'ن' => ['ن', 'ن', 'ن', 'ن'],
        'ه' => ['ه', 'ه', 'ه', 'ه'],
        'و' => ['و', 'و', 'و', 'و'],
        'ى' => ['ى', 'ى', 'ى', 'ى'],
        'ي' => ['ي', 'ي', 'ي', 'ي'],
        'لآ' => ['لآ', 'لآ', 'لآ', 'لآ'],
        'لأ' => ['لأ', 'لأ', 'لأ', 'لأ'],
        'لإ' => ['لإ', 'لإ', 'لإ', 'لإ'],
        'لا' => ['لا', 'لا', 'لا', 'لا']
    ];

    private static $hexGlyphs = [
        '0621' => ['FE80', 'FE80', 'FE80', 'FE80'], // hamza
        '0622' => ['FE81', 'FE82', 'FE81', 'FE82'], // alif_madda
        '0623' => ['FE83', 'FE84', 'FE83', 'FE84'], // alif_hamza
        '0624' => ['FE85', 'FE86', 'FE85', 'FE86'], // waw_hamza
        '0625' => ['FE87', 'FE88', 'FE87', 'FE88'], // alif_hamza_below
        '0626' => ['FE89', 'FE8A', 'FE8B', 'FE8C'], // ya_hamza
        '0627' => ['FE8D', 'FE8E', 'FE8D', 'FE8E'], // alif
        '0628' => ['FE8F', 'FE90', 'FE91', 'FE92'], // ba
        '0629' => ['FE93', 'FE94', 'FE93', 'FE94'], // ta_marbuta
        '062A' => ['FE95', 'FE96', 'FE97', 'FE98'], // ta
        '062B' => ['FE99', 'FE9A', 'FE9B', 'FE9C'], // tha
        '062C' => ['FE9D', 'FE9E', 'FE9F', 'FEA0'], // jeem
        '062D' => ['FEA1', 'FEA2', 'FEA3', 'FEA4'], // ha
        '062E' => ['FEA5', 'FEA6', 'FEA7', 'FEA8'], // kha
        '062F' => ['FEA9', 'FEAA', 'FEA9', 'FEAA'], // dal
        '0630' => ['FEAB', 'FEAC', 'FEAB', 'FEAC'], // dhal
        '0631' => ['FEAD', 'FEAE', 'FEAD', 'FEAE'], // ra
        '0632' => ['FEAF', 'FEB0', 'FEAF', 'FEB0'], // zai
        '0633' => ['FEB1', 'FEB2', 'FEB3', 'FEB4'], // seen
        '0634' => ['FEB5', 'FEB6', 'FEB7', 'FEB8'], // sheen
        '0635' => ['FEB9', 'FEBA', 'FEBB', 'FEBC'], // sad
        '0636' => ['FEBD', 'FEBE', 'FEBF', 'FEC0'], // dad
        '0637' => ['FEC1', 'FEC2', 'FEC3', 'FEC4'], // tah
        '0638' => ['FEC5', 'FEC6', 'FEC7', 'FEC8'], // zah
        '0639' => ['FEC9', 'FECA', 'FECB', 'FECC'], // ain
        '063A' => ['FECD', 'FECE', 'FECF', 'FED0'], // ghain
        '0641' => ['FED1', 'FED2', 'FED3', 'FED4'], // fa
        '0642' => ['FED5', 'FED6', 'FED7', 'FED8'], // qaf
        '0643' => ['FED9', 'FEDA', 'FEDB', 'FEDC'], // kaf
        '0644' => ['FEDD', 'FEDE', 'FEDF', 'FEE0'], // lam
        '0645' => ['FEE1', 'FEE2', 'FEE3', 'FEE4'], // meem
        '0646' => ['FEE5', 'FEE6', 'FEE7', 'FEE8'], // noon
        '0647' => ['FEE9', 'FEEA', 'FEEB', 'FEEC'], // ha
        '0648' => ['FEED', 'FEEE', 'FEED', 'FEEE'], // waw
        '0649' => ['FEEF', 'FEF0', 'FEEF', 'FEF0'], // alif_maksura
        '064A' => ['FEF1', 'FEF2', 'FEF3', 'FEF4'], // ya
        'FEFB' => ['FEFB', 'FEFC', 'FEFB', 'FEFC'], // lam_alif
        'FEF7' => ['FEF7', 'FEF8', 'FEF7', 'FEF8'], // lam_alif_hamza
        'FEF5' => ['FEF5', 'FEF6', 'FEF5', 'FEF6'], // lam_alif_madda
        'FEF9' => ['FEF9', 'FEFA', 'FEF9', 'FEFA'], // lam_alif_hamza_below
    ];

    private static $connectBefore = [
        '0622', '0623', '0624', '0625', '0627', '0629', '062F', '0630', '0631', '0632', '0648', '0649',
        'FEFB', 'FEF7', 'FEF5', 'FEF9'
    ];
    
    // Connects to nothing (Isol)
    // Connects to previous (Final)
    // Connects to next (Initial)
    // Connects to both (Medial)

    public static function utf8Glyphs($text) {
        $text = self::utf8ToUnicode($text);
        $result = [];
        $len = count($text);
        
        for ($i = 0; $i < $len; $i++) {
            $current = sprintf('%04X', $text[$i]);
            $prev = ($i > 0) ? sprintf('%04X', $text[$i-1]) : null;
            $next = ($i < $len-1) ? sprintf('%04X', $text[$i+1]) : null;
            
            // Fix Lam-Alif ligatures
            if ($current == '0644' && $next) {
                $ligature = null;
                switch ($next) {
                    case '0622': $ligature = 'FEF5'; break; // Alif Madda
                    case '0623': $ligature = 'FEF7'; break; // Alif Hamza
                    case '0625': $ligature = 'FEF9'; break; // Alif Hamza Below
                    case '0627': $ligature = 'FEFB'; break; // Alif
                }
                if ($ligature) {
                    $current = $ligature;
                    $i++; // Skip next alif
                    // Re-calculate next after skip
                    $next = ($i < $len-1) ? sprintf('%04X', $text[$i+1]) : null;
                }
            }

            if (!isset(self::$hexGlyphs[$current])) {
                $result[] = html_entity_decode('&#x'.$current.';');
                continue;
            }

            // Determine form
            $prevConnects = false;
            if ($prev && isset(self::$hexGlyphs[$prev])) {
                // If previous letter is one that DOES NOT connect after it
                if (!in_array($prev, self::$connectBefore)) {
                    $prevConnects = true;
                }
            }
            
            // Check if current letter connects to after
            $connectsNext = true;
            if (in_array($current, self::$connectBefore)) {
                $connectsNext = false;
            }
            
            // Check if next letter accepts connection
            $nextConnects = false;
            if ($next && isset(self::$hexGlyphs[$next])) {
                $nextConnects = true;
            }

            // ISOLATED: 0, FINAL: 1, INITIAL: 2, MEDIAL: 3
            if ($prevConnects && $nextConnects && $connectsNext) {
                $form = 3; 
            } elseif ($prevConnects) {
                $form = 1;
            } elseif ($nextConnects && $connectsNext) {
                $form = 2;
            } else {
                $form = 0;
            }
            
            $hex = self::$hexGlyphs[$current][$form];
            $result[] = html_entity_decode('&#x'.$hex.';');
        }
        
        // Reverse for RTL
        return implode('', array_reverse($result));
    }

    private static function utf8ToUnicode($str) {
        $unicode = [];
        $values = [];
        $lookingFor = 1;
        for ($i = 0; $i < strlen($str); $i++) {
            $thisValue = ord($str[$i]);
            if ($thisValue < 128) {
                $unicode[] = $thisValue;
            } else {
                if (count($values) == 0) $lookingFor = ($thisValue < 224) ? 2 : 3;
                $values[] = $thisValue;
                if (count($values) == $lookingFor) {
                    $number = ($lookingFor == 3) ?
                        (($values[0] % 16) * 4096) + (($values[1] % 64) * 64) + ($values[2] % 64) :
                        (($values[0] % 32) * 64) + ($values[1] % 64);
                    $unicode[] = $number;
                    $values = [];
                    $lookingFor = 1;
                }
            }
        }
        return $unicode;
    }
}
