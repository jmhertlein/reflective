/*
 * Copyright (C) 2015 Joshua Michael Hertlein <jmhertlein@gmail.com>
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
package cafe.josh.reflective.test;

import cafe.josh.reflective.CommandDefinition;
import cafe.josh.reflective.annotation.CommandMethod;
import org.bukkit.command.CommandSender;

/**
 *
 * @author joshua
 */
public class SampleCommandDefinition implements CommandDefinition {
    private String ran;
    private int argsPassed;

    @CommandMethod(path = "say hi")
    public void sayHi() {
        System.out.println("Hi");
        ran = "sayhi";
    }

    @CommandMethod(path = "this one is really pretty long")
    public void longCmd(CommandSender s, String[] args) {
        ran = "longcmd";
    }

    @CommandMethod(path = "this one is really pretty different")
    public void longCmdDiff(CommandSender s, String[] args) {
        ran = "longcmddiff";
    }

    @CommandMethod(path = "long command path", requiredArgs = 3)
    public void longCmdWithArgs(CommandSender s, String[] args) {
        System.out.println("I was called with args:");
        for(String arg : args) {
            System.out.println(arg);
        }
        ran = "longcmdwithargs";
        argsPassed = args.length;
    }

    public int getArgsPassed() {
        return argsPassed;
    }

    @CommandMethod(path = "say bye")
    public void sayBye(CommandSender s, String[] args) {
        System.out.println("Bye");
        ran = "saybye";
    }

    @CommandMethod(path = "sayone")
    public void sayOne(CommandSender s, String[] args) {
        System.out.println("One");
        ran = "sayone";
    }

    @CommandMethod(path = "echo", requiredArgs = 1, helpMsg = "This is help for echo. Usage: /echo <message>")
    public void echo(CommandSender sender, String[] args) {
        StringBuilder b = new StringBuilder();
        for(String s : args) {
            b.append(s);
            b.append(' ');
        }
        System.out.println(b.toString());
        ran = "echo";
    }

    @CommandMethod(path = "param args")
    public void paramArgs(String[] args) {
        ran = "paramargs";
    }

    @CommandMethod(path = "param sender")
    public void paramSender(CommandSender sender) {
        ran = "paramsender";
    }

    @CommandMethod(path = "prefixed command")
    public void prefixedCommand(String[] args) {
        ran = "prefixedCommand";
        argsPassed = args.length;
    }

    @CommandMethod(path = "prefixed command exec", requiredArgs = 1)
    public void prefixedCommandExec(String[] args) {
        ran = "prefixedCommandExec";
        argsPassed = args.length;
    }

    public String getRan() {
        return ran;
    }

}
