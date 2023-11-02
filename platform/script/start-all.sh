#! /bin/bash
wd=$(cd `dirname $0` && pwd)
cd $wd

if [ -z "$(docker ps|grep gateway-mysql)" ] ; then
        echo "启动docker 容器"
        if [ "$(docker ps -a|grep gateway-mysql)" ]; then
            docker start gateway-mysql
        else
            mysql_ver=`docker images|grep mysql|awk '$2~/^8./{print $2}'`
            [ ${mysql_ver} ] && docker run -d -e MYSQL_ROOT_PASSWORD=123456 --name gateway-mysql -v /data/gateway-mysql:/var/lib/mysql -p 3307:3306 --restart=always mysql:${mysql_ver}
        fi
fi

for dir in `ls $wd` ; do
        if [[ $dir =~ "-" && -d "$wd/$dir" ]]; then
                bash $dir/start-gateway.sh
        fi
done