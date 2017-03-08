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
package comp.parser.expr;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.parser.Espressione;
import comp.parser.template.FunzDich;
import comp.parser.template.NumDich;
import comp.parser.template.ParamDich;
import comp.parser.template.TemplateEle;

/**
 * Gestisce funzioni template all'interno del codice
 * @author loara
 */
public class TemplExpr extends Espressione{
    private final FunzDich funz;
    public TemplExpr(FunzDich fd){
        funz=fd;
    }
    public FunzDich getTemplate(){
        return funz;
    }
    @Override
    public void validate(Variabili var)throws CodeException{
        funz.validate();
    }
    @Override
    public void toCode(Segmenti seg, Variabili var, Environment env, Accumulator acc)
    throws CodeException{
        TemplateEle te=Types.getIstance().getSubstitutor().recursiveGet(funz);
        if(!(te instanceof NumDich))
            throw new CodeException("Errore interno");
        NumDich nd=(NumDich)te;
        seg.addIstruzione("mov", acc.getAccReg().getReg(1 << nd.expDim()), String.valueOf(nd.getNum()));
    }
    @Override
    public TypeElem returnType(Variabili var, boolean valid)throws CodeException{
        return Types.getIstance().find("ulong");
    }
    @Override
    public void println(int i){
        //Inutile
    }
}
