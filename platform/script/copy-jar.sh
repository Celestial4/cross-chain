#! /bin/bash

wd=~/gateways
file="gateway-2.0.jar"

for dir in $(ls $wd); do
        if [[ $dir =~ "-" && -d $wd/$dir ]]; then
                cp -u $file $wd/$dir/app
        fi
done