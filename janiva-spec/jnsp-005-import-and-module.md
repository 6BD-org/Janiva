# Import and Module

## Janiva Module

### Module Root
Janiva code can either be organized into one single `.janiva` file or multiple 
files under one `module`. Janiva code can import codes from other `.janiva` files 
under same module. When executing janiva module, an option `janiva.module-root` must be set. 
`janiva.module-root` is used as base directory when resolving imports.

### Import Path
Import paths are relative paths used to locate `.janiva` files. For example, with 
module root equals to `/some-path`, an import path `lib.util.math` refers to 
`/some-path/lib/util/math.janiva`


## Export and Import

A janiva object must be exported in order to be used by other janiva code files. Export is a 
built-in lambda `@export` which takes one argument. Assume `janiva.module-path=/some-path`

```
# in file /some-path/lib/value.janiva

# must export this
@export << {
  a << 1,
  b << 2,
  "value": $a + $b,
}
```

```
# import a previously exported value
_value << @import << "lib.value"

# Here we get {"value": 3} in std out
@stdout << {
  "value": $_value -> "value"
}
```