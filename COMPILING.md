Compiling
=========

You can compile CraftBook as long as you have the [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html) for Java 8 or newer.
You only need one version of the JDK installed.

The build process uses Gradle, which you do *not* need to download.

## To compile...

### On Windows

1. Shift + right click the folder with CraftBook's files and click "Open command prompt".
2. `gradlew clean`
3. `gradlew build`

### On Linux, BSD, or Mac OS X

1. In your terminal, navigate to the folder with CraftBook's files (`cd /folder/of/craftbook/files`)
2. `./gradlew clean`
3. `./gradlew build`

## Other commands

* `gradlew idea` will generate an [IntelliJ IDEA](http://www.jetbrains.com/idea/) module for each folder.
* `gradlew eclipse` will generate an [Eclipse](https://www.eclipse.org/downloads/) project for each folder.