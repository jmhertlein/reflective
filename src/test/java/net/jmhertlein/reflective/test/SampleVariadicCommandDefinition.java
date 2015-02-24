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

import java.math.BigInteger;
import net.jmhertlein.reflective.CommandDefinition;
import net.jmhertlein.reflective.annotation.CommandMethod;
import org.bukkit.command.CommandSender;

/**
 *
 * @author joshua
 */
public class SampleVariadicCommandDefinition implements CommandDefinition {
    private Class[] typesReceived;
    private int restArrSize;

    public int getRestArrSize() {
        return restArrSize;
    }

    @CommandMethod(path = "sample cmd1")
    public void simpleCoercionCommand(CommandSender s, int arg1) {
        System.out.println("Got " + arg1 + " and it ++ is " + (arg1 + 1));
        typesReceived = new Class[]{int.class};
    }

    @CommandMethod(path = "sample cmd2")
    public void coercionWithSender(CommandSender s, int arg1, String arg2, float arg3, boolean arg4) {
        System.out.println("woo: " + arg1 + ", " + arg2 + ", " + arg3 + ", " + arg4);
        typesReceived = new Class[]{int.class, String.class, float.class, boolean.class};
    }

    @CommandMethod(path = "sample cmd3")
    public void coercionWithStringArr(CommandSender s, int arg1, int arg2, String[] rest) {
        typesReceived = new Class[]{int.class, int.class, String[].class};
        restArrSize = rest.length;
    }

    @CommandMethod(path = "sample invalid1")
    public void invalidParamType(BigInteger arg1, String arg2) {
        System.out.println("/sample invalid1 should not run!!");
    }

    @CommandMethod(path = "sample invalid2")
    public void invalidStringArrPos(String[] rest, String arg2, float arg3, boolean arg4) {
        System.out.println("/sample invalid2 should not run!!");
    }

    @CommandMethod(path = "sample invalid3")
    public void invalidSenderPos(String arg2, float arg3, CommandSender s, boolean arg4) {
        System.out.println("/sample invalid3 should not run!!");
    }

    /**
     * I'm intentionally leaving the requiredArgs annotation argument out to test how reflective
     * handles auto-detecting required args
     *
     * @param arg1
     * @param arg2 this float will be missing in the test
     */
    @CommandMethod(path = "sample notEnoughArgs")
    public void invalidSenderPos(String arg1, float arg2) {
        typesReceived = new Class[]{String.class, float.class};
    }

    public Class[] getReceivedTypes() {
        return typesReceived;
    }
}
