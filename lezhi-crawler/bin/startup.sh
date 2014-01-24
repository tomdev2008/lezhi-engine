#!/bin/sh

CLASSPATH=conf:lezhi-crawler.jar
for f in lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f
done
java -server -Dfile.encoding=UTF-8 -Xmx2G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:MaxNewSize=256M -XX:MaxPermSize=256M -cp $CLASSPATH "$@"
