# JSONX

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

# Roadmap

## Basic json
- [x] Json primitive support
- [x] Object support
- [x] List support

## Extension phase 1

- [x] Addition
- [x] Subtraction
- [x] Multiplication
- [x] Division
- [ ] String concatenation for key and value

## Extension phase 2
- [ ] Value reference
- [ ] Hierarchical value lookup
- [ ] Temporary value declaration

## Extension phase 3
- [ ] Control flows
- [ ] Functional features
