# AppConfigr  [![Build Status](https://travis-ci.org/Tommy1199/AppConfigr.svg?branch=master)](https://travis-ci.org/Tommy1199/AppConfigr) [![license](https://img.shields.io/github/license/mashape/apistatus.svg?maxAge=2592000)](https://raw.githubusercontent.com/tommy1199/appconfigr/master/LICENSE)[ ![Download](https://api.bintray.com/packages/tommy1199/appconfigr/AppConfigr/images/download.svg) ](https://bintray.com/tommy1199/appconfigr/AppConfigr/_latestVersion)

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
    @Override
    Result resolve(String variableName) {
        // here resolve the variable 
    }
}
```

Then simply use it during building AppConfigr instance

```java
AppConfigr.fromDirectory("path/to/config/files")
          .withResolvingStrategy(new MyOwnResolver())
          .build();
```

> The resolve method should not throw any exception, but should return a Result.None instead. The factory method 
Result.none(String message) can be used for that.

## Chaining Resolvers

Resolvers can be chained together. In the example we have two resolvers, one which resolves every variable to the 
String "FIRST" except the variable name "unknown" and the other resolves all to "FALLBACK".

```java
public class FirstResolver extends VariableResolver {
    @Override
    Result resolve(String variableName) {
        if ("unknown".equals(variableName)) {
            return Result.none("this is unknown by me");
        } else {
            return Result.some("FIRST");
        } 
    }
}

public class FallbackResolver extends VariableResolver {
    @Override
    Result resolve(String variableName) {
        return Result.some("FALLBACK"); 
    }
}
```

With a config file based on the first example

```yaml
myInt: 12
myString: ${this} ${is} ${unknown}
```

the following code

```java
VariableResolver resolver = new FirstResolver().withFallback(new FallbackResolver());

AppConfigr configr = AppConfigr.fromDirectory("path/to/config/files")
                               .withResolvingStrategy(resolver)
                               .build();
                               
MyAppConfig config = configr.getConfig(AppConfig.class);

System.out.println("The value of myString is [" + config.getMyString() + "]");
```

would print 

```
The value of myString is [FIRST FIRST UNKNOWN]
```

## Other data formats<a name="dataformats"></a>

The default format used by AppConfigr is yaml. But as AppConfigr is based on Jackson the supported format can be 
switched easily. If you want to use e.g. Json as configuration format for the example in the "Getting started" 
section like this

```json
{
  "myInt": "12",
  "myString": "this is a string"
}
````

you only have to do the following

```java
AppConfigr configr = AppConfigr.fromDirectory("path/to/config/files")
                               .withFactory(new JsonFactory)
                               .build();
```

Features like the variable resolving can be used for all formats supported by Jackson.