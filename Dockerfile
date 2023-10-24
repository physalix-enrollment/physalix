FROM tomcat:9-jdk21

COPY tomcat /usr/local/tomcat
COPY AdminGui/target/AdminGui*.war /usr/local/tomcat/webapps/AdminGui.war
COPY UserGui/target/UserGui*.war /usr/local/tomcat/webapps/UserGui.war

# create certificate folder to put keystore
RUN mkdir -p /usr/local/tomcat/certificate

# create config folder available in classpath
RUN mkdir -p /usr/local/tomcat/lib/physalix
#COPY phx-local /usr/local/tomcat/lib/physalix
