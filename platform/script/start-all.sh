#! /bin/bash
wd=$(cd `dirname $0` && pwd)
cd $wd

if [[ -z '$(docker ps|grep "gateway-mysql")' ]]; then
        docker start gateway-mysql
fi

for dir in `ls $wd` ; do
        if [[ $dir =~ "-" && -d "$wd/$dir" ]]; then
                bash $dir/start-gateway.sh
        fi
done