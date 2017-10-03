# Libra
Soot-based taint analyzer for Java bytecode

# Directory Structure                                                       
	- README.md : this file
	- SourcesAndSinks.txt: annotations for sources and sinks
	- run.sh: script for the tool
	- src: source code
	- lib: libraries

# Usage
./run.sh $1 $2 $3
    - $1: target .jar file that needs to be analyzed
    - $2: dependencies of the project
    - $3: entry point(s)

# Example (CVE-2017-9735 detected by our tool)
./run.sh projects/jetty/themis.jar projects/jetty/jetty-util-9.4.5-SNAPSHOT.jar projects/jetty/entry.txt
    - Note that themis.jar contains both the bytecode of the original .jar file as well as the test cases as the entries of the tool.
