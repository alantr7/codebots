# Overview
CodeBots is a Minecraft plugin that allows you to create and program bots inspired by ComputerCraft's turtles. It uses a custom-made scripting language with specifications below.

**I am currently working on [Torus](https://github.com/alantr7/torus). Once it's published I'll get back to CodeBots.**

## Scripting Language

### Rework in progress!
I am remaking the scripting language from scratch. It will be similar to C and that for multiple reasons.
One is that it's easier to keep track of variables and better utilize the garbage collection. Second is:
I've noticed people getting code from AI prompts and AI tends to generate it in JavaScript which can not be used here,
and if it's not JavaScript then it usually has invalid syntax. After the rework is done, people who use
AI should be able to generate correct code.

For details about the current scripting language, please read the wiki:<br />
https://github.com/alantr7/codebots/wiki/Scripting-Language

## Todo List
#### Done and being tested:
- Variables
- Constants
- Functions
- If, else-if, else statements
- Importing standard modules
- Return statements
- String concatenation
- For, while and do-while loops
- Grouping expressions with parentheses
- Arrays and dicts
- Records
- Bot inventory
- Memory module (saving, loading data)
- Configuration for movement and rotation speeds, allow/block item drops, allow/block mining

#### Yet to be done
- WorldGuard, Factions integration
- Logic operators (AND, OR) for boolean expressions
- Error handling

#### Future plans
- Rework bots memory
- Resume programs when bots are loaded
- Bot networks using routers
- Ton of optimization
- Bot statistics (blocks moved, blocks mined)
- SQL database support
