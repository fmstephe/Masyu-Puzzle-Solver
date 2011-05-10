#!/bin/sh
#$INPUT_DIR = $1
#$OUTPUT_DIR = $2
#$PROCS = $3
#$TIMEOUT = $4
echo "/opt/java/latest/bin/java -jar -server -Xms20g -Xmx20g masyu.jar $1 $2 $3 $4"
/opt/java/latest/bin/java -jar -server -Xms64g -Xmx64g masyu.jar $1 $2 $3 $4
