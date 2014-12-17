# Welcome to the *MobileCoach*!

*MobileCoach* is the novel Open Source Behavioral Intervention Platform designed by **ETH Zurich**, the **University of St. Gallen** and the **Swiss Research Institute for Public Health and Addiction**.

&copy; 2014-2015 [Health-IS Lab](http://www.health-is.ch) at the [Institute of Technology Management](http://www.item.unisg.ch) and [University of St. Gallen](http://www.unisg.ch)   

For further information visit the *MobileCoach* Website at [https://www.mobile-coach.eu](https://www.mobile-coach.eu)!

## Team of the Release Version

* **Andreas Filler** - afiller (AT) ethz (DOT) ch
* **Tobias Kowatsch** - tkowatsch (AT) ethz (DOT) ch
* **Dr. Severin Haug** - severin (DOT) haug (AT) isgf (DOT) uzh (dot) ch
* **Jost Schweinfurther** - jostsch (AT) gmail (DOT) com
* **Prof. Dr. Elgar Fleisch** - efleisch (AT) ethz (DOT) ch

## License

License information can be found in the [LICENSE.txt](LICENSE.txt) file in the root folder of this project.

---

# Usage information

## MobileCoach components

The *MobileCoach* system consists of several components, which are available in several repositories. The components are described in the following. Working files (i.e. example website templates, example interventions) are bundled with the components.

### MobileCoach (MC)

### MobileCoach Web (MCW)

### FileServletWrapper

The **FileServletWrapper** is an optional component for the MobileCoach system. To be used it needs to be added to the library path of the *MobileCoach* system, e.g. using the build path settings in the **Eclipse** development environment.


........

## How to use MC?

Mustache template engine http://mustache.github.io/mustache.5.html

### Templates

* Are in the subfolder "templates" of the data folder
* Always have a index.html
* By using the field OptionalLayoutAttribute another variable can be set per slide to adjust the layout based on a string
* Available template fields are: ...

## How to improve or extend MC?

Eclipse specific:

Install Lombok first by double-clicking lombok.jar



### How to configure MC
-Dmhc.configuration="/Users/andreas/Documents/Projekte/MC/Development/workspace/MC/configuration.properties"