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
    /*
    private long mas, min;
    private final boolean bmas, bmin;
    
    Data la complessità della loro gestione, è stata disabilitata la possibilità
    di limitarne i valori. Si consiglia per quanto possibile di utilizzare throw
    */
    private final int dimExp;
    public NumTemplate(String i){
        super(i);
        dimExp=2;
    }
    NumTemplate(String i, int dim){
        super(i);
        dimExp=dim;
    }
    @Override
    public boolean isCompatible(TemplateEle te)throws CodeException{
        return te instanceof NumDich || te instanceof ParamDich || te instanceof FunzDich;
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
}
