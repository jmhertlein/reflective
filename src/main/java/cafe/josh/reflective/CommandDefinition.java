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
package cafe.josh.reflective;

import java.util.function.Predicate;
import org.bukkit.command.CommandSender;

/**
 * Marker interface for command definitions.
 *
 * Place methods annotated with @CommandMethod in implementations of this class.
 *
 * @author joshua
 * @see net.jmhertlein.core.reflective.annotation.CommandMethod
 */
public interface CommandDefinition {
    public default Predicate<CommandSender> getFilter(String filterName) {
        return s -> true;
    }
}
