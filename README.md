Physalix enrollment system
========

[![Build Status](https://travis-ci.org/physalix-enrollment/physalix.svg?branch=master)](https://travis-ci.org/physalix-enrollment/physalix)


Prerequisites
--------
* PostgreSQL Database 8.x or better
* Maven 2.x or better
* Java 1.6 or better
* free port ```:8080```


How-to get it run
--------
* place your configuration files under ```${user.home}/.jetty/extendedClasspath/physalix/``` to override default settings of physalix
* for production instances use ```${catalina.home}/physalix/``` as configuration directory
* adapt configuration files
  * ```database.properties``` - the database connection information
  * ```roles.properties``` - insert your ldap username to get administrative access
  * ```mail.properties``` - insert your mail server connection information
  * ```naming.properties``` - your user credentials and ldap specific information
  * ```fieldMapping.properties``` - map your ldap fields to our database schema
  * ```template.properties``` - update ```template.path``` to the absolute path of your template folder
  * ```velocity.properties``` - update ```file.resource.loader.path``` to the absolute path of your template folder
* Build and start physalix:

```
$> cd awp
$> mvn install -Dskip.test
$> cd AdminGui
$> mvn jetty:run
```
* your administrative GUI is now available under [http://localhost:8080/AdminGui](http://localhost:8080/AdminGui)
