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
import comp.code.Funz.FElement;
import comp.code.Meth;
import comp.code.TypeElem;
import comp.general.Stack;
import comp.parser.Callable;
import comp.parser.TypeName;
import comp.parser.template.TemplateEle;

/**
 *
 * @author loara
 */
public class FunzList extends TList<Callable>{
    public FunzList(){
        super();
    }
    private Callable[] find0(String name)throws CodeException{
        Stack<Callable> f=new Stack<>(Callable.class);
        val.stream().filter((te) -> (te.getName().equals(name))).forEach((te) -> {
            f.push(te);
        });
        return f.toArray();
    }
    public Callable find(String name)throws CodeException{
        Callable[] ret=find0(name);
        if(ret.length==1)
            return ret[0];
        else
            throw new CodeException("Trovate "+ret.length+" funzioni per "+name);
    }
    /*
    noAdd da impostare a true se è utilizzato durante il validate, in modo da
    non generare un file inutile
    */
    /*
    Bisogna ASSOLUTAMENTE cambiare l'algoritmo di sostituzione
    genera troppi errori utilizzare un substituteAll per sostituire ogni ricorrenza
    in quanto, una volta effettuata la sostituzione, se si vuole generare una
    nuova sostituzione dell'oggetto o si deve rileggerlo dal file oppure
    bisognerebbe ogni volta clonarlo. La soluzione migliore resta quella di utilizzare
    una mediazione nella ricerca che sostituisce automaticamente durante la ricerca
    */
            
    public FElement generate(String name, TemplateEle[] param, TypeElem[] fpdec, boolean noAdd)
            throws CodeException{
        if(param.length==0)
            throw new CodeException("");
        Callable[] t=find0(name);
        Stack<Callable> sf=new Stack<>(Callable.class);
        boolean just;
        TypeName[] pars=null;
        TypeName retT=null;
        for(Callable f:t){
            if(!f.getName().equals(name))
                continue;
            if(f.templates().length!=param.length)
                continue;
            if(f.getElems().length!=fpdec.length)
                continue;
            just=true;
            for(int i=0; i<param.length; i++){
                if(!f.templates()[i].isCompatible(param[i])){
                    just=false;
                    break;
                }
            }
            if(!just)
                continue;
            WeakSubstitutor sub=new WeakSubstitutor();//substitutor temporaneo
            sub.addAll(f.templateNames(), param);
            pars=new TypeName[f.getElems().length];
            for(int i=0; i<pars.length; i++){
                pars[i]=sub.recursiveGet(f.getElems()[i].dich.getRType());
            }
            for(int i=0; i<fpdec.length; i++){
                if(!fpdec[i].ifEstende(pars[i], noAdd)){
                    just=false;
                    break;
                }
            }
            if(!just)
                continue;
            sf.push(f);
            retT=sub.recursiveGet(f.getReturn());
        }
        Callable[] filt=sf.toArray();
        if(filt.length!=1){
            String error="Trovate "+filt.length+" funzioni distinte per "+Meth.funzKey(name, param)+":";
            for(Callable c:filt){
                error+="\n"+Meth.modName(c, param);
            }
            throw new CodeException(error);
        }
        FElement fe=new FElement(Meth.funzKey(filt[0].getName(), param), Meth.modName(filt[0], param), 
                pars, retT, false, !filt[0].getModulo().equals(Environment.currentModulo), true, 
                filt[0].errors());
        //meglio questo che utilizzare direttamente il costruttore con Callable:
        //poichè sui Callable non deve essere effettuato la sostituzione
        if(!noAdd && isIn(fe.modname, param)==null){
            nos.add(new Notifica(name, filt[0].getModulo(), param));
        }
        return fe;
    }
}
