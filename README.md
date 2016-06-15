# Gradle Bundle Plugin

[![Build Status](https://travis-ci.org/TomDmitriev/gradle-bundle-plugin.svg?branch=master)](https://travis-ci.org/TomDmitriev/gradle-bundle-plugin)
[![Gradle Status](https://gradleupdate.appspot.com/TomDmitriev/gradle-bundle-plugin/status.svg?branch=master)](https://gradleupdate.appspot.com/TomDmitriev/gradle-bundle-plugin/status)
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
        classpath 'org.dm.gradle:gradle-bundle-plugin:0.8.5'
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

You can enable bnd tracing by setting `bundle.trace` to true.

```groovy
bundle {
    trace = true
}
```

### Failing build in case of bnd build errors

You can make Gradle to fail the build in case of bnd build errors by setting `bundle.failOnError` to true.

```groovy
bundle {
    failOnError = true
}
```

### Passing transitive dependencies to Bnd

By default transitive dependencies are not included to the classpath passed to Bnd, to include them
`includeTransitiveDependencies` needs to be set to true.

```groovy
bundle {
    includeTransitiveDependencies = true
}
```

### Exclusion of dependencies from the classpath passed to Bnd

This can be done using `exclude` property of bundle extension, for example:

```groovy
bundle {
    exclude module: 'guava'
    exclude group: 'org.jmock'
}
```

### Exclusion of project properties from the set of instructions passed to Bnd

By default the project properties are passed to Bnd (which means they may end up in the resulting MANIFEST.MF),
this can be prevented by setting `passProjectProperties` to false:

```groovy
bundle {
    passProjectProperties = false
}
```

### Blueprint support

To enable blueprint support you need to pass the following instruction to Bnd:

```groovy
bundle {
    instruction '-plugin', 'aQute.lib.spring.SpringXMLType'
}
```

### Known issues
When the Gradle Daemon is enabled for a multi-module project, the plugin may produce a compilation `bad class file`
error. To get around it compilation needs to be run in a separate process, i. e. the following settings need applying:
```groovy
subprojects {
	...

	compileJava {
		options.fork = true
	}
	
	...
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
