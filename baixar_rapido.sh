#!/data/data/com.termux/files/usr/bin/bash
set -e
cd "$(dirname "$0")" || exit 1

echo "🔍 A procurar último run com sucesso..."
RUN_ID=$(gh run list --workflow="build-apk.yml" --limit 5 --json databaseId,conclusion -q '.[] | select(.conclusion=="success") | .databaseId' | head -1)

if [ -z "$RUN_ID" ]; then
    echo "❌ Nenhum run com sucesso encontrado."
    exit 1
fi

echo "⏳ A descarregar APK do run: $RUN_ID"
rm -rf temp-apk && mkdir -p temp-apk
gh run download "$RUN_ID" -n debug-apk -D temp-apk

if [ ! -f temp-apk/app-debug.apk ]; then
    echo "❌ APK não encontrado."
    exit 1
fi

cp temp-apk/app-debug.apk /sdcard/Download/lumiere-dashboard.apk
echo ""
echo "✅ APK: /sdcard/Download/lumiere-dashboard.apk"
