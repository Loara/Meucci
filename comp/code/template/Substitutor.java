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
import comp.code.Types;
import comp.parser.TypeName;
import comp.parser.template.FunzDich;
import comp.parser.template.NumDich;
import comp.parser.template.ParamDich;
import comp.parser.template.TemplateEle;
import comp.parser.template.TypeDich;
import java.util.HashMap;

/**
 * Quando si vuole creare una particolare istanza di un tipo/funzione template,
 * questa classe si preoccupa di sostituire, ovunque nel codice, i parametri template con i loro
 * equivalenti
 * 
 * Và utilizzato solo nel toCode in presenza di template, se ne sconsiglia (attualmente)
 * l'utilizzo in validate. Types NON effettua in automatico la conversione.
 * @author loara
 */
public class Substitutor extends HashMap<String, TemplateEle>{
    public void addAll(String[] k, TemplateEle[] v){
        if(k.length!=v.length)
            return;
        for(int i=0; i<k.length; i++){
            if(v[i] instanceof ParamDich){
                if(k[i].equals(((ParamDich)v[i]).getName()))
                    continue;//verrebbero a coincidere, quindi non deve essere effettuata
                //alcuna sostituzione
            }
            putIfAbsent(k[i], v[i]);
        }
    }
    /**
     * Come get, ma effettua il controllo più volte.
     * La validazione è già stata effettuata, questo serve per verificare il codice.
     * @param s
     * @return 
     * @throws comp.code.CodeException 
     */
    public TemplateEle recursiveGet(ParamDich s)throws CodeException{
        TemplateEle te=super.get(s.getName());
        if(te==null)
            return null;
        if(te instanceof NumDich){
            return te;
        }
        if(te instanceof ParamDich){
            TemplateEle tu=recursiveGet((ParamDich)te);
            if(tu==null)
                return te;
            else
                return tu;
        }
        if(te instanceof TypeDich){
            TypeDich tte=recursiveGet((TypeDich)te);
            if(tte==null)
                return te;
            else
                return tte;
        }
        if(te instanceof FunzDich){
            NumDich nd=recursiveGet((FunzDich)te);
            return nd;
        }
        return null;//Per sicurezza, anche se non accade mai
    }
    /**
     * Effettua la ricerca a livello dei parametri di s
     * Non ritorna mai null
     * @param s
     * @return 
     * @throws comp.code.CodeException 
     */
    public TypeDich recursiveGet(TypeDich s)throws CodeException{
        TemplateEle[] vals=s.templates();
        TemplateEle[] ret=recursiveGet(vals);
        return new TypeDich(s.getName(), ret);
    }
    /**
     * Ricerca limitatamente ai tipi.
     * @param s
     * @return 
     * @throws comp.code.CodeException 
     */
    public TypeName recursiveGet(TypeName s)throws CodeException{
        if(s==null)
            return null;
        if(s.templates().length==0){
            TemplateEle te=recursiveGet(new ParamDich(s.getName()));
            if(te==null)
                return s;
            return new TypeName(te);
        }
        else{
            TypeDich td=recursiveGet(new TypeDich(s.getName(), s.templates()));
            if(td==null)
                return s;
            return new TypeName(td);
        }
    }
    public TemplateEle[] recursiveGet(TemplateEle[] vals)throws CodeException{
        TemplateEle[] ret=new TemplateEle[vals.length];
        for(int i=0; i<vals.length; i++){
            if(vals[i]==null){
                ret[i]=null;
                continue;
            }
            if(vals[i] instanceof NumDich)
                ret[i]=vals[i];
            else if(vals[i] instanceof ParamDich){
                TemplateEle pp=recursiveGet((ParamDich)vals[i]);
                if(pp==null)
                    ret[i]=vals[i];
                else
                    ret[i]=pp;
            }
            else if(vals[i] instanceof TypeDich){
                ret[i]=recursiveGet((TypeDich)vals[i]);
            }
            else{
                //FunzDich
                ret[i]=recursiveGet((FunzDich)vals[i]);
            }
        }
        return ret;
    }
    public NumDich recursiveGet(FunzDich fd)throws CodeException{
        fd.setParams(recursiveGet(fd.getParams()));
        long ret;
        TypeName val;
        TemplateEle te;
        TemplateEle[] par=recursiveGet(fd.getParams());
        int dim=fd.dimension();
        if(fd instanceof FunzDich.SIZEOF){
            te=fd.getParams()[0];
            if(te instanceof ParamDich)
                val=new TypeName(((ParamDich)te).getName());
            else if(te instanceof TypeDich)
                val=new TypeName((TypeDich)te);
            else throw new CodeException("Valore errato in funzione template");
            ret=Types.getIstance().find(val, false).realDim();
        }
        else if(fd instanceof FunzDich.SUM){
            ret=0;
            for(TemplateEle t:fd.getParams()){
                if(t instanceof NumDich){
                    ret+=((NumDich)t).getNum();
                }
                else throw new CodeException("Valore invalido");
            }
        }
        else if(fd instanceof FunzDich.PROD){
            ret=1;
            for(TemplateEle t:fd.getParams()){
                if(t instanceof NumDich){
                    ret*=((NumDich)t).getNum();
                }
                else throw new CodeException("Valore invalido");
            }
        }
        else if(fd instanceof FunzDich.DIMENSION){
            te=fd.getParams()[0];
            if(te instanceof ParamDich)
                val=new TypeName(((ParamDich)te).getName());
            else if(te instanceof TypeDich)
                val=new TypeName((TypeDich)te);
            else throw new CodeException("Valore errato in funzione template");
            ret=Types.getIstance().find(val, false).dimension(false);
        }
        else
            throw new CodeException("Bug interno");
        return new NumDich(ret, dim);
    }
}
