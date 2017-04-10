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
import comp.parser.TypeName;
import comp.scanner.RealToken;

/**
 *
 * @author loara
 */
public class RealExpr extends Espressione{
    public final long val;
    public RealExpr(RealToken t){
        val=t.numerEnc;
    }
    @Override
    public TypeElem returnType(Variabili vars, boolean v)throws CodeException{
        return Types.getIstance().find(new TypeName("real"), v);
    }
    @Override
    public void validate(Variabili vars){
        
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc)
            throws CodeException{
        text.addIstruzione("mov", acc.getAccReg().getReg(), String.valueOf(val));
        text.addIstruzione("movq", acc.getXAccReg().getReg(), acc.getAccReg().getReg());
    }
    @Override
    public void println(int i){
        
    }
}
