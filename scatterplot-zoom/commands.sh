ssh ops@mr-0xc8

cd h2o-3.9.1.3469/

java -Xmx10g -jar h2o.jar -port 55555 1> h2o.out 2> h2o.err &

# then we can open h2o flow at this url:
# http://mr-0xc8:55555/

# open chrome with cross-origin protection disabled
# open -a Google\ Chrome --args --disable-web-security --user-data-dir