#!/data/data/com.termux/files/usr/bin/bash
set -u
cd "$(dirname "$0")" || exit 1

echo "🔍 A procurar o último build-apk.yml..."
RUN_ID=""
# Tenta obter o ID do último build até 5 vezes (para não falhar se a rede oscilar)
for i in {1..5}; do
    RUN_ID=$(gh run list --workflow="build-apk.yml" --limit 1 --json databaseId -q ".[0].databaseId" 2>/dev/null)
    if [ -n "$RUN_ID" ]; then
        break
    fi
    echo "⚠️ Falha de rede ao contactar a API. Tentativa $i de 5..."
    sleep 3
done

if [ -z "$RUN_ID" ]; then
    echo "❌ Não foi possível obter o ID do build (rede indisponível)."
    exit 1
fi

echo "⏳ Run: $RUN_ID"
echo "⏱️ A acompanhar o build (podes aguardar)..."

# O gh run watch acompanha o build em tempo real no terminal
gh run watch "$RUN_ID"

echo ""
echo "🔎 A verificar o resultado do build..."
CONCLUSION=$(gh run view "$RUN_ID" --json conclusion -q ".conclusion")

if [ "$CONCLUSION" != "success" ]; then
  echo ""
  echo "❌ O BUILD FALHOU. A obter os logs de erro:"
  echo "---------------------------------------------------"
  gh run view "$RUN_ID" --log-failed 2>/dev/null | grep -A 15 -E "e: file:///|error:|Caused by|FAILURE: Build failed|Migration didn.t properly handle|IllegalStateException" | sed "s/^[0-9T:.-]*Z //" | head -200
  echo "---------------------------------------------------"
  echo "Ver completo em: https://github.com/deenioarhusdanieldapaz-ops/Lumiere-android/actions/runs/$RUN_ID"
  exit 1
fi

echo ""
echo "✅ Build com sucesso! A iniciar download do APK..."

# Download resiliente usando a API do GitHub e curl (que retoma o download se a rede cair)
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
TOKEN=$(gh auth token)
ARTIFACT_URL=$(gh api repos/$REPO/actions/runs/$RUN_ID/artifacts --jq '.artifacts[] | select(.name=="debug-apk") | .archive_download_url')

if [ -z "$ARTIFACT_URL" ]; then
    echo "❌ Artefato debug-apk não encontrado."
    exit 1
fi

echo "📥 A descarregar (modo resiliente: se a rede cair, ele retoma)..."
rm -rf temp-apk && mkdir -p temp-apk

if ! curl -sS -L -C - --retry 10 --retry-delay 5 --connect-timeout 30 -H "Authorization: token $TOKEN" -o temp-apk/debug-apk.zip "$ARTIFACT_URL"; then
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
echo "✅ APK guardado em: /sdcard/Download/lumiere-fase2.apk"
