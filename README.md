# JSONX


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


JSONX is an extension of JSON. It enhances json by providing the capability of 
doing arithmetics, functional programming, and control flows

# Terminologies

## Data flow
Data flow is represented by `<<` operator. Is is used for 
- transfer output to stdout
- latent attribute binding


## Primitive
JSONX primitives are 
- number
- string
- boolean

a primitive itself is a JSONX program

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
To make JSONX usable, it is able to feed interpreted json to output, simple using `<<` operator

```json
stdout << {
  "a": 1,
  "b": 2.5,
  "c": {
    "d": [1+1, "a" + 2]
  }
}
```

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

## Control flow
There's no control flow in jsonx, instead, it provides couple of built-in functions that do the similar job.

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