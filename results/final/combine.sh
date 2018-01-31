#!/bin/bash
databases="database/*"
mkdir -p combined
for d in $databases
do
    base=$(basename "$d" .json)
    folder=$(find extracted/ -mindepth 2 -maxdepth 3 -type d -name "$base")
    if [[ $folder = "" ]]
    then 
    folder=$(find extracted/ -mindepth 2 -maxdepth 3 -type d -name "$base*")
    fi
    
    newFolder="combined/"$base
    mkdir -p $newFolder
    cp $d $newFolder"/database.json"
    cp $folder"/compilable.json" $newFolder
    if [ -e $folder"/versionDiff.json" ]
      then cp $folder"/versionDiff.json" $newFolder
    fi
    
done
