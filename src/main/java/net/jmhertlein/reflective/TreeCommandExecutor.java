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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.jmhertlein.reflective.annotation.CommandMethod;
import net.jmhertlein.reflective.io.DotWriter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author joshua
 */
public class TreeCommandExecutor implements CommandExecutor {

    private CommandNode root;
    private Set<CommandLeaf> leaves;

    /**
     * Creates a new instance of a TreeCommandExecutor
     *
     * It is ready to have leaves added to it and to be set as the executor for a command
     */
    public TreeCommandExecutor() {
        root = new CommandNode(null, "/");
        leaves = new HashSet<>();
    }

    public void add(final CommandDefinition c) {
        Method[] methods = c.getClass().getMethods();

        for(final Method m : methods) {
            final CommandMethod cmdInfo = (CommandMethod) m.getAnnotation(CommandMethod.class);
            if(cmdInfo == null) {
                continue;
            }

            CommandLeaf f = new CommandLeaf(cmdInfo, m, c);
            add(f);
        }
    }

    /**
     *
     * @param cmd the commandleaf to be added
     *
     * @throws RuntimeException if a duplicate command is added
     */
    private void add(CommandLeaf cmd) {
        CommandNode temp = root;
        for(String s : cmd.getStringNodes()) {
            CommandNode next = temp.getChild(s);

            if(next == null) {
                next = new CommandNode(temp, s);
                temp.addChild(next);
            }

            temp = next;
        }

        if(temp.executable != null) {
            throw new RuntimeException("Error: leaf node already has command bound");
        }

        temp.executable = cmd;
        leaves.add(cmd);
    }

    /**
     *
     * @param sender
     * @param command
     * @param label
     * @param args
     *
     * @return
     */
    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        TraversalResult r = traverseToEnd(command.getName(), args);

        CommandNode selectedLeaf = r.node;
        int i = r.argsPosition;

        if(selectedLeaf == null) {
            sendInvalidCommandHelp(sender, root, selectedLeaf, args[i]);
            return true;
        }
        //once we reach the end, assume the rest of the stuff in args are actually arguments
        if(selectedLeaf.executable == null) {
            sendIncompleteCommandHelp(sender, root, selectedLeaf);
            return true;
        }

        String[] cmdArgs = new String[args.length - i];
        System.arraycopy(args, i, cmdArgs, 0, cmdArgs.length);

        if(cmdArgs.length < selectedLeaf.executable.getNumRequiredArgs()) {
            sender.sendMessage(selectedLeaf.executable.getMissingRequiredArgsHelpMessage());
            return true;
        }
        try {
            selectedLeaf.executable.execute(sender, command, cmdArgs);
        } catch(InsufficientPermissionException ex) {
            if(ex.hasCustomMessage()) {
                sender.sendMessage(ChatColor.RED + ex.getCustomMessage());
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run this command.");
            }
            return true;
        } catch(UnsupportedCommandSenderException ex) {
            sender.sendMessage(ChatColor.RED + ex.getLocalizedMessage());
            return true;
        }

