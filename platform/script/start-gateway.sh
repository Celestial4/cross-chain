#! /bin/bash
cur_dir=$(pwd)

blockchain_name=$(pwd|awk -F '/' '{print $NF}'|awk -F '-' '{print $1}')

echo "starting gateway-$blockchain_name...."

java -Dspring.config.location=$cur_dir/conf/ -cp 'app/*:lib/*:conf/*' com.crosschain.CrossChainGatewayApplication &>gateway-$blockchain_name.log &
sleep 5

success_info=$(tail -n 1 gateway-${blockchain_name}.log |grep Started)

if [ -n "$success_info" ]; then
  echo "start gateway-${blockchain_name} successfully!"
else
  echo "something goes wrong when start gateway, please check gateway-$blockchain_name.log"
fi