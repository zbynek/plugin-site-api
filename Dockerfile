
FROM jetty:9-alpine
COPY target/*.war /var/lib/jetty/webapps/ROOT.war
RUN java -jar $JETTY_HOME/start.jar --create-startd --approve-all-licenses
