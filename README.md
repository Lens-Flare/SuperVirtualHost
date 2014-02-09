SuperVirtualHost
================

Blah blah

Building SVH
------------
Execute `lib/install.sh` or otherwise get naturalcli-1.2.3.jar into your maven
repo. Run `mvn`.

Using SVH
---------
The base/parent project is set up to package each subproject as two JARs, one
with the project's classes, and one that also includes those of all of it's
dependencies. If you only need one of the extensions, use
`<project>/target/<project>-<version>-jar-with-dependencies.jar`. If you need
multiple extensions, use
`SuperVirtualHost/SuperVirtualHost-<version>-jar-with-dependencies.jar`. If you
are building your own extensions, the easiest method is to manage your project
with maven, set SuperVirtualHost and all of the extensions you need as
dependencies, copy the `maven-assembly-plugin` plugin configuration from the
base project's POM, and run `mvn package`. This will create a JAR in your build
folder, `<project>-<version>-jar-with-dependencies.jar`, that has _everything_
packaged together.

`com.lensflare.svh.Launch` is the provided main/launch class. It has one
optional argument, the path to the config file. If this argument is not
specified, `Launch` will look in the current directory for a file named
'config.yml'. Log4j's default configuration file location is 'log4j2.xml' in the
current directory. This can be changed by passing
`-Dlog4j.configurationFile=path/to/log4j2.xml` to `java`.