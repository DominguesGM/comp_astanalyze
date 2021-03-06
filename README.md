#ASTANALYZE



## SUMMARY:

The tool developed generates two graphs, a control flow graph and a data dependency graph, given an abstract syntax tree.
The output is split into two dot files, one containing the control flow graph and another containing the data dependency graph.
The abstract syntax tree must be generated using spoon2ast.jar, a tool developed by Tiago Carvalho.


After compilation, the tool must be run using the following command (assuming working directory as project folder): java -cp bin;lib\* analyser.AstAnalyze <path_to_json>/<file_name>.json
In order to start with a *.java file, execute the following list of commands :
				java -jar <path_to_project>/extra/spoon2ast.jar <path_to_file>/<class_to_parse>.java
				java -cp bin;lib\* analyser.AstAnalyze <json_file_path>/<json_file_name>.json
ATTENTION: spoon2ast.jar exports 2 files, one called ast.json and another called ast.txt. The one to use is the json file.

We suggest using the program Graphviz to visualize the output graphs.


## DEALING WITH SYNTACTIC ERRORS: 

Upon finding an error in the abstract syntax tree, the tool will terminate, returning an error message.


## CODE GENERATION & INTERMEDIATE REPRESENTATION (IRs):

The code "generated" by this tool to name the graph's nodes consists of a partial reconstruction of the code from the abstract syntax tree, which means it is
considerably close to the java language.


## OVERVIEW:

ASTANALYZE processes each method in the given class (present in the AST) separately. It iterates through each line of code until finding 
a control structure. Upon finding a control structure, it follows a recursive approach, going one level lower in the abstract syntax tree,
and proceeding to iterate through every code line inside that control structure or until finding a break, continue or return statement.

A top down approach is followed for the AST processing, from a method header to the last line of code (or last return statement), adding 
a new node to the CFG as it processes a new line of code and connecting it to the graph.
During the processing of the code, usages and definitions of variables (dataflow) per code line are registered into an array, taking the form of the object
DataDependency. (done by CodeProcessor.java and CodeGenerator.java)

After the CFG has been generated and the uses and definitions of variables per code line have been registered, ASTANALYZE starts of a bottom-up approach,
taking each variable used and looking for the closest definition in the path created by each incoming edge. This is done to take into account every possible
variable definition that may be directly affecting that variable use. It does this for each line of code until it runs out of variable uses in each line.
The result are two parallel ArrayLists associated with each graph node, one with the variables it uses, and the other with the node where that variable was defined.

After analyzing each function in the class, it outputs two dot files per class analyzed, one with the control flow graph and the other with the data dependency graph.

The tool makes use of the graph library jgrapht for the graph construction and output as well as the json library json-simple for reading the AST file.




## TESTSUITE AND TEST INFRASTRUCTURE: 

To run the test infrastructure, use the following command: java -cp bin;lib\* test.Test
It will proceed to run the tool on all the example files present in the testsuite folder. There exists one for each control structure. Confirmation of a correct output
can be done manually by comparing the graphs with the source code (in jsonsource) as they are farely simple pieces of code.

Attention: some of the test json files had to be edited after using spoon2ast (for example do_while_example.json),
because spoon2ast doesn't generate a valid json file if the source code makes use of strings



## PROS: 

This tool is prepared to analyze java packages containing multiple classes containing multiple functions. In case there are multiple functions present
in the class, a graph is generated for each method (all the graphs are exported to the same CFG dot file). Even though there is
no edge connecting these graphs, for each method its header is present in the graph and method invocations from within a method body are 
explicit in that method's graph. The fact the graphs of different functions aren't connected was a design choice for the dot file to be more
easily visualized and more understandable when using a tool like graphviz.
Two dot files are generated per class, one for the control flow and another for the data dependencies.
The code generated for the construction of the graphs is designed to be easily interpreted, making it more suitable for presentation
purposes.
All control structure are supported (including the previously unsupported try/catch blocks and incomplete for statements).




## CONS: 

This tool is entirely dependent of the spoon2ast.jar tool, given that it was this tool's specific output our tool was designed to process.


## DELIVERY DETAILS:

The delivered folder contains the source code in src, the external libraries used in lib, the json ast files used to test the tool in testsuite,
the used version of spoon2ast in extra and the source code matching the testsuite in jsonsource.
