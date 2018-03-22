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

import comp.parser.TypeDef;

/**
 * Trasforma i TypeDef con parametri template in TypeElem finiti, in base a parametri aggiuntivi
 * 
 * Dato che i template ora vengono gestiti a livello dei moduli bisogna vedere se
 * questa classe possa servire o no
 * passati.
 * @author loara
 */
public class ClassList extends TList<TypeDef>{
    /*
    public TypeDef find(String name)throws CodeException{
        for(TypeDef te:super.val){
            if(te.getName().equals(name))
                return te;
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_typnfnd", name));
    }
    public TypeElem generate(String name, TemplateEle[] param, boolean validate)
            throws CodeException{
        TypeDef t=find(name);
        Template[] temp=t.templates();
        if(temp.length!=param.length)
            throw new CodeException(Lingue.getIstance().format("m_cod_errpara"));
        for(int i=0; i<temp.length; i++){
            if(!temp[i].isCompatible(param[i]))
                throw new CodeException(Lingue.getIstance().format("m_cod_errpara"));
        }
        WeakSubstitutor sub=new WeakSubstitutor();
        sub.addAll(t.templateNames(), param);
        TypeName exs;
        if(t.extend()==null)
            exs = null;
        else{
            exs = sub.recursiveGet(t.extend());
        }
        Membro[] mem=new Membro[t.getMembri().length];
        for(int i=0; i<mem.length; i++){
            Membro ii=t.getMembri()[i];
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
                t.modulo(), t.classExplicit());
    }
    public void esiste(String name, TemplateEle[] param)throws CodeException{
        TypeDef t=find(name);
        Template[] temp=t.templates();
        if(temp.length!=param.length)
            throw new CodeException(Lingue.getIstance().format("m_cod_errpara"));
        for(int i=0; i<temp.length; i++){
            if(!temp[i].isCompatible(param[i]))
                throw new CodeException(Lingue.getIstance().format("m_cod_errpara"));
        }
    }
    */
}
