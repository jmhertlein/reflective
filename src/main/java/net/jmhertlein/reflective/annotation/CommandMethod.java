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
package net.jmhertlein.reflective.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author joshua
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandMethod {
    /**
     * The command that the user will type to run the method, minus the leading slash
     *
     * @return
     */
    String path();

    /**
     *
     * @return whether or not the console sender can run this command
     */
    boolean console() default false;

    /**
     * Whether or not player senders can run this command
     *
     * @return
     */
    boolean player() default true;

    /**
     * The permission node required to run the command
     *
     * @return
     */
    String permNode() default "";

    /**
     * Should the user not supply enough arguments, this string will be returned. If not set, one
     * will be auto-generated based on the parameters' identifiers
     *
     * @return
     */
    String helpMsg() default "";

    /**
     * Number of required arguments to this command. If not specified, will use number of non-Sender
     * and non-String[] parameters to the method
     *
     * @return
     */
    int requiredArgs() default 0;
}
