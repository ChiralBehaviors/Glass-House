Glass House
===========

A REST based JMX Command and Control system.  This process uses Nexus service discovery to discover and acquire JMX connnectors to other Java processes.  Select MBeans are proxied in the local process, giving the illusion of a single, unified MBeanServer.

Glass House provides a REST interface to the underlying JMX MBeanServer, allowing querying or manipulation of individual MBeans, or aggregrate actions that allow operations across sets of nodes under the control of the Glass House process.

Glass House requires Java 7 and Maven 3.x to build.  To build, cd to root directory of this project and:

    mvn clean install
    
The license for Glass House is Apache 2.0