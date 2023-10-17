#! /bin/bash
wd=$(cd `dirname $0` && pwd)
cd $wd

file=$(find $wd -name "*.jar")

for dir in $(ls "../"); do
        if [[ $dir =~ "-" && -d $wd/../$dir ]]; then
                cp -u $file $wd/../$dir/app
        fi
done