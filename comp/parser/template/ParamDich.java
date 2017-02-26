/*
 * Copyright (C) 2016 loara
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
package comp.parser.template;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author loara
 */
public class ParamDich implements Serializable, TemplateEle{
    private String name;
    public ParamDich(String n){
        name=n;
    }
    public String getName(){
        return name;
    }
    public TypeDich toTypeDich(){
        return new TypeDich(name);
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof ParamDich){
            return name.equals(((ParamDich)o).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
