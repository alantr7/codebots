### Language example
```ts
import turtle;

function main() {
    var blocksMined = 0;
    while (blocksMined < 5) {
        var result = mineBelow();
        if (!result)
            break;
        
        turtle.move(Direction.DOWN);
        blocksMined = blocksMined + 1;
    }
}

function mineBelow() {
    var blockUnder = turtle.getBlock(Direction.DOWN);
    if (blockUnder == BlockType.DIRT) {
        turtle.dig(Direction.DOWN);
        return true;
    }
    
    return false;
}
```