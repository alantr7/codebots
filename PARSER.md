At the start of the parsing process, the parser can only look for `import`, `function` and `var` expressions.

Once the parser finds an `import` expression, it will look for a `from` expression, then a `string` expression, and finally an optional `;` expression.

Once the parser finds a `function` expression, it will look for an `identifier` expression, then a `(` expression, then a `)` expression, then a `{` expression. Then, it will look for `function_body`, and finally for a `}` expression.

Once the parser finds a `var` expression, it will look for an `identifier` expression, then an optional `=` expression, then an optional `expression` expression, and finally an optional `;` expression.

`expression` example:
```js
random(3,17)*(5*11)+3*2+random(0, 10+3)+11
```

### Parsing expression
The parser will first look for a number, or a function call, then an operator, then another number or this expression recursively.

```json
{
  "type": "expression",
  "value": {
    "type": "binary",
    "left": {
      "type": "binary",
      "left": {
        "type": "call",
        "callee": "random",
        "arguments": [
          {
            "type": "number",
            "value": 3
          },
          {
            "type": "number",
            "value": 17
          }
        ]
      },
      "operator": "*",
      "right": {
        "type": "binary",
        "left": {
          "type": "number",
          "value": 5
        },
        "operator": "*",
        "right": {
          "type": "number",
          "value": 11
        }
      }
    },
    "operator": "+",
    "right": {
      "type": "binary",
      "left": {
        "type": "number",
        "value": 3
      },
      "operator": "*",
      "right": {
        "type": "number",
        "value": 2
      }
    }
  }
}
```