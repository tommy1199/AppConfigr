# AppConfigr  [![Build Status](https://travis-ci.org/Tommy1199/AppConfigr.svg?branch=master)](https://travis-ci.org/Tommy1199/AppConfigr) [![license](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://raw.githubusercontent.com/tommy1199/appconfigr/master/LICENSE)

## Overview

AppConfigr is a small helper to manage local configuration files for applications. It is built on top of Jackson and 
its databind capabilities. It has the following key features:

- Provide access to configuration files in a dedicated configuration directory
- Support of environment variables and system properties in configuration files
- Support of different file formats (whatever is supported by Jackson databind)

## Future topics
- Support of properties in configuration files for internationalization purposes
- Support of updatable configuration data

## Prerequisites

Java 1.7 or higher is needed. AppConfigr additionally depends on Jackson for mapping the configuration files to Java 
objects and Guava which is used internally.

## Getting Started

Create a simple configuration class.

```java
public class MyAppConfig {
   // define default values by just assign them on initialization.
   private int myInt;
   private int myString;
   
   public int getMyInt() {return myInt;}
   public String getMyString() {return myString;}
}
```

Create a file in the config folder ("path/to/config/files") with the name "my-app-config.conf" and the content

```yaml
myInt: 12
myString: this is my configuration string
```

> The default file format for the configuration files is yaml. It can be changed, see [here](#dataformats)


Create a new AppConfigr instance with the configuration directory as parameter.

```java
import io.github.tommy1199.appconfigr.AppConfigr;

AppConfigr configr = AppConfigr.fromDirectory("path/to/config/files")
                               .build();
```

Now the configuration file can be loaded and used in the application. The following

```java
MyAppConfig config = configr.getConfig(MyAppConfig.class);
System.out.println("The value of myInt is [" + config.getMyInt() + "]");
System.out.println("The value of myString is [" + config.getMyString() + "]");
```

will print:

```
The value of myInt is [12]
The value of myString is [this is my configuration string]
```

## Variable Usage

On top of the rich features already provided by jackson, it is possible to use variables in the configuration files. 
As default AppConfigr tries to resolve the variables by System Properties and then by Environment Variables in this 
order.

If we change the content of the configuration file in the **Getting Started** section to:

```yaml
myInt: ${props.int}
myString: ${props.string}
```

and set the properties on application startup

```bash
java -Dprops.int=12 -Dprops.string="this is my configuration string" ...
```

we would get the same output.

## Custom Configuration File Names

The normal behaviour of AppConfigr is to use the configuration class name for looking up the file name. The rule is 
to use transform the class name to a lowercase hyphenated version with **.conf** suffix. An example can be seen below.

MyOwnConfiguration.class --> my-own-configuration.conf
 
If you want to specify an own file name, e.g. if you have several files with the same mapping, you can just use the 
overloaded method

```java
MyAppConfig config = configr.getConfig(MyAppConfig.class, "my-custom-filename.conf");
```

## Own Resolver
If you want to change the way variables are resolved by AppConfigr, you can define an own Resolver and set it on the 
Builder. Defining an own resolver is pretty simple, just extend the class VariableResolver.

```java
public class MyOwnResolver extends VariableResolver {

}
```

## Chaining Resolvers

## Other data formats<a name="dataformats"></a>