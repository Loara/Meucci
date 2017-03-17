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
package comp.code.template;

import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Meth;
import comp.code.TypeElem;
import comp.parser.Membro;
import comp.parser.TypeDef;
import comp.parser.TypeName;
import comp.parser.template.ParamDich;
import comp.parser.template.Template;
import comp.parser.template.TemplateEle;

/**
 * Trasforma i TypeDef con parametri template in TypeElem finiti, in base a parametri aggiuntivi
 * passati.
 * @author loara
 */
public class ClassList extends TList<TypeDef>{
    public TypeDef find(String name)throws CodeException{
        for(TypeDef te:super.val){
            if(te.getName().equals(name))
                return te;
        }
        throw new CodeException("Tipo non trovato: "+name);
    }
    public TypeElem generate(String name, TemplateEle[] param, boolean validate)
            throws CodeException{
        if(param.length==0)
            throw new CodeException("");
        TypeDef t=find(name);
        Template[] temp=t.templates();
        if(temp.length!=param.length)
            throw new CodeException("Parametri non validi");
        for(int i=0; i<temp.length; i++){
            if(!temp[i].isCompatible(param[i]))
                throw new CodeException("Parametri non compatibili");
        }
        WeakSubstitutor sub=new WeakSubstitutor();
        sub.addAll(t.templateNames(), param);
        TypeName exs;
        if(t.extend()==null)
            exs = null;
        else{
            exs = sub.recursiveGet(t.extend());
        }
        Membro[] mem=new Membro[t.getDich().length];
        for(int i=0; i<mem.length; i++){
            Membro ii=t.getDich()[i];
            mem[i]=new Membro(ii, sub.recursiveGet(ii.getType()),
                    sub.recursiveGet(new TemplateEle[]{ii.packed})[0]);
        }
        if(!t.classExplicit() && !validate && isIn(name, param)==null){
            nos.add(new Notifica(name, t.modulo(), param));
            //Non c'è bisogno di generare files per classi esplicite, in quanto
            //non hanno vtables e quindi nessun fmem
        }
        String className=Meth.className(name, param);
        return new TypeElem(className, exs, mem, 
                !t.modulo().equals(Environment.currentModulo), t.classExplicit());
    }
}
