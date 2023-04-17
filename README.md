# Janiva
Janiva programming language, inspired & bootstrapped by 
[Simple Language](https://github.com/graalvm/simplelanguage)

# Build Project

## Generate parser

```sh
./generate_parser.sh
```

## Set JAVA_HOME
Set JAVA_HOME to a graalvm installation path (22.30, jdk17 is preferred)

## Run unit tests

```sh
mvn clean test
```


Janiva is an extension of JSON. It enhances json by providing the capability of 
doing arithmetics, functional programming, and control flows

# Terminologies

## Data flow
Data flow is represented by `<<` operator. Is is used for 
- transfer output to stdout
- latent attribute binding


## Primitive
Janiva primitives are 
- number
- string
- boolean

a primitive itself is a Janiva program

## Object
Object composes of attributes where keys are strings and values are
- primitives
- objects
- lists

## List
List is a collections of values

## Value
Value can be primitives, objects or lists

## Attribute
Contents of objects are `attributes`
for example, the root object has two attributes `a` and `b`
```json
{
    "a": 1,
    "b": 2
}
```

### Attribute binding
Attribute binding is an operation that binds a `value` to an `attribute` in
current context
```json
{
  "a": 1
}
```

### Latent attribute binding
Attributes that are excluded from the final json output are called `latent attributes`. To define a latent attribute, simple use `<<` operator

```json
{
    "a": 1,
    // Here we bind b to 2
    b << 2,
    "e": {
      // b is bound in child scope
      b << 3,
      "c": 3
    }
}
```

### Attribute reference
A attribute or latent attribute can be referred to and bound to another attribute. Attribute references are performed by `$` operator, for example 

```json
{
    "a": 1,
    // Here we bind b to 2
    b << 2,

    // The attribute c is bound by referring to latent attribute b
    "c": $b,
    "d": {
        // looks up b in parent scope
        "c": $b,
    },
    "e": {
      // b is bound in child scope, thus "c" here is bound to 3
      b << 3,
      "c": $b
    }
}

```


## Std out
To make Janiva usable, it is able to feed interpreted json to output, simple using `<<` operator

```json
@stdout << {
  "a": 1,
  "b": 2.5,
  "c": {
    "d": [1+1, "a" + 2]
  }
}
```

Internally, stdout performs like a built-in lambda that flush the object to std out and return a reference to original object.

## lambda
`lambdas` are pure functions declared at the beginning of the script. Definitions of `lambdas` start with `@` and end with `#`. `@` is also used to refer to a lambda instance.

`lambda` can be applied by feeding data to it using `<<` operator. Note that partial evaluation is not supported, therefore parameter list must match in length. 


```json
// Define two lambdas

// adding two numbers
@add :: (x, y) >> $x + $y #

// converting a tuple to object
@tuple_to_dict :: (x, y) >> {"first": $x, "second": $y} #

stdout << {
    // binding sum of two numbers to a
    "a": @add << 1 << 2,
    // binding a dict to latent attribute _tuple
    _tuple << @tuple_to_dict << 1 << 2,
    // refer to latent attribute
    "tuple": $_tuple,
}

```

### Partial application
Janiva supports partial application of lambdas, which mean you don't have to provide all arguments once, instead, you can feed a part of arguments, and provide the rest later. For example

```
// Here we defined a lambda which takes 5 numbers as argument
@add5 :: (x1, x2, x3, x4, x5) >> $x1 + $x2 + $x3 + $x4 + $x5 #

@stdout << {
    // partial apply with two of them
    _x12 << @add5 << 1 << 1,
    // note that _x12 is a lambda that takes 3 numbers and add then to (1 + 1)
    "sum1": @_x12 << 1 << 1 << 1,
    // Everytime a partially applied lambda is called, a light copy was made to ensure immutability
    "sum2": @_x12 << 2 << 2 << 2,
}

```

## Control flow
There's no control flow in Janiva, instead, it provides couple of built-in functions that do the similar job.

### If
If takes three arguments
- condition that evaluates to a boolean
- then value when condition is true
- else value when condition is false

for example

```json

stdout << {
    "a": @if << true << 1 << 2,
    "b": @if << false << 1 << 2,
}

```

### Range
Range can take three types of arguments
- If a integer `N` is given, it returns a integer array [0, ... N-1]
- If a string is given, it simply split the string into chars, for example "hello" >> ["h", "e", "l", "l", "o"]
- If an array is given, it returns a reference to that array. Note that arrays are immutable, thus no copy is required


## Import and Export

Imports and exports looks exactly like `lambda`s, which are denoted by `@import` and `@export`, however, not like `@export`, `@import` is a primitive instead of lambda. The reason why `@import` can't be a lambda is that it needs to eccess another code file and evaluate it. This cannot be done using `lambda`, because `lambda` is a run-time role, which only participates with guest language objects, and it is difficult to insert new nodes to ast during run-time. Therefore, `@import` is designed to take effect during static analysis.

### Export
`@export` is a `lambda` that takes an object as argument, just like `@stdout`

```json
@export << {
    "a": 1
}
```

It marks an object as exported.


### Import

Import is not l lambda but it follows the same syntax. Imports are treated like pre-processors, which are executed at the very beginning of the program.

```json
_value << @import << "ex.ut-export" #

@stdout << {
    "k": {"c": "d"},
    // Refer to imported value
    "value": $_value
}
```

Note that a value **must be** exported to be imported by other codes.