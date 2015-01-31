# ABCF - A Better (Bukkit) Command Framework

ABCF is a command framework for Minecraft server plugins that use the Spigot fork of the Bukkit API.

ABCF focuses on simplifying the process of handling commands. It offers a CommandExecutor implementation (TreeExecutor) that you add CommandDefinition instances to.

Your plugin makes CommandDefinition implementations and marks methods with the @CommandMethod annotation. This lets the TreeExecutor register them.

The 'root' of each command still needs to be in your plugin.yml.

# License

GPLv3, check LICENSE or COPYING for more details. Note that this is the full GPL- to use this library, your plugin **must** also be licensed under the GPLv3+. To clarify, that means your plugin will be open source, and fully-fledged Free Software that anyone can use, copy, modify, and redistribute.

# Example

The most important classes are:

* net.jmhertlein.abcf.TreeCommandExecutor - a CommandExecutor implementation pre-made for you
* net.jmhertlein.abcf.CommandDefinition - a marker interface that you'll made an implementation of
* net.jmhertlein.abcf.CommandMethod - an annotation (one of those @ things above methods) to mark a method as a command

Suppose we have a simple ticket-handling plugin. The CommandDefinition might look like this:

    public class TicketCommandDefinition implements CommandDefinition {
      @CommandMethod(path = "ticket open", 
        requiredArgs = 1, 
        permNode = "tickets.open", 
        helpMsg = "Usage: /ticket open [message]")
      public void openTicket(CommandSender s, String[] args) {
        //logic here
      }
    
      @CommandMethod(path = "ticket close", 
        requiredArgs = 1, 
        permNode = "tickets.close", 
        helpMsg = "Usage: /ticket close [id]")
      public void closeTicket(String[] args) {
        int id = Integer.parseInt(args[0]);
        //logic here
      }
    
      @CommandMethod(path = "ticket list", 
        permNode = "tickets.list", 
        helpMsg = "Usage: /ticket list", 
        console = true)
      public void listTickets() {
        //logic here
      }
    }

Make sure that you add the "ticket" command to your plugin.yml!!!

Then, in onEnable() of your JavaPlugin subclass,

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

And you're set. Hop in-game and try it out. The commands this makes are:

* /ticket open
* /ticket close
* /ticket list

@CommandMethod has several arguments: 

* path - the command the player will type to run the method, minus the leading /. Required.
* console - whether or not the console can run the command. Default false.
* player - whether or not players can run the command. Default true.
* permNode - the permission node the player needs to be able to run the command. Default none.
* requiredArgs - the number of arguments that are guaranteed to be in the args array. If the player doesn't provide enough args, they will get the contents of helpMsg printed for them. Default 0.
* helpMsg - The message printed to a player if they don't provide enough arguments. Default "No help available."

# Requirements

Methods annotated with @CommandMethod must:

* Be public
* Have formal parameter lists of one of the following: (), (CommandSender), (String[]), (CommandSender, String[]). 

# Options

While the framework can do a lot of args-count-checking, sender-type (console/player) checking, and permissions checking for you, you are of course free to ignore them and do your own checks.

# Bonus Features

* FREE Compile-time checking of your CommandMethod-annotated methods to make sure they have the right visibility modifier and parameters (You will need to enable this in your IDE or maven- the annotation processor is net.jmhertlein.core.abcf.processor.CommandMethodProcessor)
* FREE tab-completion for all commands registered with the TreeCommandExecutor. Just make a TreeTabExecutor and set it as the command's tab completer (see code snipped above).

# Future Work

With reflection, there are a lot of possibilities. One thing I'm strongly considering doing is letting you define a method like

    public void someCommand(CommandSender s, int arg1, String arg2, boolean arg3) //and so on

and the framework will try to parse the input array into the types you want. After all, the point of this is to make writing commands take as little duplicated code/logic as possible.

# Bugs

Open an issue here on GitHub.

# Contributing

I'm always open to pull requests, bug reports, comments, criticism, etc. All code must be licensed under the GPLv3+, no exceptions.
