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
public class ForIstr extends Istruzione{
    public ClassisIstr frist, after;
    public Istruzione doi;
    public Espressione wh;
    @Override
    public void println(int ee){
        String i="";
        for(int h=0; h<ee; h++)
            i+=" ";
        System.out.println(i+"For:");
        System.out.println(i+"from:");
        frist.println(ee+Info.inde);
        System.out.println(i+"whil:");
        wh.println(ee+Info.inde);
        System.out.println(i+"step:");
        after.println(ee+Info.inde);
        System.out.println(i+"do:");
        doi.println(ee+Info.inde);
    }
    @Override
    public void validate(Variabili var, Environment env)throws CodeException{
            env.increment("FOR");
            int i=env.get("FOR");
            var.getGhostVar().addBlock();
            frist.validate(var, env);
            wh.validate(var);
            if(!wh.returnType(var, true).name.equals("boolean"))
                throw new CodeException("L'espressione non ritorna boolean");
            env.push("FOR"+i);//siamo in questo while
            doi.validate(var, env);
            env.pop();
            after.validate(var, env);
            var.getGhostVar().removeBlock();
    }
    @Override
    public void substituteAll(Substitutor sub)throws CodeException{
        if(after!=null)
            after.substituteAll(sub);
        if(frist!=null)
            frist.substituteAll(sub);
        if(doi!=null)
            doi.substituteAll(sub);
        if(wh!=null)
            wh.substituteAll(sub);
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env,
            Accumulator acc)throws CodeException{
            env.increment("FOR");
            int i=env.get("FOR");
            frist.toCode(text, var, env, acc);
            text.addLabel("FR"+i+"IN0");
            Istruzione.ottimizzaIF(wh, text, var, env, "FR"+i+"EN", acc);
            var.getVarStack().addBlock();
            env.push("FR"+i);//siamo in questo for
            doi.toCode(text, var, env, acc);
            env.pop();
            text.addLabel("FR"+i+"IN");//per i continue che devono saltare qua
            var.getVarStack().removeBlock(text);
            after.toCode(text, var, env, acc);
            text.addIstruzione("jmp","FR"+i+"IN0", null);
            text.addLabel("FR"+i+"EN");
    }
}
