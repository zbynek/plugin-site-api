FROM jetty:9-alpine

USER root

# Bring our Java backend app over for Jetty
COPY *.war /var/lib/jetty/webapps/ROOT.war

RUN mkdir -p /home/jetty/.yarn
RUN chown -R jetty /home/jetty

WORKDIR $JETTY_BASE

CMD /docker-entrypoint.sh
