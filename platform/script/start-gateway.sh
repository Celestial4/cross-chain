#! /bin/bashwd=$(cd `dirname $0` && pwd)cd wdbc_dir_name=$(pwd|awk -F '/' '{print $NF}')blockchain_name=$(echo $bc_dir_name | awk -F '-' '{print $1}')check=$(ps -ef|grep java|awk '$9~/'"${bc_dir_name}"'/')if [[ -n $check ]]; then        echo "gateway-$blockchain_name has been started!"        exit 1fiecho "starting gateway-$blockchain_name...."java -Dspring.config.location=$wd/conf/ -cp 'app/*:lib/*:conf/*' com.crosschain.CrossChainGatewayApplication &>"gateway-$blockchain_name.log" &success_flag="Started CrossChainGatewayApplication"for (( i=0; i<10;i++ )); do        echo "starting ... please wait..." $((i+1))        sleep 1        success_info=$(grep "${success_flag}"<< EOF$(tail -n 1 gateway-${blockchain_name}.log)EOF)        if [ -n "$success_info" ]; then          echo "start gateway-${blockchain_name} successfully!"          exit 0        fidoneecho "tried to start gateway-$blockchain_name but failed, please check gateway-$blockchain_name.log"exit 1