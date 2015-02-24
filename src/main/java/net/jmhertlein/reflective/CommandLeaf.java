/*
 * Copyright (C) 2013 Joshua Michael Hertlein <jmhertlein@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jmhertlein.reflective;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import net.jmhertlein.reflective.annotation.CommandMethod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * A CommandLeaf is the executable leaf of a tree of commands, and represents the actual command.
 *
 * @author joshua
 */
public class CommandLeaf {

    private final String[] nodeStrings;
    private final CommandMethod info;
    private final Method m;
    private final CommandDefinition caller;

    /**
     * Creates a new CommandLeaf from meta-information.
     *
     * If your CommandDefinition class has only static methods, then d can be null
     *
     * @param info - meta-information about the command
     * @param m - the method to invoke to run the command
     * @param d - the CommandDefinition that m belongs to
     */
    public CommandLeaf(CommandMethod info, Method m, CommandDefinition d) {
        this.nodeStrings = info.path().split(" ");
        this.m = m;
        this.caller = d;
        this.info = info;

        if(nodeStrings.length == 0) {
            throw new RuntimeException("Error: command's path is zero-length");
        }
    }

    /**
     *
     * @return how many required arguments the leaf requires
     */
    public int getNumRequiredArgs() {
        return info.requiredArgs();
    }

    /**
     *
     * @param index the index of the string to retrieve (0 is the first string)
     *
     * @return The string at position 'index' of the command string (no arguments included)
     */
    public String getStringAt(int index) {
        return index < nodeStrings.length ? nodeStrings[index] : null;
    }

    /**
     *
     * @return an unmodifiable list of all substrings in the command string (space-delimited)
     */
    public List<String> getStringNodes() {
        return Collections.unmodifiableList(Arrays.asList(nodeStrings));
    }

    /**
     * The analog to CommandExecutor::onCommand(). This is called when a player has successfully
     * type the command and supplied enough required args
     *
     * @param sender the CommandSender executing the command
     * @param cmd
     * @param args required arguments and optional arguments, required arguments first.
     *
     * @throws InsufficientPermissionException if the sender doesn't have sufficient permission to
     * run the command
     * @throws UnsupportedCommandSenderException if the sender is not able to run the command
     * (example: sender is console instead of Player)
     */
    public void execute(CommandSender sender, Command cmd, String[] args) throws InsufficientPermissionException, UnsupportedCommandSenderException {
        if(!info.permNode().isEmpty() && !sender.hasPermission(info.permNode())) {
            throw new InsufficientPermissionException();
        }

        try {
            Type[] t = m.getParameterTypes();
            Object[] reflectiveArgs = new Object[t.length];

            int paramPos = 0;
            if(t[0] == CommandSender.class || t[0] == Player.class || t[0] == ConsoleCommandSender.class) {
                paramPos = 1;
                reflectiveArgs[0] = sender;

                if(t[0] == ConsoleCommandSender.class && !(sender instanceof ConsoleCommandSender)) {
                    throw new UnsupportedCommandSenderException(sender);
                } else if(t[0] == Player.class && !(sender instanceof Player)) {
                    throw new UnsupportedCommandSenderException(sender);
                }
            }

            paramLoop:
            for(int argsPos = 0; paramPos < t.length && argsPos < args.length; paramPos++, argsPos++) {
                try {
                    if(t[paramPos] == Integer.class) {
                        reflectiveArgs[paramPos] = Integer.parseInt(args[argsPos]);
                    } else if(t[paramPos] == Long.class) {
                        reflectiveArgs[paramPos] = Long.parseLong(args[argsPos]);
                    } else if(t[paramPos] == Float.class) {
                        reflectiveArgs[paramPos] = Float.parseFloat(args[argsPos]);
                    } else if(t[paramPos] == Double.class) {
                        reflectiveArgs[paramPos] = Double.parseDouble(args[argsPos]);
                    } else if(t[paramPos] == Boolean.class) {
                        reflectiveArgs[paramPos] = strictParseBoolean(args[argsPos]);
                    } else if(t[paramPos] == Character.class) {
                        if(args[argsPos].length() == 1)
                            reflectiveArgs[paramPos] = args[argsPos].charAt(0);
                        else
                            throw new IllegalArgumentException(args[argsPos] + " must be a single character.");
                    } else if(t[paramPos] == Byte.class) {
                        reflectiveArgs[paramPos] = Byte.parseByte(args[argsPos]);
                    } else if(t[paramPos] == Short.class) {
                        reflectiveArgs[paramPos] = Short.parseShort(args[argsPos]);
                    } else if(t[paramPos] == String.class) {
                        reflectiveArgs[paramPos] = args[argsPos];
                    } else if(t[paramPos] == String[].class) {
                        int remaining = args.length - argsPos;
                        String[] leftover = new String[remaining];
                        System.arraycopy(args, argsPos, leftover, 0, remaining);
                        reflectiveArgs[paramPos] = leftover;
                        if(paramPos != t.length - 1) {
                            throw newComplaintAboutParams(m);
                        }
                    } else {
                        throw newComplaintAboutParams(m);
                    }
                } catch(IllegalArgumentException ex) {
                    sender.sendMessage("Error converting \"" + args[argsPos] + "\" to " + t[paramPos].getTypeName() + ": " + ex.getLocalizedMessage());
                }
            }

            /**
             * We'll be kind enough to not split hairs over a null array vs empty array. The
             * String[] representing the rest of the args will *never* be null if it is present.
             */
            if(t[t.length - 1] == String[].class && reflectiveArgs[reflectiveArgs.length - 1] == null) {
                reflectiveArgs[reflectiveArgs.length - 1] = new String[0];
            }

            m.invoke(caller, reflectiveArgs);
        } catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(TreeCommandExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return the message to be sent to the user if they correctly type the command, but don't
     * supply enough required arguments
     */
    public String getMissingRequiredArgsHelpMessage() {
        return info.helpMsg().length() == 0 ? composeUsageMessage(m) : info.helpMsg();
    }

    private static UnsupportedParameterException newComplaintAboutParams(Method m) {
        return new UnsupportedParameterException("ERROR: Method " + m.getName() + " of class " + m.getClass().getName() + " has unsupported parameters.");
    }

    public static class UnsupportedParameterException extends RuntimeException {

        public UnsupportedParameterException(String message) {
            super(message);
        }

    }

    /**
     * Parses a string into a boolean, but only "true" parses to true and only "false" parses to
     * false. All other inputs throw an exceptions.
     *
     * @param s
     * @return the boolean value of s
     * @throws IllegalArgumentException if the string is not "true" or "false"
     */
    private static Boolean strictParseBoolean(String s) {
        if(s.equalsIgnoreCase("true"))
            return true;
        else if(s.equalsIgnoreCase("false"))
            return false;
        else
            throw new IllegalArgumentException(s + " must be \"true\" or \"false\"");
    }

    private static String composeUsageMessage(Method m) {
        String path = m.getAnnotation(CommandMethod.class).path();
        List<Parameter> params = Arrays.asList(m.getParameters());
        String args = params.stream()
                .filter(p -> !isSenderType(p.getType()))
                .map(p -> "<" + p.getName() + ":" + p.getType().getSimpleName() + ">")
                .reduce("", (acc, s) -> acc + " " + s);

        return "Usage: /" + path + args;
    }

    private static boolean isSenderType(Class<?> c) {
        return c == Player.class || c == CommandSender.class || c == ConsoleCommandSender.class;
    }
}
