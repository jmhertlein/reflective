/*
 * Copyright (C) 2015 Joshua Michael Hertlein
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
package net.jmhertlein.reflective.test;

import net.jmhertlein.reflective.CommandDefinition;
import net.jmhertlein.reflective.annotation.CommandMethod;
import org.bukkit.command.CommandSender;

/**
 *
 * @author joshua
 */
public class SampleVariadicCommandDefinition implements CommandDefinition {
    private Class[] typesReceived;

    @CommandMethod(path = "sample cmd1", console = true)
    public void cmd1(CommandSender s, int arg1) {
        System.out.println("Got " + arg1 + " and it ++ is " + (arg1 + 1));
        typesReceived = new Class[]{int.class};
    }

    @CommandMethod(path = "sample cmd2", console = true)
    public void cmd2(CommandSender s, int arg1, String arg2, float arg3, boolean arg4) {
        System.out.println("woo: " + arg1 + ", " + arg2 + ", " + arg3 + ", " + arg4);
        typesReceived = new Class[]{int.class, String.class, float.class, boolean.class};
    }

    public Class[] getReceivedTypes() {
        return typesReceived;
    }
}
