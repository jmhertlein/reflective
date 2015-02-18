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
package net.jmhertlein.reflective.test;

import java.util.Collection;
import java.util.HashSet;
import net.jmhertlein.reflective.TreeCommandExecutor;
import net.jmhertlein.reflective.TreeTabCompleter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author joshua
 */
public class CommandDefinitionTest {
    private TreeCommandExecutor e;
    private SampleCommandDefinition d;

    @Before
    public void setUp() {
        e = new TreeCommandExecutor();
        d = new SampleCommandDefinition();
        e.add(d);
    }

    @After
    public void tearDown() {
        e = null;
        d = null;
    }

    @Test
    public void testSimple() {
        e.onCommand(new MockCommandSender(), new MockCommand("sayone"), "sayone", new String[0]);
        assertEquals(d.getRan(), "sayone");
    }

    @Test
    public void testLongs() {
        e.onCommand(new MockCommandSender(), new MockCommand("this"), "this", new String[]{"one", "is", "really", "pretty", "long"});
        assertEquals(d.getRan(), "longcmd");

        e.onCommand(new MockCommandSender(), new MockCommand("this"), "this", new String[]{"one", "is", "really", "pretty", "different"});
        assertEquals(d.getRan(), "longcmddiff");
    }

    @Test
    public void testEchoNoArgs() {
        e.onCommand(new MockCommandSender(), new MockCommand("echo"), "echo", new String[0]);
        assertNull(d.getRan());
    }

    @Test
    public void testEchoWithArgs() {
        e.onCommand(new MockCommandSender(), new MockCommand("echo"), "echo", new String[]{"echo", "this,", "machine!"});
        assertEquals(d.getRan(), "echo");
    }

    @Test
    public void testLongWithArgs() {
        e.onCommand(new MockCommandSender(), new MockCommand("long"), "long", new String[]{"command", "path", "arg1", "arg2", "arg3"});
        assertEquals(d.getRan(), "longcmdwithargs");
        assertEquals(d.getArgsPassed(), 3);
    }

    @Test
    public void testInvalidParams() {
        boolean exceptionThrown = false;
        e.add(new SampleInvalidCommandDefinition());
        try {
            e.onCommand(new MockCommandSender(), new MockCommand("invalid"), "invalid", new String[0]);
        } catch(RuntimeException ex) {
            System.out.println("testInvalidParams() correctly threw an exception!");
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testSingleParamMethods() {
        e.onCommand(new MockCommandSender(), new MockCommand("param"), "param", new String[]{"args"});
        assertEquals(d.getRan(), "paramargs");

        e.onCommand(new MockCommandSender(), new MockCommand("param"), "param", new String[]{"sender"});
        assertEquals(d.getRan(), "paramsender");
    }

    @Test
    public void testPrefixedCommands() {
        e.onCommand(new MockCommandSender(), new MockCommand("prefixed"), "prefixed", new String[]{"command"});
        assertEquals(d.getRan(), "prefixedCommand");
        e.onCommand(new MockCommandSender(), new MockCommand("prefixed"), "prefixed", new String[]{"command", "arg1", "arg2"});
        assertEquals(d.getRan(), "prefixedCommand");
        assertEquals(d.getArgsPassed(), 2);
        e.onCommand(new MockCommandSender(), new MockCommand("prefixed"), "prefixed", new String[]{"command", "exec", "arg1"});
        assertEquals(d.getRan(), "prefixedCommandExec");
        assertEquals(d.getArgsPassed(), 1);
    }

    @Test
    public void testTabCompletion() {
        TreeTabCompleter l = new TreeTabCompleter(e);
        Collection<String> res = l.onTabComplete(null, new MockCommand("this"), null, new String[]{"one", "is", "really", "pretty"});

        Collection<String> expected = new HashSet<>();
        expected.add("different");
        expected.add("long");

        assertEquals(expected.size(), res.size());
        for(String s : res) {
            assertTrue(expected.contains(s));
        }
    }

    @Test
    public void testTabCompletionPartial() {
        TreeTabCompleter l = new TreeTabCompleter(e);
        Collection<String> res = l.onTabComplete(null, new MockCommand("this"), null, new String[]{"one", "is", "really", "pretty", "di"});

        Collection<String> expected = new HashSet<>();
        expected.add("different");

        assertEquals(expected.size(), res.size());
        for(String s : res) {
            assertTrue(expected.contains(s));
        }
    }
}
