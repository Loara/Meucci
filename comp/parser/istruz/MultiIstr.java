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
import comp.code.Segmenti;
import comp.code.vars.Variabili;
import comp.parser.Istruzione;

/**
 *
 * @author loara
 */
public class MultiIstr extends Istruzione{
    public final Istruzione[] m;
    public MultiIstr(Istruzione[] i){
        m=i;
    }
    @Override
    public void validate(Variabili var, Environment env)throws CodeException{
        var.getGhostVar().addBlock();
        for(Istruzione e:m){
            if(e!=null)//alcune istruzioni vengono eliminate dai callable
                e.validate(var, env);
        }
        var.getGhostVar().removeBlock();
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc)throws CodeException{
        var.getVarStack().addBlock();
        for(Istruzione e:m){
            if(e!=null)
                e.toCode(text, var, env, acc);
        }
        var.getVarStack().removeBlock(text);
    }
}
