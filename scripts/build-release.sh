#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
: "${IMAGE_PREFIX:?Set authenticated registry image prefix}"
revision=$(git rev-parse HEAD)
if test -n "$(git status --porcelain --untracked-files=normal -- . ':!.agents/skills/**')"; then
    echo 'Commit and validate the source before building a release.' >&2
    exit 1
fi
docker buildx build --platform linux/amd64,linux/arm64 --push -t "$IMAGE_PREFIX/backend:$revision" .
docker buildx build --platform linux/amd64,linux/arm64 --push --build-arg VITE_API_BASE_URL= --build-arg VITE_DEMO_MODE=false --build-arg VITE_REQUIRE_LIVE_API=true -t "$IMAGE_PREFIX/frontend:$revision" frontend
printf 'Release images built from commit %s. Public deployment gates still apply.\n' "$revision"
