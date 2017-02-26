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
import comp.code.template.TNumbers;

/**
 *
 * @author loara
 */
public class NumTemplate extends Template{
    private long mas, min;//La possibilità che fossero TemplateEle è stata rimossa
    //in quanto generano molti problemi con le FunzEle che verranno generate in futuro.
    private final boolean bmas, bmin;
    private int dimExp;
    public NumTemplate(String i){
        super(i);
        dimExp=2;
        bmas=false;
        bmin=true;
    }
    NumTemplate(String i, int dim, long inf, long sup, boolean binf, boolean bsup){
        super(i);
        dimExp=dim;
        bmas=binf;
        bmin=bsup;
        mas=sup;
        min=inf;
    }
    @Override
    public boolean isCompatible(TemplateEle te)throws CodeException{
        if(!(te instanceof NumDich || te instanceof ParamDich || te instanceof FunzDich))
            return false;
        if(!bmas && !bmin)
            return true;
        boolean pp=true;
        if(bmas){
            if(te instanceof NumDich)
                pp= ((NumDich)te).getNum()<mas;
            else if(te instanceof ParamDich){
                pp= TNumbers.getIstance().minoreDi(((ParamDich)te).getName()
                        ,mas);
            }
            else if(te instanceof FunzDich){
                FunzDich fde=(FunzDich)te;
                fde.validate();
                if(fde.hasUp()){
                    pp=fde.upBound()<=mas;
                }
                else
                    pp=false;
            }
            else
                pp=false;//da modificare
        }
        if(!pp)
            return false;
        if(bmin){
            if(te instanceof NumDich)
                pp= ((NumDich)te).getNum()>min;
            else if(te instanceof ParamDich)
                pp= TNumbers.getIstance().maggioreDi(((ParamDich)te).getName()
                        , min);
            else if(te instanceof FunzDich){
                FunzDich fde=(FunzDich)te;
                fde.validate();
                if(fde.hasLow()){
                    pp=fde.lowBound()>=min;
                }
                else
                    pp=false;
            }
            else
                pp=false;
        }
        return pp;
    }
    public int dimExp(){
        return dimExp;
    }
    @Override
    public boolean equals(Object o){
        if(!(o instanceof NumTemplate))
            return false;
        return getIdent().equals(((NumTemplate)o).getIdent());
    }

    @Override
    public int hashCode() {
        return getIdent().hashCode();
    }
    public boolean hasMin(){
        return bmin;
    }
    public boolean hasMax(){
        return bmas;
    }
    public long getMin(){
        return min;
    }
    public long getMax(){
        return mas;
    }
}
