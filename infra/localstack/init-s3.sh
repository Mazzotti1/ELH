#!/bin/bash
# Cria o bucket S3 local no LocalStack ao subir o container
echo "→ Criando bucket ELH no LocalStack..."
awslocal s3 mb s3://elh-media
awslocal s3 mb s3://elh-media-dev

# Estrutura de prefixos (S3 não tem pastas, mas prefixos ajudam organizar)
# originals/{guildId}/{mediaId}/arquivo.ext
# thumbnails/{guildId}/{mediaId}/thumb.jpg

echo "✓ Buckets criados:"
awslocal s3 ls
