# Daikon Gradle Plugin

(experimental) daikon gradle plugin

This Gradle plug-in creates a task, `daikonRun`, that runs [Daikon](https://plse.cs.washington.edu/daikon/) on Java projects.

TODO: Document what runs the plugin uses for Daikon.  Is it all test runs?

## Configuration

To use this plug-in, Daikon should be in the `libs` directory (TODO: is it OK for it to be anywhere on the classpath?) and it should be a `file` dependency in your `build.gradle` file.

### Configuring your project

Add the following to the `build.gradle` file of the project that applies the plugin:

```groovy
plugins {
    id 'java'
    id 'maven-publish'
    id 'com.sri.gradle.daikon' version '0.0.1-SNAPSHOT'
}

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

    pluginManagement {
        repositories {
            mavenLocal()
            gradlePluginPortal()
        }
    }

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
