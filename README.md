# Welcome to the *MobileCoach*!

*MobileCoach* is the Open Source Behavioral Intervention Platform designed by **ETH Zurich**, the **University of St. Gallen** and the **Swiss Research Institute for Public Health and Addiction**.

© 2013-2017 [Center for Digital Health Interventions, Health-IS Lab](http://www.c4dhi.org) a joint initiative of the [Institute of Technology Management](http://www.item.unisg.ch) at [University of St. Gallen](http://www.unisg.ch) and the [Department of Management, Technology and Economics](http://mtec.ethz.ch) at [ETH Zurich](http://www.ethz.ch).   

For further information visit the *MobileCoach* Website at [https://www.mobile-coach.eu](https://www.mobile-coach.eu)!

## Team of the release version

### Center for Digital Health Interventions, Health-IS Lab

* **Andreas Filler** - afiller (AT) ethz (DOT) ch
* **Dr. Tobias Kowatsch** - tobias (DOT) kowatsch (AT) unisg (DOT) ch
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

* Java (8 or newer) SDK 
* Web application server (Apache Tomcat 8 or newer/compatible)
* Web application server configured to listen on port 80 (HTTP) and 443 (HTTPS) with a valid or self-signed certificate
* MongoDB (2.6.x or newer, but lower than 3.x.x) installation with enabled user authentication, a created database and configured user
* [Eclipse IDE for Java EE Developers](https://eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/keplersr2) (Project files included in repository)
* [Apache Ivy Eclipse Integration](http://ant.apache.org/ivy/ivyde/) (already included in some distributions)
* [Project Lombok Eclipse Integration](http://projectlombok.org) (Can be installed using a double-click on the **.jar** file)
* [Vaadin Eclipse Plug-ins](http://vaadin.com/eclipse) (required to compile the widget and themes)
* [ResourceBundle Editor Eclipse Plug-in](http://essiembre.github.io/eclipse-rbe/) (optional, but recommended for properties file editing)

### Server environment

* Java (8 or newer) SDK 
* Web application server (Apache Tomcat 8 or newer/compatible)
* Web application server configured to listen on port 80 (HTTP) and 443 (HTTPS) with a valid certificate
* MongoDB (2.6.* or newer, but lower than 3.*) installation with enabled user authentication, a created database and configured user

## Basic configuration

The whole **MobileCoach** can be configured using one configuration file **configuration.properties** which is included in the repository containing example values. All folders mentioned in this file should be created before the first startup of the **MobileCoach** system.

Your adjusted copy of the **configuration.properties** can be placed anywhere on your system, but the file need to be referenced as system property with the name **[YOUR CONTEXT PATH in lower case].configuration** (or **virtualservername.xyz.[YOUR CONTEXT PATH in lower case].configuration** if you run several virtual servers). Otherwise the default configuration from the **Constants.java** will be used - and you won't like it.

E.g., to define the system property for a **MobileCoach** instance running at your server in the path "/MC" (deployed using a MC.war) you can start your java runtime with the following parameter:
	
	-Dmc.configuration="/PATH_TO_YOUR_FILE/configuration.properties"

Extend your tomcat configuration files or startup scripts with this setting.

If you are using Tomcat8.5 or newer, with the support for virtual servers, then you can provide several configuration files for each server by adjusting the configuration parameter as follows:

	-Dvirtualservername.xyz.mc.configuration="/PATH_TO_YOUR_FILE/configuration.properties"

Also the following should be added to avoid encoding problems on several operating systems and/or RAM problems:

	-server -Dfile.encoding=UTF8 -Djava.awt.headless=true -Xms2g -Xmx2g

You should also add the following parameters to avoid problems with the Log4J logging system:

	-Dmc_logging_folder=/tmp -Dmc_logging_console_level=INFO -Dmc_logging_rolling_file_level=INFO

**Suggestion:**   
Ensure that your Tomcat installation is allowed to read and write to all folders configured in the **configuration.properties**

**Suggestion:**   
Never activate **automaticallyLoginAsDefaultAdmin** on your server!!!! It's quite helpful while development, because you can login to the **MobileCoach** by only clicking the **login** button.

**Suggestion:**   
Change the **defaultAdminPassword** before the first startup. The user **admin** will be created with your defined password. You can always change the password afterwards in the backend.

**Suggestion:**   
You lost the password of the user **admin**? Shutdown the system, rename the user **admin** in the database collection **Author** to another unique name (never delete anything) and startup the system again.

**Suggestion:**   
*Mobile Coach* is tested with Apache Tomcat 8(.5)

After deploying a (newly created) web archive (**.war**) to your server you can access the **MobileCoach** on your server at
	
	https://YOURDOMAIN.WHATEVER/YOUR_CONTEXT_PATH/admin

**Suggestion:** Use the provided **build.xml** within **Eclipse** to create the **.war** file.

E.g.:

	MC.war

## Important URLs

The *MobileCoach* administration:

	https://YOURDOMAIN.WHATEVER/YOUR_CONTEXT_PATH/admin

E.g.:
	
	https://mydomain.com/MC/admin
	
A listing of your existing screening surveys (if activated):

	https://YOURDOMAIN.WHATEVER/YOUR_CONTEXT_PATH/
	
E.g.:

	https://mydomain.com/MC/

## Screening survey and feedback templates

The templates used for screening surveys and feedbacks are HTML templates provided in the folder defined in the **configuration.properties** in the variable **templatesFolder**. The templates are automatically recognized at system startup when following these rules:

* Each template has a unique folder name
* Each template has a **index.html** file

For further information on the template engine have a look at the documentation of the [mustache](http://mustache.github.io/mustache.5.html) template engine.

**Suggestion:**   
When the debug parameter **IS_LIVE_SYSTEM** in the file **Constants.java** is set to **true** the templates are cached. It's therefore suggested to set this value to **false** but only on development systems.

## Backend user guide

An extended backend user guide will be published in the following months.

### Version @@VERSION@@

## Release notes
### 1.6.0

**Enhancements**

* Dashboards provide a way to give teachers, caretakers, service personal etc. (non-participants) a way to have look at the current status of participants within an intervention
* Specific REST endpoints allow the retrieval of accumulated group/intervention values for the dashboard
* The variable uniqueness check allows to check variables across interventions for uniqueness
* Intervention starting days can now be definied dynamically
* New rules comparators to better work with "select many" variables

**Changes**

* Decimal based user inputs are now handled the same way independent of the usage of "," or "." as decimal separator
* Participant variables are now cached in memory to improve read access performance

**Bugfixes**

* Several minor bugfixes in the backend and UI


### 1.5.0

**Enhancements**

* Supervisors can now be set for participants as alternative message recipients
* Supervisors can be used in rules to receive messages under specific conditions

**Changes**

* The following configuration parameter have been added or replaced by more generic onces - You should adjust your configuration files accordingly:
	* mailSubjectStartsWith --> now smsMailSubjectStartsWith
	* emailSubjectForParticipant (new) 
	* emailSubjectForSupervisor (new)
	* emailFrom (new)

**Bugfixes**

* Bugfix for problems with daylight saving time

### 1.4.3

**Enhancements**

* Monitoring can be switched for participants while surveys are active

**Changes**

* none

**Bugfixes**

* Bugfix for variables with duplicate timestamps (which could occur under very rare conditions)

### 1.4.2

**Enhancements**

* Report generator to validate the modelled intervention
* Several small enhancements

**Changes**

* none

**Bugfixes**

* Automatic image rotation for iOS images based on EXIF information
* Many bugfixes for the features introduced in 1.4.0-1.4.1
* Bugfix for dropping a rule on itself, which could lead to cyclic rules

### 1.4.1

**Enhancements**

* More compact user interface (saves space on screen)
* Duplication for monitoring messages

**Changes**

* none

**Bugfixes**

* Many bugfixes for the features introduced in 1.4.0

### 1.4.0

**Enhancements**

* Support for interventions with several languages (which can be defined for a system)
* Variables with group support (sharing for whole intervention or group)
* REST interface for dynamic variable access with:
	* Variable based security system (to ensure privacy for private variables)
	* Bulk retrieval
	* Participant media upload
	* Average calculation
	* Credits collection system
* Surveys at any time (which can be referenced in messages)
* Support for virtual server names (e.g. in Tomcat 8.5 or newer)

**Changes**

* Optimized for Java 8 and Tomcat 8+
* The configuration parameter **feedbackLinkingBaseURL** has bee* n replaced by a generic **surveyLinkingBaseURL** - You should adjust your configuration files accordingly

**Bugfixes**

* Many small UI improvements
* Many small source code improvements

### 1.3.1

**Enhancements**

* New JVM argument **-Dmc_logging_rolling_file_level=INFO** and configuration paramter **loggingRollingFileLevel** to define logging level of rolling file appender - you should adjust your installation accordingly 

**Bugfixes**

* Fixing date calculations for 2-digit years entered by participants

### 1.3.0

**Enhancements**

* New Vaadin version for better browser compatibility

**Bugfixes**

* Fixing table sort parent problem for monitoring (reply) rules

