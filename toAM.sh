#!/bin/bash -eux
cp policyevaluation-0.0.1-SNAPSHOT.jar /opt/tomcat/webapps/openam/WEB-INF/lib/
cp translation.json /opt/tomcat/webapps/openam/XUI/locales/en/
chown tomcat:root /opt/tomcat/webapps/openam/WEB-INF/lib/policyevaluation-0.0.1-SNAPSHOT.jar
chown tomcat:root /opt/tomcat/webapps/openam/XUI/locales/en/translation.json
initctl restart tomcat


