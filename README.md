# Daikon Gradle Plugin
(experimental) daikon gradle plugin

This Gradle plug-in creates a task to run [Daikon](https://plse.cs.washington.edu/daikon/) on Java projects.

## Configuration

To use this plug-in, [you must have downloaded Daikon and have it as dependency](https://plse.cs.washington.edu/daikon/download/).

### Configuring your project

Add the plug-in dependency and apply it to the root project's `build.gradle`:

```groovy
plugins {
    id 'java'
    id 'maven-publish'
    id 'com.sri.gradle.daikon' version '0.0.1-SNAPSHOT'
}
```

Make sure you have also declared the following repositories and dependencies on your `build.gradle` file:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url 'https://plugins.gradle.org/m2/'
    }
}

dependencies { 
    implementation 'com.google.guava:guava:28.0-jre'
    testImplementation 'org.hamcrest:hamcrest:2.2'
    testImplementation 'junit:junit:4.13'
}
```

Note: The Daikon Gradle Plugin uses Guava's immutable collection classes. 


### Configuring the Daikon plugin

In the `build.gradle` of the project that applies the plugin:

```groovy
runDaikon {
    outputDir = file("${projectDir}/build/daikon-output")
    // the project directory where daikon.jar, ChicoryPremain.jar,
    // and dcomp_*.jar files exist  
    requires = file("libs")
    // *TestDriver package name
    // If you use Randoop, then Randoop will generate this TestDriver for you. 
    testDriverPackage = "com.foo"
    // However, if you are not using Randoop, then you need a TestDriver.
    // This TestDriver should have a static void main method, so Daikon can
    // executed. Having said that, this plugin can help you generate
    // this TestDriver. This TestDriver will reside in the package as
    // the one you specified in the `testDriverPackage` statement. 
    // To generate this TestDriver, all you need to do is to include the 
    // following statement (default value is false):
    generateTestDriver = true
}
```

Your `settings.gradle` file must contain:

```
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```

## Task

* `daikonRun` - runs Daikon workflow'.

## Configuration

## License

    Copyright (C) 2020 SRI International

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
