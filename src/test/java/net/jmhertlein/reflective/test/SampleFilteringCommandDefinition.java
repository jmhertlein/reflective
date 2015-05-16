/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jmhertlein.reflective.test;

import java.util.function.Predicate;
import net.jmhertlein.reflective.CommandDefinition;
import net.jmhertlein.reflective.annotation.CommandMethod;
import org.bukkit.command.CommandSender;

/**
 *
 * @author joshua
 */
public class SampleFilteringCommandDefinition implements CommandDefinition {
    private String ran;
    
    @CommandMethod(path = "use filter", filters = {"youShallNotPass"})
    public void useFilter() {
        System.out.println("useFilter was called");
        ran = "useFilter";
    }
    
    @CommandMethod(path = "no filter")
    public void dontUseFilter() {
        System.out.println("dontUseFilter was called");
        ran = "dontUseFilter";
    }

    @Override
    public Predicate<CommandSender> getFilter(String name) {
        return s -> false;
    }

    public String getRan() {
        return ran;
    }
}
