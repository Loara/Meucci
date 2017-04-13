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
import comp.general.Lingue;
import comp.parser.template.NumDich;
import comp.parser.template.NumTemplate;
import comp.parser.template.ParamDich;
import comp.parser.template.TemplateEle;
import java.util.HashSet;

/**
 * Gestisce i template per i numeri. E' l'equivalente di Types
 * @author loara
 */
public class TNumbers {
    private static TNumbers tnumb;
    private Substitutor suds;
    static{
        tnumb=null;
    }
    public static TNumbers getIstance(){
        if(tnumb==null)
            tnumb=new TNumbers();
        return tnumb;
    }
    private final HashSet<NumTemplate> hs;
    public TNumbers(){
        hs=new HashSet<>();
        suds=null;
    }
    public void setSubstuitutor(Substitutor sub){
        suds=sub;
    }
    public boolean add(NumTemplate tn){
        return hs.add(tn);
    }
    public boolean remove(NumTemplate tn){
        return hs.remove(tn);
    }
    public boolean isIn(String t){
        if(suds !=null && suds.containsKey(t))
            return true;
        NumTemplate res=new NumTemplate(t);
        return hs.contains(res);
    }
    public NumTemplate find(String name){
        for(NumTemplate t:hs){
            if(t.getIdent().equals(name))
                return t;
        }
        return null;
    }
    public NumDich obtain(ParamDich pd)throws CodeException{
        TemplateEle te=suds.recursiveGet(pd);
        if(te instanceof NumDich){
            return (NumDich)te;
        }
        else throw new CodeException(Lingue.getIstance().format("m_cod_temnufn"));
    }
    /**
     * Dice se tname è maggiore di val
     * @param tname
     * @param numb
     * @return 
     * @throws comp.code.CodeException 
     */
    public boolean maggioreDi(String tname, long numb)throws CodeException{
        NumTemplate t=find(tname);
        if(t==null)
            throw new CodeException("Impossibile trovare il parametro "+tname);
        if(t.hasMin()){
            return t.getMin()>=numb;
        }
        else return false;
    }
    /**
     * Dice se tname è minore di val
     * @param tname
     * @param numb
     * @return 
     * @throws comp.code.CodeException 
     */
    public boolean minoreDi(String tname, long numb)throws CodeException{
        NumTemplate t=find(tname);
        if(t==null)
            throw new CodeException("Impossibile trovare il parametro "+tname);
        if(t.hasMax()){
            return t.getMax()<=numb;
        }
        else return false;
    }
    public int dimension(String tname)throws CodeException{
        NumTemplate t=find(tname);
        if(t==null)
            throw new CodeException("Impossibile trovare il parametro "+tname);
        return 1 << t.dimExp();
    }
    public int expDim(String tname)throws CodeException{
        if(suds!=null){
            if(suds.containsKey(tname)){
                TemplateEle ret=suds.recursiveGet(new ParamDich(tname));
                if(ret instanceof NumDich){
                    return ((NumDich)ret).expDim();
                }
                else throw new CodeException("Parametro erroneo");
            }
        }
        NumTemplate t=find(tname);
        if(t==null)
            throw new CodeException("Impossibile trovare il parametro "+tname);
        return t.dimExp();
    }
    /*
    Non più utilizzato
    public boolean maggioreDi(ParamDich tname, ParamDich tnum){
        NumTemplate t=find(tname);
        NumTemplate tn=find(tnum);
        if(t==null || tn==null)
            return false;
        if(t.getMin()!=null){
            if(t.getMin() instanceof NumDich){
                long val=((NumDich)t.getMin()).getNum();
                return minoreDi(tnum.getName(), val);
            }
            if(maggioreDi((ParamDich)t.getMin(), tnum))
                return true;
        }
        if(tn.getMax()!=null){
            if(tn.getMax() instanceof NumDich){
                long val=((NumDich)t.getMax()).getNum();
                return maggioreDi(tname.getName(), val);//ci sono casi in cui questo controllo non è
                //superfluo
            }
            if(maggioreDi(tname, (ParamDich)tn.getMax()))
                return true;
        }
        return false;
    }
    public boolean minoreDi(ParamDich tname, ParamDich tnum){
        return maggioreDi(tnum, tname);
    }
    */
    public void clearAll(){
        hs.clear();
        suds=null;
    }
}
