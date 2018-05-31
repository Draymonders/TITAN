#!/bin/bash

#每台机器启动的agent节点数量
AGENT_NUM=2

#所有agent机器地址
AGENT_ADDRESS=('10.180.4.231' '10.180.4.232' '10.180.4.233' '10.180.4.234')

#停止所有机器上的agents节点
for address in ${AGENT_ADDRESS[*]}
do
        nohup ssh root@$address "cd /root/titan/titan-agent*; sh shutdown.sh &" >/dev/null 2>log &
done
sleep 5
echo '所有机器上的agent节点已经全部停止'

