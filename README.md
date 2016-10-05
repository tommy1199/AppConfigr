## Build Status

[![Build Status](https://travis-ci.org/Tommy1199/AppConfigr.svg?branch=master)](https://travis-ci.org/Tommy1199/AppConfigr)

## Overview

AppConfigr is a small helper to manage local configuration files for applications. It is built on top of Jackson and 
its databind capabilities. It has the following key features:

- Provide access to configuration files in a dedicated configuration directory
- Support of environment variables and system properties in configuration files
- Support of properties in configuration files for internationalization purposes
- Support of different file formats (whatever is supported by Jackson databind)
- Support of updatable configuration data

## Prerequisites

Java 1.7 or higher is needed. AppConfigr additionally depends on Jackson for mapping the configuration files to Java 
objects and Guava which is used internally.

## Getting Started

Create a simple configuration class.

```java
public class MyAppConfig {
   // define default values by just assign them on initialization.
   private int myInt = 0;
   private int myString = "default";
   
   public int getMyInt() {return myInt;}
   public String getMyString() {return myString;}
}
```

Create a new AppConfigr instance with the configuration directory as parameter.

```java
import io.github.tommy1199.appconfigr.AppConfigr;

AppConfigr configr = AppConfigr.fromDirectory("path/to/config/files")
                               .build();
```


