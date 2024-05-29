import bot

function main() {
    bot.chat("Watch these moves!")
    bot.move("forward")

    bot.rotateLeft()
    bot.rotateLeft()

    bot.move("forward")
    bot.rotateLeft()
    bot.rotateLeft()

    bot.chat("Yeah.. I can't dance.")
}