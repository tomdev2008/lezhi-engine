#!/bin/sh

CLASSPATH=conf:lezhi-api.jar
for f in lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f
done
java -server -Dfile.encoding=UTF-8 -Xms1G -Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:MaxNewSize=1024M -XX:MaxPermSize=256M -XX:+DisableExplicitGC -cp $CLASSPATH com.buzzinate.lezhi.index.api.IndexServer > /dev/null 2>&1 &
