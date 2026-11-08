#!/bin/bash

echo "🚀 1. A enviar código para o GitHub..."
git add .
git commit -m "Auto-update: Nova versão da app"
git push origin main

echo "⏳ 2. A aguardar compilação do APK (Isto demora 1-2 min)..."
RUN_ID=$(gh run list --workflow="build-apk.yml" --limit 1 --json databaseId -q '.[0].databaseId')

gh run watch $RUN_ID

CONCLUSION=$(gh run view $RUN_ID --json conclusion -q '.conclusion')
if [ "$CONCLUSION" != "success" ]; then
    echo ""
    echo "=========================================="
    echo "❌ COMPILAÇÃO FALHOU — LOG DE ERRO:"
    echo "=========================================="
    # Extrai as linhas exatas do compilador Kotlin (ficheiro, linha e motivo)
    gh run view $RUN_ID --log-failed 2>/dev/null | grep -E "e: file:///|Caused by|FAILURE: Build failed" | sed 's/^[0-9T:.-]*Z //'
    echo ""
    echo "=========================================="
    echo "Ver log completo em: https://github.com/deenioarhusdanieldapaz-ops/Lumiere-android/actions/runs/$RUN_ID"
    exit 1
fi

echo "📥 3. A descarregar o novo APK..."
rm -rf temp-apk
mkdir -p temp-apk
gh run download $RUN_ID -n debug-apk -D temp-apk

if [ ! -f temp-apk/app-debug.apk ]; then
    echo "❌ O ficheiro APK não foi encontrado."
    exit 1
fi

echo "📱 4. A copiar para a pasta Downloads..."
cp temp-apk/app-debug.apk /sdcard/Download/lumiere-m01.apk

echo "✅ APK pronto! Vai à pasta Downloads e instala/atualiza o lumiere-m01.apk."
