### Disclaimer:
Script language is still very experimental, and many features are missing! Please read the to-do list below to see what's implemented and what's still missing.

## Todo List
#### Done and being tested:
- Variables
- Functions
- If, else-if, else statements
- Importing standard modules
- Recursiveness
- Return statements
- String concatenation
- While and do-while loops
- For loops
- Grouping expressions with parentheses
- Arrays

#### Yet to be implemented
- Logic operators (AND, OR) for boolean expressions
- Importing user modules
- Types for variables, parameters, etc. (dynamic will be the default)
- Error handling

#### Future plans
- More standard modules
- Web editor for writing scripts
- Bot networks using routers
- Ton of optimization

## Language examples
<hr />

### Main function
Main function is called as soon as the Bot starts.
```ts
function main() {
    print("Hello world!")
}
```
<hr />

### Variables
Variables can be declared using the `var` keyword. Variables are dynamically typed. They can also be declared without a value. Variables can be declared outside a function too, which makes them accessible from all functions.
```ts
var name = "Alan"

function main() {
    var greeting = "Hello "
    print(greeting + name)
}
```
<hr />

### If statements
Expressions must be surrounded by parentheses.
You can use `and`, `or` and `not` operators.
If the expression is false, the `else`/`else if` block will be executed if present.
```ts
function main() {
    var foo = "bar"
    if (foo == "bar") {
        print("foo is bar")
    } else {
        print("foo is not bar")
    }
}
```
<hr />

### Arrays
Arrays can be created using `array()` function.
Default capacity for arrays is 8. You can also specify the capacity as an argument, it is limited to 20 by default. 

By default, all elements in the array have a value of `null`.
```ts
var numbers = array()
var numbers2 = array(10)
```

To access, or set an element in an array, use the square brackets.

```ts
numbers[0] = 5
print(numbers[0])
```

To get the length of an array, use the `length(array)` function.
```ts
print(length(numbers))
```
<hr />

### Dictionaries
Dictionaries are a datatype that can store key-value pairs. They are defined using `dict()` function.
```ts
var locations = dict()
```

To add a key-value pair to a dictionary, use the square brackets. Key can only be of string type (subject to change). You can also use `dict_set(dict, key, value)`
```ts
locations["home"] = new Location(12, 20, -12)
locations["farm"] = new Location(123, 20, 456)

dict_set(locations, "farm", new Location(123, 20, 456))
```

To remove a key-value pair from a dictionary, use the `dict_unset(dict, key)` function.
```ts
dict_unset(locations, "farm")
```
<hr />

### Records
Records are a datatype that can store multiple values. They are defined using `record` keyword.
Properties of the record are defined in the parentheses. Properties are read-only.

Records must be defined outside a function, so in the root of a module.
```ts
record Location(var x, var y, var z)
```

To create an instance of a record, use the `new` keyword.
```ts
var location = new Location(123, 20, 456)
```
To access a property of a record, use the dot operator.
```ts
var x = location.x
var z = location.z
```
<hr />

### Importing modules
You can import modules using the `import` keyword. There are a few modules that are available by default:
- `turtle` - provides functions for controlling the turtle
- `math` - provides math functions
- `lang` - module for language functions. All functions from this module are available by default, so importing it is unnecessary.

You can also import your own modules. *(Work in progress)*

If you'd like to use a different name for an imported module, you can use the `as` keyword, like so: `import turtle as t;`
```ts
import turtle

function main() {
    print("Moving forward.")
    turtle.move("forward")
}
```
<hr />

### A bit complex example
```ts
import turtle

function main() {
    var blocksMined = 0
    while (blocksMined < 5) {
        var result = mineBelow()
        if (!result)
            break
        
        turtle.move("down")
        blocksMined = blocksMined + 1
    }
}

function mineBelow() {
    var blockUnder = turtle.getBlock("down")
    if (blockUnder == "dirt") {
        turtle.mine("down")
        return true
    }
    
    return false
}
```