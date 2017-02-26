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
package comp.parser;

import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.parser.template.*;
import comp.general.VScan;
import comp.parser.template.Template;
import comp.scanner.IdentToken;
import comp.scanner.Token;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Il nome dei tipi che compaiono nelle dichiarazioni et similia.
 * 
 * Quando si utilizza un tipo (e non quando si crea con "type") và utilizzato questo
 * 
 * Non serve clonarlo, in quanto è read-only
 * 
 * @author loara
 */
public class TypeName implements Serializable{
    private String typename;
    private TemplateEle[] params;
    public TypeName(VScan<Token> t)throws ParserException{
        if(!t.reqSpace(1))
            throw new FineArrayException();
        if(t.get() instanceof IdentToken){
            typename=((IdentToken)t.get()).getString();
            t.nextEx();
            params=Template.detectTemplate(t);
        }
        else throw new ParserException("Utilizzo non valido del tipo", t);
    }
    public TypeName(String name, TemplateEle... te){
        typename=name;
        params=te;
    }
    public TypeName(TemplateEle typ)throws CodeException{
        if(typ instanceof ParamDich){
            typename=((ParamDich)typ).getName();
            params=new TemplateEle[0];
        }
        else if (typ instanceof TypeDich){
            TypeDich i=(TypeDich)typ;
            typename=i.getName();
            params=i.templates();
        }
        else throw new CodeException("Non è un tipo");
    }
    public String getName(){
        return typename;
    }
    public TemplateEle[] templates(){
        return params;
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof TypeName){
            TypeName i=(TypeName)o;
            if(!typename.equals(i.typename))
                return false;
            if(params.length!=i.params.length)
                return false;
            for(int ii=0; ii<params.length; ii++){
                if(!params[ii].equals(i.params[ii]))
                    return false;
            }
        return true;
        }
    return false;
    }
    //la validazione viene effettuata tramite una ricerca su Types

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.typename);
        hash = 37 * hash + Arrays.deepHashCode(this.params);
        return hash;
    }
}
