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
    public int dimension(String tname)throws CodeException{
        NumTemplate t=find(tname);
        if(t==null)
            throw new CodeException(Lingue.getIstance().format("m_cod_pnftemp"));
        return 1 << t.dimExp();
    }
    public int expDim(String tname)throws CodeException{
        if(suds!=null){
            if(suds.containsKey(tname)){
                TemplateEle ret=suds.recursiveGet(new ParamDich(tname));
                if(ret instanceof NumDich){
                    return ((NumDich)ret).expDim();
                }
                else throw new CodeException(Lingue.getIstance().format("m_cod_pnftemp"));
            }
        }
        NumTemplate t=find(tname);
        if(t==null)
            throw new CodeException(Lingue.getIstance().format("m_cod_pnftemp"));
        return t.dimExp();
    }
    public void clearAll(){
        hs.clear();
        suds=null;
    }
}
