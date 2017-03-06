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
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.parser.Espressione;
import comp.parser.Istruzione;
import comp.general.Info;

/**
 *
 * @author loara
 */
public class WhileIstr extends Istruzione{
    public Espressione e;
    public Istruzione d;
    @Override
    public void println(int ee){
        String i="";
        for(int j=0; j<ee; j++){
            i+=" ";
        }
        System.out.println(i+"While:");
        e.println(ee+Info.inde);
        System.out.println(i+"Then:");
        d.println(ee+Info.inde);
    }
    @Override
    public void validate(Variabili var, Environment env)throws CodeException{
            env.increment("WHILE");
            int i=env.get("WHILE");
            var.getGhostVar().addBlock();
            e.validate(var);
            if(!e.returnType(var, true).name.equals("boolean"))
                throw new CodeException("L'espressione non ritorna boolean");
            env.push("WH"+i);//siamo in questo while
            d.validate(var, env);
            env.pop();
            var.getGhostVar().removeBlock();
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc)throws CodeException{
            env.increment("WHILE");
            int i=env.get("WHILE");
            text.addLabel("WH"+i+"IN");
            var.getVarStack().addBlock();
            Istruzione.ottimizzaIF(e, text, var, env, "WH"+i+"EN", acc);
            env.push("WH"+i);//siamo in questo while
            d.toCode(text, var, env, acc);
            env.pop();
            var.getVarStack().removeBlock(text);
            text.addIstruzione("jmp","WH"+i+"IN",null);
            text.addLabel("WH"+i+"EN");
    }
}
