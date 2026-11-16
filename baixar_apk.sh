#!/data/data/com.termux/files/usr/bin/bash
set -u
cd "$(dirname "$0")" || exit 1

echo "🔍 A procurar o último build-apk.yml..."
RUN_ID=""
for i in {1..5}; do
    RUN_ID=$(gh run list --workflow="build-apk.yml" --limit 1 --json databaseId -q ".[0].databaseId" 2>/dev/null)
    if [ -n "$RUN_ID" ]; then break; fi
    echo "⚠️ Falha de rede. Tentativa $i de 5..."
    sleep 3
done

if [ -z "$RUN_ID" ]; then echo "❌ Não foi possível obter o build."; exit 1; fi

echo "⏳ Run: $RUN_ID"
echo "⏱️ A acompanhar o build..."
gh run watch "$RUN_ID"

CONCLUSION=$(gh run view "$RUN_ID" --json conclusion -q ".conclusion")
if [ "$CONCLUSION" != "success" ]; then
  echo "❌ O BUILD FALHOU. Logs:"
  gh run view "$RUN_ID" --log-failed 2>/dev/null | grep -A 15 -E "e: file:///|error:|Caused by|FAILURE: Build failed" | sed "s/^[0-9T:.-]*Z //" | head -200
  exit 1
fi

echo ""
echo "✅ Build com sucesso! A descarregar via GitHub CLI (Seguro)..."

if ! command -v unzip &> /dev/null; then pkg install unzip -y >/dev/null 2>&1; fi

rm -rf temp-apk && mkdir -p temp-apk
# O comando oficial do GitHub faz o download de forma segura e encriptada
gh run download "$RUN_ID" -n debug-apk -D temp-apk

if [ ! -f temp-apk/app-debug.apk ]; then echo "❌ APK não encontrado."; exit 1; fi

cp temp-apk/app-debug.apk /sdcard/Download/lumiere-fase2.apk
echo ""
echo "✅ SUCESSO! APK guardado em: /sdcard/Download/lumiere-fase2.apk"
