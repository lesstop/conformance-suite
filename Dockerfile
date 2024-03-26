FROM openjdk:17-jdk-slim
COPY target/fapi-test-suite.jar /server/
RUN echo 'application/javascript js es mjs' > /etc/mime.types
ENV BASE_URL https://localhost:8443
ENV MONGODB_HOST mongodb
ENV JAVA_EXTRA_ARGS=
EXPOSE 8080
ENTRYPOINT java \
  -D"fintechlabs.base_url=${BASE_URL}" \
  -D"spring.data.mongodb.uri=mongodb://${MONGODB_HOST}:27017/test_suite" \
  ${JWKS:+-D"fintechlabs.jwks=${JWKS}"} \
  ${SIGNING_KEY:+-D"fintechlabs.signingKey=${SIGNING_KEY}"} \
  -D"oidc.google.clientid=${OIDC_GOOGLE_CLIENTID}" \
  -D"oidc.google.secret=${OIDC_GOOGLE_SECRET}" \
  -D"oidc.gitlab.clientid=${OIDC_GITLAB_CLIENTID}" \
  -D"oidc.gitlab.secret=${OIDC_GITLAB_SECRET}" \
  -D"docusign.userid=${DOCUSIGN_USER_ID}" \
  -D"docusign.clientid=${DOCUSIGN_CLIENT_ID}" \
  -D"docusign.privatekey=${DOCUSIGN_PRIVATE_KEY}" \
  $JAVA_EXTRA_ARGS \
 -jar /server/fapi-test-suite.jar \
 -Djdk.tls.maxHandshakeMessageSize=65536

