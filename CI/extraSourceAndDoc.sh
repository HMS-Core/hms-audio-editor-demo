#!/bin/bash
str=`ls| grep aar$|sed -n '1p'`
name=${str%.*}
touch ${name}-sources.jar
sha1sum ${name}-sources.jar | awk '{print $1}' > ${name}-sources.jar.sha1
touch ${name}-javadoc.jar
sha1sum ${name}-javadoc.jar | awk '{print $1}' > ${name}-javadoc.jar.sha1