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
package cafe.josh.reflective.test;

import java.math.BigInteger;
import cafe.josh.reflective.CommandDefinition;
import cafe.josh.reflective.annotation.CommandMethod;
import org.bukkit.command.CommandSender;
import org.junit.Assert;

/**
 *
 * @author joshua
 */
public class SampleVariadicCommandDefinition implements CommandDefinition {
    private String ran;
    private int restArrSize;

    public int getRestArrSize() {
        return restArrSize;
    }

    @CommandMethod(path = "sample cmd1")
    public void simpleCoercionCommand(CommandSender s, Integer arg1) {
        System.out.println("Got " + arg1 + " and it ++ is " + (arg1 + 1));
        ran = "simpleCoercionCommand";
    }

    @CommandMethod(path = "sample cmd2")
    public void coercionWithSender(CommandSender s, Integer arg1, String arg2, Float arg3, Boolean arg4) {
        System.out.println("woo: " + arg1 + ", " + arg2 + ", " + arg3 + ", " + arg4);
        ran = "coercionWithSender";
    }

    @CommandMethod(path = "sample cmd3")
    public void coercionWithStringArr(CommandSender s, Integer arg1, Integer arg2, String[] rest) {
        ran = "coercionWithStringArr";
        restArrSize = rest.length;
    }

    @CommandMethod(path = "sample invalid1")
    public void invalidParamType(BigInteger arg1, String arg2) {
        System.out.println("/sample invalid1 should not run!!");
        ran = "invalidParamType";
    }

    @CommandMethod(path = "sample invalid2")
    public void invalidStringArrPos(String[] rest, String arg2, Float arg3, Boolean arg4) {
        System.out.println("/sample invalid2 should not run!!");
        ran = "invalidStringArrPos";
    }

    @CommandMethod(path = "sample invalid3")
    public void invalidSenderPos(String arg2, Float arg3, CommandSender s, Boolean arg4) {
        System.out.println("/sample invalid3 should not run!!");
        ran = "invalidSenderPos";
    }

    @CommandMethod(path = "sample notEnoughArgs", requiredArgs = 2)
    public void invalidSenderPos(String arg1, Float arg2) {
        ran = "invalidSenderPos";
    }

    @CommandMethod(path = "sample missingOptionalArgs", requiredArgs = 1)
    public void missingOptionalArgs(String arg1, Integer arg2) {
        ran = "missingOptionalArgs";
        Assert.assertNotNull(arg1);
        Assert.assertNull(arg2);
    }

    public String getRan() {
        return ran;
    }
}
