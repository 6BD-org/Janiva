# Jnsp-004: Functional Programming

## Built-in

## Lambda definition
Lambdas are defined at the top of the script file, like this 
```
@lambda :: (x1, x2) >> $x1 + $x2 #
```
Some key components in this definition are
- Lambda name: name of the lambda is defined with `@` sign
- a `::` separates lambda name and the actual template definition of the lambda
- `x1` and `x2` in the definition above indicates the parameter list
- `>>` separates parameter list and the actual implementation;

### Namespacing

## Partial application