        return true;
    }

    private static List<String> composeChildNodesString(CommandNode selectedLeaf) {
        List<String> ret = new LinkedList<>();
        selectedLeaf.children.values().stream().forEach((child) -> {
            ret.add(child.nodeString);
        });

        return ret;
    }

    /**
     *
     * @return
     */
    public Set<CommandLeaf> getLeaves() {
        return Collections.unmodifiableSet(leaves);
    }

    private static void sendIncompleteCommandHelp(CommandSender sender, CommandNode root, CommandNode selectedLeaf) {
        //they didn't type a complete command, so tell them what they might want to type next
        //print children node strings
        sender.sendMessage(ChatColor.RED + "Incomplete command: \"/" + composeCommandParentage(root, selectedLeaf) + "\"");
        sender.sendMessage(ChatColor.YELLOW + "Possible completions:");
        composeChildNodesString(selectedLeaf).stream().forEach((s) -> {
            sender.sendMessage(ChatColor.AQUA + s);
        });
    }

    private static void sendInvalidCommandHelp(CommandSender sender, CommandNode root, CommandNode currentNode, String invalidNodeString) {
        sender.sendMessage(String.format("%sInvalid command: \"%s%s%s\"", ChatColor.RED, ChatColor.DARK_RED, composeCommandParentage(root, currentNode) + " " + invalidNodeString, ChatColor.RED));
        sender.sendMessage(String.format("%sPossible replacements for \"%s%s%s\"", ChatColor.YELLOW, ChatColor.DARK_RED, invalidNodeString, ChatColor.YELLOW));
        composeChildNodesString(currentNode).stream().forEach((s) -> {
            sender.sendMessage(ChatColor.AQUA + s);
        });
    }

    private static String composeCommandParentage(CommandNode root, CommandNode n) {
        return n.parent == root ? n.nodeString : composeCommandParentage(root, n.parent) + " " + n.nodeString;
    }

    public List<String> getTabCompletions(String name, String[] args) {
        TraversalResult r = traverseToEnd(name, args);

        if(r.argsPosition < args.length - 1) {
            return Collections.EMPTY_LIST;
        } else {
            List<String> ret;
            //if there's exactly one token remaining, try to filter
            if(r.argsPosition == args.length - 1) {
                ret = r.node.children.keySet().stream()
                        .filter(s -> s.startsWith(args[r.argsPosition]))
                        .collect(Collectors.toList());
            } else {
                ret = new ArrayList<>(r.node.children.keySet().size());
                ret.addAll(r.node.children.keySet());
            }

            return ret;
        }
    }

    /**
     * Traverses as far down the tree as it can go
     *
     * @param name the command's actual name, i.e. Command#getName()
     * @param args the args the user typed, used to traverse tree
     * @return the last node that could be matched + the index that failed to match any further
     * nodes (either because there were no more nodes or none of them matched)
     */
    private TraversalResult traverseToEnd(String name, String[] args) {
        CommandNode cur = null, next = root.getChild(name);

        int i = -1; //Bukkit wants name and args separate, so the thing before args[0] is name, which we just processed
        while(next != null) {
            i++;
            cur = next;
            if(!cur.children.isEmpty() && i < args.length) {
                next = cur.getChild(args[i]);
            } else {
                next = null;
            }
        }

        //The contract of argsPosition is that it's the beginning pos in args[] that's args,
        //so we shouldn't return -1
        return new TraversalResult(cur, cur == null ? 0 : i);
    }

    private static class TraversalResult {

        /**
         * The last node that was able to be matched
         */
        CommandNode node;
        /**
         * The beginning (inclusive) of the sub-array in args that did not get used when traversing
         * to node.
         *
         * i.e. these are not part of the command, they're arguments or the incorrect trailing part
         * of a malformed command
         */
        int argsPosition;

        public TraversalResult(CommandNode node, int argsPosition) {
            this.node = node;
            this.argsPosition = argsPosition;
        }
    }

    private class CommandNode {

        CommandNode parent;
        CommandLeaf executable;
        String nodeString;
        Map<String, CommandNode> children;

        public CommandNode() {
            children = new HashMap<>();
        }

        public CommandNode(CommandNode parent, String nodeString) {
            this.parent = parent;
            this.nodeString = nodeString;
            children = new HashMap<>();
        }

        public void addChild(CommandNode n) {
            children.put(n.nodeString, n);
        }

        public CommandNode getChild(String nodeString) {
            return children.get(nodeString);
        }
    }

    public void writeToGraph(DotWriter w) {
        writeToGraph(w, root);
    }

    private void writeToGraph(DotWriter w, CommandNode n) {
        String curNode = n.nodeString + Objects.hashCode(n);
        w.printLabel(curNode, n.nodeString);
        if(n.children.isEmpty())
            return;

        for(CommandNode child : n.children.values()) {
            String childNode = child.nodeString + Objects.hashCode(child);
            w.printEdge(curNode, childNode);
            writeToGraph(w, child);
        }
    }
}
