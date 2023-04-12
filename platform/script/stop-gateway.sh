#! /bin/bash
pid=$(lsof -i :3001 | awk 'NR==2{print $2}')
if [ -n "$pid" ]; then
	echo "fabric gateway is running. pid:$pid"
else
	echo "fabric gateway is not running"
	exit 0
fi

kill -9 $pid
if [ $! == 0 ]; then
	echo "kill fabric gateway successfully"
else
	echo "something goes wrong"
fi