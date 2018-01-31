#!/bin/bash
echo "Compilable Files"
find combined/ -mindepth 1 -type f -name 'compilable.json' | wc -l
echo "Version Diffs"
find combined/ -mindepth 1 -type f -name 'versionDiff.json' -and ! -empty | wc -l
echo "Projects which did not work"
find combined/ -mindepth 1 -type d '!' -exec test -e '{}/versionDiff.json' ';' -print | wc -l
echo "Empty Projects"
find combined/ -mindepth 1 -type f -empty | wc -l
