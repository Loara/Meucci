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
import comp.parser.expr.IdentExpr;

/**
 *
 * @author loara
 */
public class IfIstr extends Istruzione{
    public Istruzione ifi, ele;
    public Espressione con;
    @Override
    public void println(int ee){
        String t="";
        for(int i=0; i<ee; i++){
            t+=" ";
        }
        System.out.println(t+"If:");
        con.println(ee+Info.inde);
        System.out.println(t+"Then:");
        ifi.println(ee+Info.inde);
        if(ele!=null){
            System.out.println(t+"Else:");
            ele.println(ee+Info.inde);
        }
    }
    @Override
    public void validate(Variabili var, Environment env)throws CodeException{
            con.validate(var);
            if(!con.returnType(var, true).name.equals("boolean"))
                throw new CodeException("L'espressione non ritorna boolean");
            var.getGhostVar().addBlock();
            ifi.validate(var, env);
            var.getGhostVar().removeBlock();
            var.getGhostVar().addBlock();
            if(ele!=null)
                ele.validate(var, env);
            var.getGhostVar().removeBlock();
    }
    @Override
    public void substituteAll(Substitutor sub)throws CodeException{
        if(ele!=null)
            ele.substituteAll(sub);
        if(ifi!=null)
            ifi.substituteAll(sub);
        con.substituteAll(sub);
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env,
            Accumulator acc)throws CodeException{
        if("boolean".equals(con.returnType(var, false).name)){
            if(con instanceof IdentExpr && ((IdentExpr)con).identEq("true")){
                if(ifi!=null)
                    ifi.toCode(text, var, env, acc);
            }
            else if(con instanceof IdentExpr &&((IdentExpr)con).identEq("false")){
                if(ele!=null)
                    ele.toCode(text, var, env, acc);
            }
            else{
                env.increment("IF");
                int i=env.get("IF");
                //ottimizzazione
                Istruzione.ottimizzaIF(con, text, var, env, "IF"+i+"EL", acc);
                var.getVarStack().addBlock();
                ifi.toCode(text, var, env, acc);
                if(ele==null)
                    text.addLabel("IF"+i+"EL");
                else{
                    var.getVarStack().removeBlock(text);
                    var.getVarStack().addBlock();
                    text.addIstruzione("jmp","IF"+i+"EN",null);
                    text.addLabel("IF"+i+"EL");
                    ele.toCode(text, var, env, acc);
                    text.addLabel("IF"+i+"EN");
                }
                var.getVarStack().removeBlock(text);
            }
        }
        else throw new CodeException("Espressione irregolare: non ritorna boolean");
    }
}
