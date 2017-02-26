/*
 * Copyright (C) 2015 loara
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
package comp.parser.expr;

import comp.parser.Espressione;
import comp.scanner.IdentToken;
import java.io.Serializable;

/**
 *
 * @author loara
 */
public class IdentEle implements Serializable{
        private final String name;
        private final Espressione[] params;
        public IdentEle(String n, Espressione[] e){
            name=n;
            if(e==null)
                params=new Espressione[0];
            else
                params=e;
        }
        public IdentEle(String n){
            this(n, new Espressione[0]);
        }
        public boolean identEq(String s){
            return name.equals(s);
        }
        public String getIdent(){
            return name;
        }
        public Espressione[] getVals(){
            return params;
        }
}
