# Heuristic methods for JobShop scheduling

This repository contains the starter code for the assignment.


## Compile

Compilation instructions are given for Linux. On windows you can use the `gradlew.bat` script.

```
❯ ./gradlew build  # Compiles the project
❯ ./gradlew jar      # Creates a fat-jar in build/libs/JSP.jar
```

The compiled jar is now `build/libs/JSP.jar` can be executed like so :

```
❯ java -jar build/libs/JSP.jar --solver basic --instance ft06
```
The command line above indicates that we want to solve the instance named`ft06` with the `basic` solver. It should give an output like the following :
```
                         basic
instance size  best      runtime makespan ecart
ft06     6x6     55            1       60   9.1
AVG      -        -          1.0        -   9.1
```

Fields in the result view are the following :
- `instance`: name of the instance
- `size`: size of the instance `{nom-jobs}x{num-tasks}`
- `best`: best known resultfor this instance
- `runtime`: time taken by the solver in milliseconds (rounded)
- `makespan`: makespan of the solution
- `ecart`: normalized distance to the best result: `100 * (makespan - best) / best` 

One can also specify multiple solvers (below `basic` and `random`) and instances (below `ft06`, `ft10` and `ft20`) for simultaneous testing:

```
❯ java -jar build/libs/JSP.jar --solver basic random --instance ft06 ft10 ft20

                         basic                         random
instance size  best      runtime makespan ecart        runtime makespan ecart
ft06     6x6     55            1       60   9.1            999       55   0.0
ft10     10x10  930            0     1319  41.8            999     1209  30.0
ft20     20x5  1165            0     1672  43.5            999     1529  31.2
AVG      -        -          0.3        -  31.5          999.0        -  20.4
```
Here the last line give the average `runtime` and `ecart` for each solver.

```
usage: jsp-solver [-h]  [-t TIMEOUT] --solver SOLVER [SOLVER ...]
                  --instance INSTANCE [INSTANCE ...]

Solves jobshop problems.

named arguments:
  -h, --help             show this help message and exit
  --solver SOLVER [SOLVER ...]
                         Solver(s) to use  (space  separated  if  more than
                         one)
  -t TIMEOUT, --timeout TIMEOUT
                         Solver  timeout  in  seconds   for  each  instance
                         (default: 1)
  --instance INSTANCE [INSTANCE ...]
                         Instance(s) to  solve  (space  separated  if  more
                         than one)


```

### Running directly from Gradle

The project can be executed directly with `gradle` by specifying the arguments like so :

```
❯ ./gradlew run --args="--solver basic random --instance aaa1 ft06 ft10 ft20"
```

This notably ensures that sources have been recompiled whenever necessary.


## IDE Support

Most IDEs should provide support for importing gradle projects. However, our experience has been best with IntelliJ so far and we would recommend it.

#### IntelliJ

IntelliJ has worked best, with out of the box support for the import of gradle projets:
https://www.jetbrains.com/help/idea/gradle.html#gradle_import_project_start


#### Eclipse

This projet is already configure as an eclipse project, just import it using `import project from filesystem`
However, if you want to restart a fresh new eclipse project for some reasons use the configured gradle script to allow generating an eclipse configuration like so : 
```
./gradlew cleanEclipse eclipse
```
------------------------------------------------------------
Tasks runnable from root project
------------------------------------------------------------

Application tasks
-----------------
run - Runs this project as a JVM application

Build tasks
-----------
assemble - Assembles the outputs of this project.
build - Assembles and tests this project.
buildDependents - Assembles and tests this project and all projects that depend on it.
buildNeeded - Assembles and tests this project and all projects it depends on.
classes - Assembles main classes.
clean - Deletes the build directory.
jar - Assembles a jar archive containing the main classes.
testClasses - Assembles test classes.

Build Setup tasks
-----------------
init - Initializes a new Gradle build.
wrapper - Generates Gradle wrapper files.

Distribution tasks
------------------
assembleDist - Assembles the main distributions
distTar - Bundles the project as a distribution.
distZip - Bundles the project as a distribution.
installDist - Installs the project as a distribution as-is.

Documentation tasks
-------------------
javadoc - Generates Javadoc API documentation for the main source code.

Help tasks
----------
buildEnvironment - Displays all buildscript dependencies declared in root project 'JSP'.
components - Displays the components produced by root project 'JSP'. [incubating]
dependencies - Displays all dependencies declared in root project 'JSP'.
dependencyInsight - Displays the insight into a specific dependency in root project 'JSP'.
dependentComponents - Displays the dependent components of components in root project 'JSP'. [incubating]
help - Displays a help message.
model - Displays the configuration model of root project 'JSP'. [incubating]
projects - Displays the sub-projects of root project 'JSP'.
properties - Displays the properties of root project 'JSP'.
tasks - Displays the tasks runnable from root project 'JSP'.

IDE tasks
---------
cleanEclipse - Cleans all Eclipse files.
eclipse - Generates all Eclipse files.

Verification tasks
------------------
check - Runs all checks.
test - Runs the unit tests.

Rules
-----
Pattern: clean<TaskName>: Cleans the output files of a task.
Pattern: build<ConfigurationName>: Assembles the artifacts of a configuration.
Pattern: upload<ConfigurationName>: Assembles and uploads the artifacts belonging to a configuration.

To see all tasks and more detail, run gradlew tasks --all

To see more detail about a task, run gradlew help --task <task>