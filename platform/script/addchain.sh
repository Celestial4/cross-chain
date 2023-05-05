#!/bin/bash
if [ $# -lt 1 ]; then
        echo "miss connection info to add"
        exit 1
fi

info_add=$1

cnt=0
file_conf="conf/config.properties"

for i in $(ls); do
        if [ -d $i ]; then
                cd ${i}
                echo "$1" >> conf/config.properties
                if [[ $? -eq "0" ]]; then
                        ((cnt++))
                fi
                cd -
        fi
done
echo "$cnt gateway(s) has been updated"