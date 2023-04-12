# Language scope

This specification defines which abilities will/won't be included in Janiva programming language. Janiva programming language is a data templating language focusing on JSON data. One of the main usecases of this language is configuration management, where you want to propogate a small set of configuration keys to a very large manifests.

## What will be included?
- Full JSON compatibility
- Arithmetics
- Lambdas
- Import/Export for objects and lambdas
- Control-flow equivalent built-in lambdas
- Functional programming
- Stdout
- Attribute accessing
- Array item accessing

## What won't be included?
- Threads as well as any other async programming paradigm
- Network I/O (don't try to develop a XXX Management System using Janiva)
- Mutability
- YAML support :)