# Jnsp-003: Type system

In Janiva, every visible element is a value, whose type can be one of

| Type    | Has Member | Has Array Element | Executable | o-bindable | l-bindable |
|---------|------------|-------------------|------------|------------|------------|
| String  | No         | Yes               | No         | Yes        | Yes        |
| Number  | No         | No                | No         | Yes        | Yes        |
| Boolean | No         | No                | No         | Yes        | Yes        |
| Object  | Yes        | No                | No         | Yes        | Yes        |
| Array   | No         | Yes               | No         | Yes        | Yes        |
| Lambda  | No         | No                | Yes        | No         |            |
| Null    | No         | No                | No         | Yes        | Yes        |


## Immutability

Although latent attributes can be bound for unlimited times, the underlying value is always immutable. For example, 
there is no way to modify an array element or object attribute in-place, instead, you always make a copy with modifications.

There are basically two ways of making a modified copy of a value. The first one is to use a view. A view is a 
reference to original + zero or more modifications stored in a separated "delta" area. The second way is to construct
a new object from scratch. The first approach is efficient for large objects, such like very long strings, while second
approach is much simpler.

Lambdas are immutable as well. When doing partial application or lambda composition, a light-weight copy of the lambda 
should be created. Note that internals of a lambda copy might change during partial application, but it's ensured to 
freeze once the operation finishes.

## Null
Every attribute can potentially be null. Access members or array elements of a null value always get null, and trying to
invoke a null value will throw exception.

## String
Strings are created using string literals,

```json
{
 "a": "I am a String"
}
```

## Number

Numbers can be integers or float numbers, which are backed by Java's `BigDecimal`. Any limitations on `BigDecimal` also
applies to Janiva's Number.

```json
{
  "int": 66666666666666,
  "floating-point": 33333333333.444
}
```

## Boolean
Boolean values can be either `true` or `false`

```json
{
  "true": true,
  "false": false
}
```
## Object
An object composes of zero or multiple attributes. Attributes in object are simply key-value pairs, where keys are strings 
and values can be any type including `null`. When assembling object, only o-bound attributes are used while l-bound ones are 
discarded, that's why programmer should use l-binding whenever possible, because assembling objects are quite expensive due
to overheads such as dynamic object shape transitions, while l-binding is fast, because it used stack frame, which are 
statically determined during static analysis.

A bad example is like

```
{
  "_a": 1,
  "_b": $_a + 1,
  "result": $_b + 1
}
```

do this instead

```
{
  _a << 1,
  _b << $_a + 1,
  "result": $_b + 1
}

```

### Member accessing
One main ability of object is member accessing. Members of an object can be accessed using "." operator, just like most 
object-oriented languages.

```
_some_obj << @import << "mylib.obj"

@stdout << {
    "res": $_some_obj.val
}
```

The difference between member accessing and attribute referring is that 
attribute referring is performed before object assembly while member can be accessed only when object assembly is finished. This 
brings one limitation that only o-bound attributes can be access from out-side of the object. Following code won't work

```
{
  "a": {
    _key << 1,
  },
  // This won't work, because l-bounded attribute is accessed from out-side of the object
  "b": $a._key
}
```

Do this instead

```
{
  "a": {
    "key": 1,
  },
  "b": $a.key
}
```

## Array

Arrays can contain items with all types, including array itself. Arrays in Janiva are immutable, which means there's no
primitives or built-in lambdas which are able to mutate elements in it. An array can be created in two ways, array assembling and
array view.

Array assembling creates array by allocating a separate memory, and set value to it. Note that although arrays are immutable, 
setting items are allowed internally. The following code assembles an array and binds it to "a";
```json
{
  "a": [1,2,3,4,5]
}
```

Array view is a read-only view built on top of any data structure and provides array-like operations. When creating 
array views, no memory allocation happens, which makes it fast. The following code using `@range` lambda
only create array views instead of
allocating them.

```
{
  "a": @range << 3,
  "b": @range << "Hello, world"
}
```

## Lambda

Please see [Functional support](./jsnp-004-functional.md) for lambda details.