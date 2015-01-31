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
package net.jmhertlein.core.abcf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jmhertlein.core.abcf.annotation.CommandMethod;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A CommandLeaf is the executable leaf of a tree of commands, and represents
 * the actual command.
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
     * If your CommandDefinition class has only static methods,
     * then d can be null
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

        if (nodeStrings.length == 0) {
            throw new RuntimeException("Invalid command: Must have at least one non-argument string.");
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
     * @return The string at position 'index' of the command string (no
     * arguments included)
     */
    public String getStringAt(int index) {
        return index < nodeStrings.length ? nodeStrings[index] : null;
    }

    /**
     *
     * @return an unmodifiable list of all substrings in the command string
     * (space-delimited)
     */
    public List<String> getStringNodes() {
        return Collections.unmodifiableList(Arrays.asList(nodeStrings));
    }

    /**
     * The analogue to CommandExecutor::onCommand(). This is called when a
     * player has successfully type the command and supplied enough required
     * args
     *
     * @param sender the CommandSender executing the command
     * @param cmd
     * @param args required arguments and optional arguments, required arguments
     * first.
     *
     * @throws InsufficientPermissionException if the sender doesn't have
     * sufficient permission to run the command
     * @throws UnsupportedCommandSenderException if the sender is not able to
     * run the command (example: sender is console instead of Player)
     */
    public void execute(CommandSender sender, Command cmd, String[] args) throws InsufficientPermissionException, UnsupportedCommandSenderException {
        if (!info.permNode().isEmpty() && !sender.hasPermission(info.permNode())) {
            throw new InsufficientPermissionException();
        }

        boolean isPlayer = sender instanceof Player;
        if ((isPlayer && !info.player()) || (!isPlayer && !info.console())) {
            throw new UnsupportedCommandSenderException(sender);
        }

        try {
            Type[] t = m.getParameterTypes();
            if(t.length == 0) {
                m.invoke(caller, null);
                
            } else if(t.length == 1) {
                if(t[0].equals(CommandSender.class)) {
                    m.invoke(caller, sender);
                } else if(t[0].equals(String[].class)) {
                    m.invoke(caller, (Object) args);
                } else {
                    throw newComplaintAboutParams(m);
                }
                
            } else if(t.length == 2) {
                if(t[0].equals(CommandSender.class) && t[1].equals(String[].class)) {
                    m.invoke(caller, sender, args);
                } else {
                    throw newComplaintAboutParams(m);
                }
                
            } else {
                throw newComplaintAboutParams(m);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(TreeCommandExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return the message to be sent to the user if they correctly type the
     * command, but don't supply enough required arguments
     */
    public String getMissingRequiredArgsHelpMessage() {
        return info.helpMsg();
    }
    
    private static RuntimeException newComplaintAboutParams(Method m) {
        return new RuntimeException("ERROR: Method " + m.getName() + " of class " + m.getClass().getName() + " has unsupported parameters. "
                        + "Supported parameters are: (), (CommandSender s), (String[] args), (CommandSender s, String[] args).");
    }
}
