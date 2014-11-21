# Gradle Bundle Plugin

[![Maintainer Status](http://stillmaintained.com/TomDmitriev/gradle-bundle-plugin.png)](http://stillmaintained.com/TomDmitriev/gradle-bundle-plugin)
[![Build Status](https://travis-ci.org/TomDmitriev/gradle-bundle-plugin.svg?branch=master)](https://travis-ci.org/TomDmitriev/gradle-bundle-plugin)
[![Download](https://api.bintray.com/packages/tomdmitriev/gradle-plugins/org.dm.bundle/images/download.svg)](https://bintray.com/tomdmitriev/gradle-plugins/org.dm.bundle/_latestVersion)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Gradle Bundle Plugin allows you to create OSGI bundles. Its main difference from the 
[Gradle OSGI Plugin](http://www.gradle.org/docs/current/userguide/osgi_plugin.html)
is that it uses [the bnd tool](http://www.aqute.biz/Bnd/Bnd) to generate not only a
manifest but a whole jar.


## Installation
Plugin is hosted in Maven Central Repository. You can easily add plugin to your build
script using following configuration

```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'org.dm.gradle:gradle-bundle-plugin:0.6'
    }
}

apply plugin: 'org.dm.bundle'

```

Depending on the type of your project you also need to add `apply plugin: 'java'` or 
`apply plugin: 'groovy'`, etc.


## Tasks


### `jar`

Generates an OSGI bundle.

When you apply the bundle plugin, `Jar` task no longer uses gradle Java plugin to
generate the output but rather delegates this action to the bnd tool. The latter,
however, uses the 'Jar' task customization, such as extension, baseName, etc.


## Customization


### Instructions

To customise the plugin's behaviour you can either add bnd instructions as attributes
of the jar manifest or you can specify them in bundle extension (the latter will
take precedence over the former). An example:

```groovy
jar {
    manifest {
        attributes 'Implementation-Title': 'Bundle Quickstart', 	// Will be added to manifest
                         'Import-Package': '*'	// Will be overwritten by the insturctions below
    }
}

bundle {
    instructions << [
        'Bundle-Activator': 'foo.bar.MyBundleActivator',
        'Import-Package': 'foo.*',
        '-sources': true
    ]
    
    instruction 'Export-Package', '*' // Specify an individual instruction
    instruction '-wab', ''
}
```

Note that restrictions of the bnd tool hold true, that is for example instruction `'-sources': true`
will not include groovy or scala sources.

### Bnd tracing

You can also enable bnd tracing by setting `bundle.trace` to true.

```groovy
bundle {
    trace = true
}
```

### Gradle 1.x support
The current version of plugin assumes Gradle 2.x is used. The last version that supports
Gradle 1.x is 0.5, which can be added to your script as follows

```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'org.dm.gradle:gradle-bundle-plugin:0.5'
    }
}

apply plugin: 'bundle'

```
