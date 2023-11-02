#!/bin/bash
function print_error() {
  cat <<EOF
    $1
    输入ctrl+c 退出添加配置。
EOF
}

function input_chain_name() {
  while true; do
    read -p "请输入要添加的区块链网关名：-> " chain_name

    if [[ "$(grep "$chain_name" conf/config.properties)" ]]; then
      print_error "已添加$chain_name网关信息，请勿重复添加！"
    else
      return
    fi
  done
}

input_ip_addr() {
  while true; do
    read -p "请输入要添加的区块链网关所属的可寻址IP地址：-> " ip_addr

    if [[ ! "$ip_addr" =~ ^((25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})\.){3}(25[0-5]|2[0-4][0-9]|1?[0-9]{1,2})$ ]]; then
      print_error "输入ip地址格式不正确，请重新输入（无前导0）！"
    else
      return
    fi
  done
}

input_g_port() {
  while true; do
    read -p "请输入要添加的区块链网关的端口地址：-> " g_port

    if [[ ! "$g_port" =~ ^([1-9][0-9]{4}|[1-9][0-9]{3}|[1-9][0-9]{2}|[1-9][0-9]{1}|[1-9])$ || ($g_port -gt 65535) ]]; then
      print_error "输入端口地址格式不正确，请重新输入（无前导0）！"
    else
      return
    fi
  done
}

input_in_port() {
  while true; do
    read -p "请输入要添加的区块链接入组件的端口地址：-> " in_port

    if [[ ! "$in_port" =~ ^([1-9][0-9]{4}|[1-9][0-9]{3}|[1-9][0-9]{2}|[1-9][0-9]{1}|[1-9])$ || ($in_port -gt 65535) ]]; then
      echo "输入端口地址格式不正确，请重新输入（无前导0）！"
    else
      return
    fi
  done
}

if [[ ! -f "conf/config.properties" ]]; then
  read -p "请输入即将要部署的网关链名：-> " dep_name
  echo "self=$dep_name" >conf/config.properties
  echo "config文件创建成功！"
fi

input_chain_name
input_ip_addr
input_g_port
input_in_port

file=conf/config.properties
echo "gateway-$chain_name=$ip_addr:$g_port" >> $file
echo "$chain_name=$ip_addr:$in_port" >> $file

sort -t = -k 2n $file -o $file