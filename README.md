# Daikon Gradle Plugin
(experimental) daikon gradle plugin

This Gradle plug-in creates a task to run [Daikon](https://plse.cs.washington.edu/daikon/) on Java projects.

## Integration

To use this plug-in, [you must have downloaded Daikon and have it as dependency](https://plse.cs.washington.edu/daikon/download/).

Add the plug-in dependency and apply it in your project's `build.gradle`:
```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        ...
        classpath 'com.sri.gradle:daikon-gradle-plugin:1.0'
    }
}
```

## Applying the Plugin

### Java

```groovy
apply plugin: 'java'
apply plugin: 'com.sri.gradle.daikon'
```

## Daikon configuration

In the build.gradle of the project that applies the plugin:
```groovy
runDaikon {
    // the project directory where daikon.jar, ChicoryPremain.jar,
    // and dcomp_*.jar files exist  
    neededlibs = file("libs")
    outputdir = file("${projectDir}/build/daikon-output")
    // *TestDriver package name
    // This TestDriver was generated by Randoop 
    driverpackage = "com.foo"
}
```

## Task

* `daikonRun` - runs Daikon workflow'.
