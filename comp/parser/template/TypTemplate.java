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

import comp.code.CodeException;
import comp.code.TypeElem;
import comp.code.Types;
import comp.parser.TypeName;

/**
 *
 * @author loara
 */
public class TypTemplate extends Template{
    private final boolean reference, number;
    private final TypeName ext;
    public TypTemplate(String i){
        super(i);
        reference=false;
        number=false;
        ext=null;
    }
    public TypTemplate(String i, boolean ref, boolean num, TypeName ex){
        super(i);
        reference=ref;
        number=num;
        ext=ex;
    }
    @Override
    public boolean isCompatible(TemplateEle tp)throws CodeException{
        TypeElem te=Types.getIstance().find(new TypeName(tp), true);
        boolean pp=true;
        if(reference)
            pp=te.isReference();
        if(number)
            pp=pp&&te.isNum();
        if(!pp)
            return false;
        if(reference && ext!=null)
            return te.ifEstende(ext, true);//Và migliorata
        return true;
    }
    public boolean isRef(){
        return reference;
    }
    public boolean isNum(){
        return number;
    }
    public TypeName ext(){
        return ext;
    }
    public void validate()throws CodeException{
        if(reference && number)
            throw new CodeException("Errore template: "+getIdent()+" non può essere "
                    + "reference e number");
    }
}
