#!/data/data/com.termux/files/usr/bin/bash
set -u
cd "$(dirname "$0")" || exit 1
echo "🔍 A procurar último build-apk.yml..."
RUN_ID=$(gh run list --workflow="build-apk.yml" --limit 1 --json databaseId -q ".[0].databaseId")
if [ -z "$RUN_ID" ]; then echo "❌ Sem runs."; exit 1; fi
echo "⏳ Run: $RUN_ID"
gh run watch "$RUN_ID"
CONCLUSION=$(gh run view "$RUN_ID" --json conclusion -q ".conclusion")
if [ "$CONCLUSION" != "success" ]; then
  echo ""
  echo "❌ FALHOU. Log:"
  gh run view "$RUN_ID" --log-failed 2>/dev/null | grep -A 15 -E "e: file:///|error:|Caused by|FAILURE: Build failed|Migration didn.t properly handle|IllegalStateException" | sed "s/^[0-9T:.-]*Z //" | head -200
  echo ""
  echo "Ver: https://github.com/deenioarhusdanieldapaz-ops/Lumiere-android/actions/runs/$RUN_ID"
  exit 1
fi
echo ""
echo "📥 A descarregar APK..."
rm -rf temp-apk && mkdir -p temp-apk
gh run download "$RUN_ID" -n debug-apk -D temp-apk
if [ ! -f temp-apk/app-debug.apk ]; then echo "❌ APK não encontrado."; exit 1; fi
cp temp-apk/app-debug.apk /sdcard/Download/lumiere-fase2.apk
echo ""
echo "✅ APK: /sdcard/Download/lumiere-fase2.apk"
