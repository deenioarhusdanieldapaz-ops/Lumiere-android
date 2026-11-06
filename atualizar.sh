#!/bin/bash
echo "🚀 1. A enviar código para o GitHub..."
git add .
git commit -m "Auto-update: Nova versão da app"
git push origin main

echo "⏳ 2. A aguardar compilação da Cloud (Isto demora 1-2 min)..."
gh run watch

# O 'gh run watch' devolve 0 se a build passou, ou 1 se falhou
if [ $? -ne 0 ]; then
    echo "❌ A compilação falhou na Cloud! Verifica o erro no GitHub Actions."
    exit 1
fi

echo "📥 3. A descarregar o novo APK..."
rm -rf temp-apk
mkdir -p temp-apk
gh run download -n debug-apk -D temp-apk

if [ ! -f temp-apk/app-debug.apk ]; then
    echo "❌ O ficheiro APK não foi encontrado após o download."
    exit 1
fi

echo "📱 4. A abrir o instalador no telemóvel..."
cp temp-apk/app-debug.apk /sdcard/Download/lumiere-m01.apk
termux-open /sdcard/Download/lumiere-m01.apk

echo "✅ Tudo pronto! Clica em 'Instalar'."
