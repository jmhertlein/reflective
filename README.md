# Reflective

Reflective is a command framework for Minecraft server plugins that use the Spigot fork of the Bukkit API.

It lets you write commands like this:

```java
@CommandMethod(path = "my test command", permNode="mytest.command", requiredArgs=3)
public void myTestCommand(Player sender, Integer arg1, Double arg2, String arg3, String[] rest) {
  //the player's command is mapped to this method in O(log(n)) time
  //The type of sender is checked for you
  //the permission node is checked for you
  //the number of supplied arguments is checked for you
  //arg1, 2, and 3 are all type-checked and converted for you- it can handle all the eight boxed primitive types + String
  //any additional stuff the user typed is copied into the String[]
}
```

And if you don't even need any parameters (you just want the command to DO something), well, that's even simpler:

```java
@CommandMethod(path = "do something")
public void thingToDo() {

}
```

Reflective focuses on simplifying the process of handling commands and drastically reducing the amount of code you need to write. It offers a CommandExecutor implementation (TreeExecutor) that you add CommandDefinition instances to.

Your plugin makes CommandDefinition implementations and marks methods with the @CommandMethod annotation. This lets the TreeExecutor register them.

Reflective offers automatic tab completion for all commands registered with a TreeCommandExecutor- just add the associated TreeTabCompleter.

Reflective provides an annotation processor to add compile-time sanity checks of all methods annotated with @CommandMethod (`net.jmhertlein.reflective.processor.CommandMethodProcessor`) and **it is strongly suggested you use it, I promise it's awesome**.

The 'root' of each command still needs to be in your plugin.yml.

# Usage

This is a library, so to use it just add it to your classpath. Since this is a snapshot build, I don't have public downloads, but it's a maven project so feel free to build and deploy it to your own internal maven artifact server. I'll eventually have a public maven repo for it when the first stable version is released.

# License

GPLv3, check LICENSE or COPYING for more details. Note that this is the full GPL- to use this library, **your plugin must also be licensed under the GPLv3+.** To clarify, that means your plugin will be free and open source software that anyone can use, copy, modify, and redistribute.

# Example

The most important classes are:

* net.jmhertlein.reflective.TreeCommandExecutor - a CommandExecutor implementation pre-made for you
* net.jmhertlein.reflective.CommandDefinition - a marker interface that you'll make an implementation of
* net.jmhertlein.reflective.CommandMethod - an annotation (one of those @ things above methods) to mark a method as a command

Suppose we have a simple ticket-handling plugin. The CommandDefinition might look like this:

```java
public class TicketCommandDefinition implements CommandDefinition {
  @CommandMethod(path = "ticket open", 
    requiredArgs = 1, 
    permNode = "tickets.open", 
    helpMsg = "Usage: /ticket open [message]")
  public void openTicket(Player p, String[] args) {
    //logic here
  }

  @CommandMethod(path = "ticket close", 
    requiredArgs = 1, 
    permNode = "tickets.close", 
    helpMsg = "Usage: /ticket close [id]")
  public void closeTicket(CommandSender s, Integer id) {
    //logic here
  }

  @CommandMethod(path = "ticket list", 
    permNode = "tickets.list", 
    helpMsg = "Usage: /ticket list")
  public void listTickets() {
    //logic here
  }
}
```

Make sure that you add the "ticket" command to your plugin.yml!!!

Then, in onEnable() of your JavaPlugin subclass,

```java
onEnable() {
  //...
  TreeCommandExecutor tree = new TreeCommandExecutor();
  TreeTabCompleter completer = new TreeTabCompleter(tree); //only if you want free tab completion, see below

  tree.add(new TicketCommandDefinition());

  PluginCommand cmd = getCommand("ticker");
  cmd.setExecutor(tree);
  cmd.setTabCompleter(completer); //only if you want free tab completion, see below
  //...
}
```

And you're set. Hop in-game and try it out. The commands this makes are:

* /ticket open
* /ticket close
* /ticket list

@CommandMethod has several arguments: 

* path - the command the player will type to run the method, minus the leading /. Required.
* permNode - the permission node the player needs to be able to run the command. Default none.
* requiredArgs - the number of arguments that are guaranteed to be in the args array. If the player doesn't provide enough args, they will get the contents of helpMsg printed for them. Default 0.
* helpMsg - The message printed to a player if they don't provide enough arguments. Default "No help available."

# Requirements

Methods annotated with @CommandMethod must conform to these rules:

* Method must be public
* Method must have either 0 or 1 parameters in the set {CommandSender, ConsoleCommandSender, Player}
* Any parameter that is a CommandSender, ConsoleCommandSender, or Player, must be the first parameter
* Method must have either 0 or 1 parameters of type String[]
* Any parameter is a String[] must be the last parameter
* The types of all parameters must be in the set {Player, ConsoleCommandSender, CommandSender, Integer, Long, Float, Double, Boolean, Character, Byte, Short, String, String[]}

# Type Coercion

Reflective can automatically parse strings into any of the eight boxed primitive types listed above (in Requirements), as well as String itself.

# Sender Checking

Reflective will check the runtime type of the CommandSender against what you specify in your method's parameter list. If you used Player, only players can run the command. If you used ConsoleCommandSender, then only console can run that command. If you used CommandSender, then either can run that command.

# Optional Arguments

Reflective can handle optional arguments. For instance:

```java
@CommandMethod(path = "cmd", requiredArgs = 1)
public void myCmd(Player p, Integer requiredInt, Float optionalFloat) {
  // NOTE: The names of the identifiers are not important
  // The order, from left to right, is what's important!
  //...
}
```

If the player types "/cmd 1", then optionalFloat will be null. If the player types "/cmd 1 2.2" then the float will contain 2.2.


# Permission Checking

Reflective can also handle multiple permission nodes per command. If permNode="node.one node.two node.three" then if a user has node.one OR node.two OR node.three, then they will be able to run the command.

# Opt-out

While the framework can do a lot of args-count-checking, sender-type (console/player) checking, and permissions checking for you, you are of course free to ignore them and do your own checks.

# Bugs

Open an issue here on GitHub.

# Contributing

I'm always open to pull requests, bug reports, comments, criticism, etc. All contributed code must be licensed under the GPLv3+.
