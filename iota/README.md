## *iota*  Compiler

*iota* is a compiler for a language called *iota*. Refer to [The *iota* Language Specification](langspec) 
for the formal details about the language. The compiler targets a register-based architecture called 
[Marvin](https://www.cs.umb.edu/~siyer/teaching/marvinspec.pdf).

The following command compiles the compiler:
```bash
$ ant
```

The following command runs the compiler and prints the usage string:
```bash
$ ./bin/iota
```

The following command compiles a test *iota* program `Factorial.iota` using the *iota* compiler, which translates 
the program into a Marvin program called `Factorial.marv`:
```bash
$ ./bin/iota tests/Factorial.iota
```

The following command assembles and simulates the `Factorial.marv` program:
```bash
$ python3 ./bin/marvin.py Factorial.marv
```

## Software Dependencies

* [OpenJDK](https://openjdk.org/)
* [Ant](https://ant.apache.org/)
* [Python](https://www.python.org/)
