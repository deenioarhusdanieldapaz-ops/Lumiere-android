#!/data/data/com.termux/files/usr/bin/bash
set -u
cd "$(dirname "$0")" || exit 1

echo "🔍 A procurar último build-apk.yml com sucesso..."
# Vai buscar os últimos 5, filtra os que tiveram sucesso, e pega no mais recente
RUN_ID=$(gh run list --workflow="build-apk.yml" --limit 5 --json databaseId,conclusion -q '.[] | select(.conclusion=="success") | .databaseId' | head -1)

if [ -z "$RUN_ID" ]; then
    echo "❌ Nenhum run com sucesso encontrado."
    exit 1
fi

echo "⏳ Run selecionado: $RUN_ID"

echo "🔗 A obter link de download do GitHub..."
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
TOKEN=$(gh auth token)
ARTIFACT_URL=$(gh api repos/$REPO/actions/runs/$RUN_ID/artifacts --jq '.artifacts[] | select(.name=="debug-apk") | .archive_download_url')

if [ -z "$ARTIFACT_URL" ]; then
    echo "❌ Artefato debug-apk não encontrado no run $RUN_ID."
    exit 1
fi

echo "📥 A descarregar APK (modo resiliente)..."
rm -rf temp-apk && mkdir -p temp-apk

# O curl vai tentar até 10 vezes e retomar o download se a rede cair (-C -)
curl -sS -L -C - --retry 10 --retry-delay 3 -H "Authorization: token $TOKEN" -o temp-apk/debug-apk.zip "$ARTIFACT_URL"

if [ ! -f temp-apk/debug-apk.zip ]; then
    echo "❌ Falha ao descarregar o ficheiro."
    exit 1
fi

echo "📦 A extrair APK..."
if ! command -v unzip &> /dev/null; then
    pkg install unzip -y >/dev/null 2>&1
fi
cd temp-apk
unzip -o -q debug-apk.zip
cd ..

if [ ! -f temp-apk/app-debug.apk ]; then
    echo "❌ APK não encontrado após extração."
    exit 1
fi

cp temp-apk/app-debug.apk /sdcard/Download/lumiere-fase2.apk
echo ""
echo "✅ Sucesso! APK guardado em: /sdcard/Download/lumiere-fase2.apk"
