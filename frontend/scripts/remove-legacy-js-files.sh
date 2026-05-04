#!/usr/bin/env bash
set -euo pipefail
# Run from frontend root if you choose to merge instead of deleting src.
find ./src -type f \( -name '*.js' -o -name '*.jsx' \) -delete
