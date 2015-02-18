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
package net.jmhertlein.reflective.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.bukkit.command.CommandSender;

/**
 *
 * @author joshua
 */
@SupportedAnnotationTypes("net.jmhertlein.core.ebcf.annotation.CommandMethod")
public class CommandMethodProcessor extends AbstractProcessor {
    private static final String ERR_MSG_PARAMS = "CommandMethod-annotated method params must be either (), (CommandSender), (String[]), or (CommandSender,String[])";
    private static final String ERR_MSG_VISIBILITY = "CommandMethod-annotated methods must be public.";
    
    private static final Set<List<String>> validParamsLists;
    
    static {
        validParamsLists = new HashSet<>();
        validParamsLists.add(new ArrayList<>(0));
        validParamsLists.add(new ArrayList<String>(1) {{ add(CommandSender.class.getCanonicalName()); }});
        validParamsLists.add(new ArrayList<String>(1) {{ add(String[].class.getCanonicalName()); }});
        validParamsLists.add(new ArrayList<String>(2) {{ 
            add(CommandSender.class.getCanonicalName());
            add(String[].class.getCanonicalName()); 
        }});
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
        set.stream().forEach(annotation -> {
            re.getElementsAnnotatedWith(annotation)
                    .stream()
                    .filter(e -> e instanceof ExecutableElement)
                    .map(e -> (ExecutableElement) e)
                    .forEach(executable -> {
                        if(!executable.getModifiers().contains(Modifier.PUBLIC)) {
                            processingEnv.getMessager().printMessage(Kind.ERROR, ERR_MSG_VISIBILITY, executable);
                        }
                        
                        List<String> params = executable.getParameters().stream().map(p -> p.asType().toString()).collect(Collectors.toList());
                        if(!validParamsLists.stream().anyMatch(goodList -> goodList.equals(params))) {
                            processingEnv.getMessager().printMessage(Kind.ERROR, ERR_MSG_PARAMS, executable);
                        }
                    });
        });
        
        return true;
    }
}
