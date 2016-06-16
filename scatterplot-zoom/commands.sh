#!/bin/bash
ssh ops@mr-0xc8
ssh ops@172.16.2.148

cd h2o-3.9.1.3469/

# @micah
java -Xmx10g -jar ./h2o.jar -port 55555 1> h2o.out 2> h2o.err &

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