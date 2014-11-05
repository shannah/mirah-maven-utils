#!/bin/sh
mvn gpg:sign-and-deploy-file -e \
 -DpomFile=mirahc-pom.xml \
 -Dfile=mirahc.jar \
 -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
 -DrepositoryId=ossrh
mvn gpg:sign-and-deploy-file -e \
 -DpomFile=mirahc-pom.xml \
 -Dfile=mirahc-sources.jar \
 -Dclassifier=sources \
 -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
 -DrepositoryId=ossrh
mvn gpg:sign-and-deploy-file -e \
 -DpomFile=mirahc-pom.xml \
 -Dfile=mirahc-javadoc.jar \
 -Dclassifier=javadoc \
 -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
 -DrepositoryId=ossrh
#mvn gpg:sign-and-deploy-file \
# -DpomFile=pom.xml \
# -Dfile=mirahc.jar \
# -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
# -DrepositoryId=ossrh
mvn gpg:sign-and-deploy-file \
 -DpomFile=mirahc-pom.xml \
 -Dfile=LICENSE \
 -Dclassifier=license \
 -Dpackaging=txt \
 -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ \
 -DrepositoryId=ossrh
