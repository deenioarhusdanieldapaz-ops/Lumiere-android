#!/bin/bash

echo "🚀 1. A enviar código para o GitHub..."
git add .
git commit -m "Auto-update: Nova versão da app"
git push origin main

echo "⏳ 2. A aguardar compilação do APK (Isto demora 1-2 min)..."
RUN_ID=$(gh run list --workflow="build-apk.yml" --limit 1 --json databaseId -q '.[0].databaseId')

gh run watch $RUN_ID

if [ $? -ne 0 ]; then
    echo "❌ A compilação falhou! Verifica o GitHub Actions."
    exit 1
fi

echo "📥 3. A descarregar o novo APK..."
rm -rf temp-apk
mkdir -p temp-apk
gh run download $RUN_ID -n debug-apk -D temp-apk

echo "📱 4. A copiar para a pasta Downloads..."
cp temp-apk/app-debug.apk /sdcard/Download/lumiere-m01.apk

echo "✅ APK pronto! Abre o teu Gestor de Ficheiros e toca no 'lumiere-m01.apk' na pasta Downloads para atualizar."
