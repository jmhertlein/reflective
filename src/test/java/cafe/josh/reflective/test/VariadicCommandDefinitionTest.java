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

import cafe.josh.reflective.TreeCommandExecutor;
import cafe.josh.reflective.CommandLeaf.UnsupportedParameterException;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author joshua
 */
public class VariadicCommandDefinitionTest {
    private TreeCommandExecutor e;
    private SampleVariadicCommandDefinition d;

    @Before
    public void setUp() {
        e = new TreeCommandExecutor();
        d = new SampleVariadicCommandDefinition();
        e.add(d);
    }

    @After
    public void tearDown() {
        e = null;
        d = null;
    }

    @Test
    public void testSimpleCoercionCommand() {
        e.onCommand(new MockCommandSender(), new MockCommand("sample"), "sample", new String[]{"cmd1", "100"});
        assertEquals("simpleCoercionCommand", d.getRan());
    }

    @Test
    public void testCoercionWithSender() {
        e.onCommand(new MockCommandSender(), new MockCommand("sample"), "sample", new String[]{"cmd2", "100", "muh str", "10.25", "false"});
        assertEquals("coercionWithSender", d.getRan());
    }

    @Test
    public void testCoercionWithStringArr() {
        e.onCommand(new MockCommandSender(), new MockCommand("sample"), "sample", new String[]{"cmd3", "100", "200", "more", "stuff", "here"});
        assertEquals("coercionWithStringArr", d.getRan());
        assertEquals(d.getRestArrSize(), 3);
    }

    @Test
    public void testInvalidParamType() {
        boolean thrown = false;
        try {
            e.onCommand(new MockCommandSender(), new MockCommand("sample"), "sample", new String[]{"invalid1", "100", "muh str"});
        } catch(UnsupportedParameterException ex) {
            thrown = true;
            System.out.println("Correctly threw: " + ex.getLocalizedMessage());

        }
        assertTrue(thrown);
        assertNull(d.getRan());
    }

    @Test
    public void testIncorrectStringArrPos() {
        boolean thrown = false;
        try {
            e.onCommand(new MockCommandSender(), new MockCommand("sample"), "sample", new String[]{"invalid2", "rest", "str", "100.1", "true"});
        } catch(UnsupportedParameterException ex) {
            thrown = true;
            System.out.println("Correctly threw: " + ex.getLocalizedMessage());

        }
        assertTrue(thrown);
        assertNull(d.getRan());
    }

    @Test
    public void testIncorrectSenderPos() {
        boolean thrown = false;
        try {
            e.onCommand(new MockCommandSender(), new MockCommand("sample"), "sample", new String[]{"invalid3", "str", "100.1", "true"});
        } catch(UnsupportedParameterException ex) {
            thrown = true;
            System.out.println("Correctly threw: " + ex.getLocalizedMessage());
        }
        assertTrue(thrown);
        assertNull(d.getRan());
    }

    @Test
    public void testLazyReqArgsCheck() {
        e.onCommand(new MockCommandSender(), new MockCommand("sample"), "sample", new String[]{"notEnoughArgs", "str"});
        assertEquals(null, d.getRan());
    }

    @Test
    public void testMissingOptionalArgs() {
        e.onCommand(new MockCommandSender(), new MockCommand("sample"), "sample", new String[]{"missingOptionalArgs", "str"});
        assertEquals("missingOptionalArgs", d.getRan());
    }
}
