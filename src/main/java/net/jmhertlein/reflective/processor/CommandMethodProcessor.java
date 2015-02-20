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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author joshua
 */
@SupportedAnnotationTypes("net.jmhertlein.reflective.annotation.CommandMethod")
public class CommandMethodProcessor extends AbstractProcessor {
    private static final String ERR_MSG_PARAMS = "CommandMethod-annotated method params must be either (), (CommandSender), (String[]), or (CommandSender,String[])";
    private static final String ERR_MSG_VISIBILITY = "CommandMethod-annotated methods must be public.";

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

                        //List<String> params = executable.getParameters().stream().map(p -> p.asType().toString()).collect(Collectors.toList());
                        checkWellFormedParamList(executable);
                    });
        });

        return true;
    }

    private void checkWellFormedParamList(ExecutableElement e) {
        List<? extends VariableElement> params = e.getParameters();

        int i = 0;
        for(VariableElement v : params) {
            if(isType(v, "org.bukkit.command.CommandSender") || isType(v, "org.bukkit.entity.Player") || isType(v, "org.bukkit.command.ConsoleCommandSender")) {
                if(i != 0) {
                    compileError(v, "CommandSender variable must be first in parameter list.");
                }
            } else if(isType(v, getArrayMirrorForName("java.lang.String"))) {
                if(i != (params.size() - 1)) {
                    compileError(v, "String[] must be the last parameter.");
                }
            } else {
                if(!isAllowedPrimitiveKind(v.asType().getKind())) {
                    compileError(v, "Cannot automatically convert to non-primitive type \"" + v.asType().toString() + "\"");
                }
            }

            i++;
        }
    }

    /**
     * takes the FQN of a type and returns a TypeMirror representing it based on how a name was
     * mapped to a TypeMirror in the answer to this post:
     * https://stackoverflow.com/questions/20358039/java-annotations-processor-check-if-typemirror-implements-specific-interface
     *
     * @param s
     * @return
     */
    private TypeMirror getMirrorForName(String s) {
        return processingEnv.getElementUtils().getTypeElement(s).asType();
    }

    private TypeMirror getArrayMirrorForName(String s) {
        return processingEnv.getTypeUtils().getArrayType(getMirrorForName(s));
    }

    /**
     *
     * @param v variable's type to check
     * @param fqn fully qualified type name
     * @return whether the variable's type is the name as the FQN
     */
    private boolean isType(VariableElement v, String fqn) {
        return processingEnv.getTypeUtils().isSameType(v.asType(), getMirrorForName(fqn));
    }

    private boolean isType(VariableElement v, TypeMirror type) {
        return processingEnv.getTypeUtils().isSameType(v.asType(), type);
    }

    private void compileError(Element e, String msg) {
        processingEnv.getMessager().printMessage(Kind.ERROR, msg, e);
    }

    private static boolean isAllowedPrimitiveKind(TypeKind k) {
        switch(k) {
            case LONG:
            case BOOLEAN:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case BYTE:
            case SHORT:
                return true;
            default:
                return false;
        }
    }
}
