# دليل النشر — Deployment Guide

## 1. استضافة مشتركة (cPanel / Hostinger)
1. ارفع محتويات المستودع إلى مجلد عام، مثل `public_html/nawawi-kids/`.
2. تأكد أن PHP ≥ 8.1 وأن امتدادَي `gd` و `mbstring` مفعّلان.
3. افتح `https://your-domain/nawawi-kids/`.
4. لا يلزم أي إعداد أو قاعدة بيانات.

## 2. داخل منصة أكاديمية الجوهري (الوضع المدمج)
ضع المجلد باسم `nawawi-kids/` في جذر الأكاديمية. سيكتشف `bootstrap.php` وجود `../config/config.php` و `../includes/functions.php` فيستخدم اسم الموقع وجلسة الطالب المسجّل لتعبئة الشهادة، و `../vendor/autoload.php` لمكتبة ArPHP.

## 3. خادم VPS (Nginx + PHP-FPM)

```nginx
server {
    listen 443 ssl http2;
    server_name kids.example.com;
    root /var/www/nawawi-kids;
    index index.php;

    location ~ ^/(lib|docs|vendor)/ { deny all; }
    location = /bootstrap.php { deny all; }

    location ~* \.(webp|png|jpg|mp3|ttf)$ {
        expires 30d;
        add_header Cache-Control "public";
    }

    location ~ \.php$ {
        include fastcgi_params;
        fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
        fastcgi_pass unix:/run/php/php8.3-fpm.sock;
    }
}
```

## 4. التحقق بعد النشر

```bash
curl -I https://your-domain/nawawi-kids/                       # 200
curl -s -H "Range: bytes=0-99" -o /dev/null -w "%{http_code}" \
     "https://your-domain/nawawi-kids/audio.php?h=1"            # 206
curl -s -o /dev/null -w "%{content_type}" \
     "https://your-domain/nawawi-kids/certificate.php?name=test" # image/png
```

## 5. التحديثات
- عند تعديل `kids.css` أو `kids.js` ارفع رقم النسخة `?v=` في `index.php` لكسر ذاكرة المتصفح.
- لا يوجد ما يستدعي نسخاً احتياطياً على الخادم؛ تقدّم الأطفال محفوظ في متصفحاتهم.
