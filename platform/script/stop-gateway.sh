#! /bin/bash
declare -A map
map=(
  [fabric]=3001
  [ca]=3002
  [bcos]=3003
  [hyperchain]=3004
  [ethereum]=3005
)

blockchain_name=$(pwd | awk -F '/' '{print $NF}' | awk -F '-' '{print $1}')

for i in "${!map[@]}"; do
        if [[ $i =~ ${blockchain_name} ]]; then
            index=$i
        fi
done

if [ -z "${index}" ]; then
    echo "check your deployment directory, make sure that name of the deployment directory compatible with chain name"
    exit 1
fi

pid=$(lsof -i :"${map[$index]}" | awk 'NR==2{print $2}')
if [ -n "$pid" ]; then
  echo "${index} gateway is running. pid:$pid"
else
  echo "${index} gateway is not running"
  exit 1
fi

kill -9 $pid

if [ $? == 0 ]; then
  echo "kill ${index}-gateway successfully"
  exit 0
else
  echo "kill ${index}-gateway failed"
  exit 1
fi