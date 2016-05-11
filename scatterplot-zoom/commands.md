ssh ops@mr-0xc8

java -Xmx10g -jar h2o.jar -port 55555 1> h2o.out 2> h2o.err &

#### open chrome with cross-origin protection disabled

open -a Google\ Chrome --args --disable-web-security --user-data-dir