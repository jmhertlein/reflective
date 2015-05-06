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
package net.jmhertlein.reflective.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 * @author joshua
 */
public class DotWriter extends PrintWriter {
    private final boolean directed;

    public DotWriter(String filename, boolean directed) throws FileNotFoundException {
        super(filename);
        println(directed ? "digraph {" : "graph {");
        this.directed = directed;
    }

    public DotWriter(File file, boolean directed) throws FileNotFoundException {
        super(file);
        println(directed ? "digraph {" : "graph {");
        this.directed = directed;
    }

    public void printEdge(String a, String b) {
        if(directed) {
            printf("\"%s\" -> \"%s\"\n", a, b);
        } else {
            printf("\"%s\" -- \"%s\"\n", a, b);
        }
    }

    public void printLabel(String node, String label) {
        printf("\"%s\" [label=\"%s\"]\n", node, label);
    }

    public boolean isDirected() {
        return directed;
    }

    @Override
    public void close() {
        println("}");
        super.close();
    }

}
