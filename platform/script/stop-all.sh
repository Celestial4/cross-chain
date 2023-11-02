#! /bin/bash
wd=$(cd `dirname $0` && pwd)
cd $wd

for dir in `ls $wd` ; do
        if [[ $dir =~ "-" && -d "$wd/$dir" ]]; then
                bash $dir/stop-gateway.sh
        fi
done