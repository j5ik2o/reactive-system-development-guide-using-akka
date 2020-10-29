package example1

class Command {
  def execute(): Unit = {
    println("hello, world")
  }
}

class CommandExecutor {
  def execute(command: Command): Unit = {
    val thread = new Thread({ () =>
      command.execute()
    })
    thread.start()
  }
}

object Main extends App {
  new CommandExecutor().execute(new Command)
}
