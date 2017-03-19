/*
 * Copyright (C) 2015 loara
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
package comp.parser.istruz;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.vars.Variabili;
import comp.parser.Espressione;
import comp.parser.Istruzione;

/**
 *
 * @author loara
 */
public class ReturnIstruz extends Istruzione{
    public final Espressione e;
    public ReturnIstruz(Espressione e){
        this.e=e;
    }
    @Override
    public void validate(Variabili vars, Environment env)throws CodeException{
        if(e!=null){
            e.validate(vars);
            if(!e.returnType(vars, true).ifEstende(Environment.ret, true))
                throw new CodeException("L'espressione non estende il tipo di ritorno");
        }
        else{
            if(!Environment.ret.name.equals("void"))
                throw new CodeException("La funzione non ritorna void, ma "+Environment.ret.name);            
        }
    }
    /*
    il valore di ritorno và in rdi
    */
    
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc)throws CodeException{
        if(e!=null){
            if(!e.returnType(var, false).ifEstende(Environment.ret, false))
                throw new CodeException("L'espressione("+e.returnType(var, false).name+
                        ") non estende il tipo "+Environment.ret.name);
            e.toCode(text, var, env, acc);
            int i=acc.saveAccumulator();
            var.getVarStack().destroyAll(text);
            text.addIstruzione("mov", Register.AX.getReg(e.returnType(var, false).realDim()),
                    acc.getReg(i).getReg(e.returnType(var, false).realDim()));
            acc.restoreAccumulator(i);
        }
        else{
            if(!Environment.ret.name.equals("void"))
                throw new CodeException("La funzione non ritorna void");
            var.getVarStack().destroyAll(text);
            text.addIstruzione("xor",Register.AX.getReg(),Register.AX.getReg());
        }
            text.addIstruzione("leave",null, null);
            int p=var.getVarStack().getDimArgs();
            if(p>0)
                text.addIstruzione("ret",""+p,null);
            else
                text.addIstruzione("ret", null, null);
    }
}
