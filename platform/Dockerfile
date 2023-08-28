FROM java:openjdk-8u111-jre
WORKDIR /gateway
COPY ./lib lib/
COPY ./conf conf/
COPY st*.sh .
COPY ./app app/
EXPOSE 3001
CMD ["java","-Dspring.config.location=conf/", "-cp", "app/*:lib/*:conf/*", "com.crosschain.CrossChainGatewayApplication", "&>gateway.log","&"]