# دليل تسليم المطوّر — Developer Handover

## المتطلبات
- [ ] PHP 8.1+ مع `gd` (FreeType) و `mbstring`
- [ ] Git
- [ ] متصفح حديث (Chrome / Edge / Firefox / Safari)

## التشغيل المحلي

```bash
php -S 127.0.0.1:8080
```

ثم افتح `http://127.0.0.1:8080/`. لفتح محطة مباشرة: `?h=14` (تحترم القفل إلا في وضع المعلم).

## معايير الكود
- **ممنوع استخدام الإيموجي** في الواجهة أو الكود؛ استخدم أيقونات FontAwesome أو SVG.
- كل نص ديناميكي في `kids.js` يمر عبر `escapeHtml()`.
- الأرقام المعروضة للطفل عربية مشرقية عبر `toArDigits()`.
- أي نص عربي يُرسم بـ GD يمر عبر `nkReshape()`.
- حافظ على معرّفات DOM الحالية (`nk*`) لأن `kids.js` يعتمد عليها.

## أوامر مفيدة

| الهدف | الأمر |
| :--- | :--- |
| فحص PHP | `php -l index.php && php -l certificate.php && php -l audio.php` |
| فحص JS | `node -c assets/js/kids.js` |
| اختبار النطاقات | `curl -s -H "Range: bytes=0-99" -o NUL -w "%{http_code}" "http://127.0.0.1:8080/audio.php?h=1"` |
| مسح التقدم | من المتصفح: `localStorage.removeItem('algohari_nawawi_kids_v1')` |

## سير العمل في Git
1. فرع للميزة: `git checkout -b feat/<name>`
2. رسائل الالتزام: `feat(scope): …` / `fix(scope): …` / `style(scope): …`
3. ارفع رقم `?v=` عند تعديل الأصول الثابتة.

## مشكلات شائعة

| المشكلة | الحل |
| :--- | :--- |
| النقر على كلمة لا يقفز للموضع | تأكد أن الصوت يُطلب عبر `audio.php` وأنه يعيد `206` |
| الحروف مقطّعة في الشهادة | ثبّت `khaled.alshamaa/ar-php` أو تأكد من وجود `lib/ArabicGlyphs.php` |
| الشهادة تعيد 500 | فعّل امتداد `gd` مع دعم JPEG و FreeType |
| الخريطة لا تتحدث بعد التعديل | ارفع رقم `?v=` في `index.php` |
