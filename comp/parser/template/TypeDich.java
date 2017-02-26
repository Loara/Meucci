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

import comp.general.VScan;
import comp.parser.FineArrayException;
import comp.parser.ParserException;
import comp.parser.template.Template;
import comp.scanner.IdentToken;
import comp.scanner.Token;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Se ha 0 parametri allora deve essere convertito in ParamDich
 * @author loara
 */
public class TypeDich implements Serializable, TemplateEle{
    private String typename;
    private TemplateEle[] params;
    public TypeDich(VScan<Token> t)throws ParserException{
        if(!t.reqSpace(1))
            throw new FineArrayException();
        if(t.get() instanceof IdentToken){
            typename=((IdentToken)t.get()).getString();
            t.nextEx();
            params=Template.detectTemplate(t);
        }
        else throw new ParserException("Utilizzo non valido del tipo", t);
    }
    public TypeDich(String name, TemplateEle... te){
        typename=name;
        params=te;
    }
    public String getName(){
        return typename;
    }
    public TemplateEle[] templates(){
        return params;
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof TypeDich){
            TypeDich ed=(TypeDich)o;
            if(typename.equals(ed.typename))
                return Arrays.equals(params, ed.params);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.typename);
        hash = 41 * hash + Arrays.deepHashCode(this.params);
        return hash;
    }
}
