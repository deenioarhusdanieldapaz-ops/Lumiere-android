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
gh run watch "$RUN_ID"

CONCLUSION=$(gh run view "$RUN_ID" --json conclusion -q ".conclusion")
if [ "$CONCLUSION" != "success" ]; then
  echo "❌ O BUILD FALHOU."
  gh run view "$RUN_ID" --log-failed 2>/dev/null | grep -A 15 -E "e: file:///|error:|Caused by|FAILURE: Build failed" | sed "s/^[0-9T:.-]*Z //" | head -200
  exit 1
fi

echo ""
echo "✅ Build com sucesso! A preparar download..."
# Fixar o repositório diretamente para evitar falhas de rede do gh repo view
REPO="deenioarhusdanieldapaz-ops/Lumiere-android"
TOKEN=$(gh auth token)
ARTIFACT_URL=$(gh api repos/$REPO/actions/runs/$RUN_ID/artifacts --jq '.artifacts[] | select(.name=="debug-apk") | .archive_download_url')

if [ -z "$ARTIFACT_URL" ]; then echo "❌ Artefato não encontrado."; exit 1; fi

if ! command -v unzip &> /dev/null; then pkg install unzip -y >/dev/null 2>&1; fi
if ! command -v aria2c &> /dev/null; then pkg install aria2 -y >/dev/null 2>&1; fi

echo "📥 A descarregar..."
rm -rf temp-apk && mkdir -p temp-apk

while true; do
    aria2c -c -x16 -s16 --max-tries=0 --retry-wait=3 \
      --header="Authorization: token $TOKEN" \
      -o debug-apk.zip -d temp-apk "$ARTIFACT_URL"
    
    if [ $? -eq 0 ]; then
        cd temp-apk
        unzip -o -q debug-apk.zip
        cd ..
        if [ -f temp-apk/app-debug.apk ]; then break; fi
    fi
    echo "⚠️ A rede falhou. A retomar em 5 segundos..."
    sleep 5
done

cp temp-apk/app-debug.apk /sdcard/Download/lumiere-fase2.apk
echo "✅ SUCESSO! APK guardado em: /sdcard/Download/lumiere-fase2.apk"
