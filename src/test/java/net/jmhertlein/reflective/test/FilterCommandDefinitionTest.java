/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.reflective.test;

import net.jmhertlein.reflective.TreeCommandExecutor;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author joshua
 */
public class FilterCommandDefinitionTest {
    private TreeCommandExecutor e;
    private SampleFilteringCommandDefinition d;

    @Before
    public void setUp() {
        e = new TreeCommandExecutor();
        d = new SampleFilteringCommandDefinition();
        e.add(d);
    }

    @After
    public void tearDown() {
        e = null;
        d = null;
    }
    
    @Test
    public void testUseFilter() {
        e.onCommand(new MockCommandSender(), new MockCommand("use"), "use", new String[]{"filter"});
        assertEquals(d.getRan(), null);
    }
    
    @Test
    public void testDontUseFilter() {
        e.onCommand(new MockCommandSender(), new MockCommand("no"), "no", new String[]{"filter"});
        assertEquals(d.getRan(), "dontUseFilter");
    }
}
