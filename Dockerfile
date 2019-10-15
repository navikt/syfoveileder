FROM navikt/java:8-appdynamics
ENV APPD_ENABLED=true
COPY build/libs/*.jar app.jar

#ENV SRVDIALOGFORDELER_CERT_KEYSTORE="$NAIS_SECRETS/srvdialogfordeler_cert_keystore"
#ENV NAIS_SECRETS="/var/run/secrets/naisd.io/"

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote \
               -Dhttps.proxyHost=webproxy-nais.nav.no \
               -Dhttps.proxyPort=8088 \
               -Dhttp.nonProxyHosts=*.adeo.no|*.preprod.local"
