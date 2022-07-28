# JSONX

JSONX is an extension of JSON. It enhances json by providing the capability of 
doing arithmetics, functional programming, and control flows

# Terminologies

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
