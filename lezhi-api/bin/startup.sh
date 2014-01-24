#!/bin/sh

CLASSPATH=conf:lezhi-api.jar
for f in lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f
done
java -server -Dfile.encoding=UTF-8 -Xms4G -Xmx4G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:MaxNewSize=1024M -XX:MaxPermSize=256M -XX:+DisableExplicitGC -cp $CLASSPATH com.buzzinate.lezhi.api.RecommendServer > /dev/null 2>&1 &
