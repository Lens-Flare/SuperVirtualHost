#!/bin/sh

cd ${0%/*}

mvn install:install-file -Dfile=naturalcli-1.2.3.jar -DgroupId=naturalcli -DartifactId=naturalcli -Dversion=1.2.3 -Dpackaging=jar