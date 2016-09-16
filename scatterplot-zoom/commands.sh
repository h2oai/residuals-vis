#!/bin/bash
ssh ops@mr-0xc8
ssh ops@172.16.2.148

ssh ops@mr-0xc1
ssh ops@172.16.2.141

# copy a directory from local machine to server
scp -r aggregator-h2o ops@mr-0xc1:/home/ops/

cd h2o-3.9.1.3469/

# start server in background and keep alive
nohup http-server -p 8989 &
exit

# find process running on port
lsof -i :8989

# start server with pm2
pm2 start /usr/bin/http-server . --name residuals -- -p 8989 -d false

# stop process, stop server running in the background
kill #<pid>

# @micah
java -Xmx10g -jar ./h2o.jar -port 55555 1> h2o.out 2> h2o.err &

# long-lived on the server, with a name
nohup java -Xmx10g -jar h2o.jar -port 55555 -name H2ODemo 1> h2o.out 2> h2o.err &

# local
java -Xmx4g -jar ./h2o.jar -port 55555 1> h2o.out 2> h2o.err &

# @markc
# java -Xmx10g -jar ./h2o.jar -port 54321 1> h2o.out 2> h2o.err &

# @markc on port 55555
# this allows us to use the JDBC connector to memsql
java -Xmx10g -cp h2o.jar:/usr/share/java/mysql-connector-java.jar water.H2OApp -port 55555 1> h2o.out 2> h2o.err &


# then we can open h2o flow at this url:
# http://mr-0xc8:55555/

# open chrome with cross-origin protection disabled
# open -a Google\ Chrome --args --disable-web-security --user-data-dir

# open chrome canary with cross-origin protection disabled
# open -a Google\ Chrome\ Canary --args --disable-web-security --user-data-dir