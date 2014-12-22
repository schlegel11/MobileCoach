# Welcome to the *MobileCoach*!

*MobileCoach* is the Open Source Behavioral Intervention Platform designed by **ETH Zurich**, the **University of St. Gallen** and the **Swiss Research Institute for Public Health and Addiction**.

© 2013-2015 [Health-IS Lab](http://www.health-is.ch) a joint initiative of the [Institute of Technology Management](http://www.item.unisg.ch) at [University of St. Gallen](http://www.unisg.ch) and the [Department of Management, Technology and Economics](http://mtec.ethz.ch) at [ETH Zurich](http://www.ethz.ch).   

For further information visit the *MobileCoach* Website at [https://www.mobile-coach.eu](https://www.mobile-coach.eu)!

## Team of the release version

### Health-IS Lab

* **Andreas Filler** - afiller (AT) ethz (DOT) ch
* **Tobias Kowatsch** - tobias (DOT) kowatsch (AT) unisg (DOT) ch
* **Jost Schweinfurther** - jschweinfurther (AT) ethz (DOT) ch
* **Prof. Dr. Elgar Fleisch** - efleisch (AT) ethz (DOT) ch

### Swiss Research Institute for Public Health and Addiction

* **Dr. Severin Haug** - severin (DOT) haug (AT) isgf (DOT) uzh (DOT) ch
* **Raquel Paz Castro** - raquel (DOT) paz (AT) isgf (DOT) uzh (DOT) ch

## License

License information can be found in the [LICENSE.txt](LICENSE.txt) file in the root folder of this project.

---

# *MobileCoach* usage information

## MobileCoach components

The *MobileCoach* system consists of several components, which are available in several repositories. The components are described in the following. Working files (i.e. example website templates, example interventions) are bundled with the components.

### MobileCoach (MC)

The *MobileCoach* component is the main component of the *MobileCoach*. It provides the whole functionality to develop and execute interventions including screenings surveys, feedbacks, monitoring and results export.

### MobileCoach Web (MCW)

The *MobileCoach Website* component provides functionalities for short URL redirects as well as the feature to provide a small website with CMS functionalites including statistics created by the *MobileCoach*.

### FileServletWrapper

The *FileServletWrapper* is an optional component for the MobileCoach system. To be used, it needs to be added to the library path of the *MobileCoach* system, e.g. by copying the created .jar file to the WEB-INF/lib folder.

## Prerequisites

### Development environment

* Java (7+) SDK 
* Web application server (e.g. Apache Tomcat 7)
* Web application server configured to listen on port 80 (HTTP) and 443 (HTTPS) with a valid or self-signed certificate
* MongoDB installation with enabled user authentication, a created database and configured user
* [Eclipse IDE for Java EE Developers](https://eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/keplersr2) (Project files included in repository)
* [Project Lombok](http://projectlombok.org) (Can be installed using a double-click on the **.jar** file)
* [Vaadin Eclipse Plugins](http://vaadin.com/eclipse) (required to compile the widget and themes)
* [Apache Ivy Eclipse Integration](http://ant.apache.org/ivy/ivyde/) (already included in some distributions)

### Server environment

* Java (7+) SDK 
* Web application server (e.g. Apache Tomcat 7)
* Web application server configured to listen on port 80 (HTTP) and 443 (HTTPS) with a valid certificate
* MongoDB installation with enabled user authentication, a created database and configured user

## Basic configuration

The whole **MobileCoach** can be configured using one configuration file **configuration.properties** which is included in the repository containing example values. All folders mentioned in this file should be created before the first startup of the **MobileCoach** system.

Your adjusted copy of the **configuration.properties** can be placed anywhere on your system, but the file need to be referenced as system property with the name **mc.configuration**. Otherwise the default configuration from the **Constants.java** will be used - and you won't like it.

To define the system property you can start your java runtime with the following parameter:
	
	-Dmc.configuration="/PATH_TO_YOUR_FILE/configuration.properties"

Extend your tomcat configuration files or startup scripts with this setting.

**Suggestion:**   
Ensure that your Tomcat installation is allowed to read and write to all folders configured in the **configuration.properties**

**Suggestion:**   
Never activate **automaticallyLoginAsDefaultAdmin** on your server!!!! It's quite helpful while development, because you can login to the **MobileCoach** by only clicking the **login** button.

**Suggestion:**   
Change the **defaultAdminPassword** before the first startup. The user **admin** will be created with your defined password. You can always change the password afterwards in the backend.

**Suggestion:**   
You lost the password of the user **admin**? Shutdown the system, rename the user **admin** in the database collection **Author** to another unique name (never delete anything) and startup the system again.

After deploying a (newly created) web archive (**.war**) to your server you can access the **MobileCoach** on your server at
	
	https://YOURDOMAIN.WHATEVER/MC/admin

**Suggestion:**   
Use the WAR building functionalities provided by **Eclipse WTP** in the **"Export->WAR file"** context menu when clicking on a web project.

## Important URLs

The *MobileCoach* administration:

	https://YOURDOMAIN.WHATEVER/MC/admin

A listing of your existing screening surveys (if activated):

	https://YOURDOMAIN.WHATEVER/MC/

## Screening survey and feedback templates

The templates used for screening surveys and feedbacks are HTML templates provided in the folder defined in the **configuration.properties** in the variable **templatesFolder**. The templates are automatically recognized at system startup when following these rules:

* Each template has a unique folder name
* Each template has a **index.html** file

For further information on the template engine have a look at the documentation of the [mustache](http://mustache.github.io/mustache.5.html) template engine.

**Suggestion:**   
When the debug parameter **IS_LIVE_SYSTEM** in the file **Constants.java** is set to **true** the templates are cached. It's therefore suggested to set this value to **false** but only on development systems.

## Backend user guide

An extended backend user guide will be published in the following months.

### Version 1.0.0 (Build 20141222)

