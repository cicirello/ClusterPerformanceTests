#!/bin/bash
cd $HOME/lib
export CLASSPATH=.:$HOME/lib:$HOME/lib/algengine.jar
rmiregistry &
if [ ! -f server.policy ]; then
    printf 'grant {\n    permission java.security.AllPermission;\n};\n' > server.policy
fi
java -Djava.rmi.server.hostname=$HOSTNAME.local -Djava.security.policy=server.policy -jar algengine.jar 256 &

