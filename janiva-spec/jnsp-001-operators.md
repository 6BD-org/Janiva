# Jnsp-001: Syntax and Operators

## Arithmetics

## Data flow
Janiva is a data-flow driven programming language. Immutable values flow into certain nodes, and some new data flows out.

### Attribute binding
Data flowing from an expression into an attribute name indicates that the expression is evaluated and the result is 
bound to target attribute, like this

```
{
  a << 1
}
```

### Lambda application
Lambdas can be applied by feeding arguments to it, this is also one type of data flow. 

```
{
  a << @range << 3
}
```

## ..