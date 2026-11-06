#!/bin/bash
echo "🚀 1. A enviar código para o GitHub..."
git add .
git commit -m "Auto-update: Nova versão da app"
git push origin main

echo "⏳ 2. A aguardar compilação da Cloud (Isto demora 1-2 min)..."
gh run watch

echo "📥 3. A descarregar o novo APK..."
rm -rf temp-apk
mkdir -p temp-apk
# Descarrega o artefacto mais recente (o gh escolhe automaticamente o último)
gh run download -n debug-apk -D temp-apk

echo "📱 4. A abrir o instalador no telemóvel..."
cp temp-apk/app-debug.apk ~/storage/downloads/lumiere-m01.apk
# O comando termux-open abre o ficheiro com a app padrão (Instalador de Pacotes)
termux-open ~/storage/downloads/lumiere-m01.apk

echo "✅ Tudo pronto! Só tens de clicar em 'Atualizar' ou 'Instalar' no ecrã."
