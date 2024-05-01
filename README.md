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
- Grouping expressions with parentheses

#### Yet to be implemented
- Logic operators (AND, OR) for boolean expressions
- For loops
- Importing user modules
- Types for variables, parameters, etc. (dynamic will be the default)
- Error handling

#### Future plans
- More standard modules
- Web editor for writing scripts
- Bot networks using routers
- Ton of optimization

## Language examples

### Main function
Main function is called as soon as the Bot starts.
```ts
function main() {
    print("Hello world!")
}
```

### Variables
Variables can be declared using the `var` keyword. Variables are dynamically typed. They can also be declared without a value. Variables can be declared outside a function too, which makes them accessible from all functions.
```ts
var name = "Alan"

function main() {
    var greeting = "Hello "
    print(greeting + name)
}
```

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

### Importing modules
You can import modules using the `import` keyword. There are a few modules that are available by default:
- `turtle` - provides functions for controlling the turtle
- `math` - provides math functions

You can also import your own modules. *(Work in progress)*

If you'd like to use a different name for an imported module, you can use the `as` keyword, like so: `import turtle as t;`
```ts
import turtle;

function main() {
    print("Moving forward.")
    turtle.move("forward")
}
```

### A bit complex example
```ts
import turtle;

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