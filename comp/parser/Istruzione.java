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
package comp.parser;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.immop.Aritm;
import comp.code.immop.Aritm2;
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.parser.expr.Op1Expr;
import comp.parser.expr.Op2Expr;
import java.io.Serializable;

/**
 *
 * @author loara
 */
public abstract class Istruzione implements Serializable{
    public abstract void toCode(Segmenti text, Variabili var, Environment env,
            Accumulator acc)throws CodeException;
    public abstract void validate(Variabili var, Environment env)throws CodeException;
    /*
    per if, for, while
    */
    /**
     * Se l'istruzione precedente è set...al, allora se la condizione per il settaggio
     * di al non è verificata salta a label, altrimenti continua normalmente
     * @param text
     * @param label
     * @param reg
     * @throws CodeException 
     */
    public static void jmpIfFalse(Segmenti text, String label, Register reg)
        throws CodeException{
        String[] prec=text.text.prevIstr();
        if(prec[0] !=null && prec[0].startsWith("set")){
            String operatorN;
            if(prec[0].substring(3, 4).equals("n")){
                operatorN=prec[0].substring(4);
            }
            else{
                operatorN="n"+prec[0].substring(3);
            }
            text.text.substitute("j"+operatorN, label, null);
        }
        else{
            text.addIstruzione("test",reg.getReg(1),reg.getReg(1));//boolean, meglio di cmp perché più corto e veloce
            text.addIstruzione("jz",label, null);
        }
    }
    /**
     * Se l'istruzione precedente è set...al, allora se la condizione per il settaggio
     * di al non è verificata salta a label, altrimenti continua normalmente
     * @param text
     * @param label
     * @param reg
     * @throws CodeException 
     */
    public static void jmpIfTrue(Segmenti text, String label, Register reg)
        throws CodeException{
        String[] prec=text.text.prevIstr();
        if(prec[0]!=null&&prec[0].startsWith("set")){
            String operator=prec[0].substring(3);
            text.text.substitute("j"+operator, label, null);
        }
        else{
            text.addIstruzione("test",reg.getReg(1),reg.getReg(1));//boolean, meglio di cmp perché più corto e veloce
            text.addIstruzione("jnz",label, null);
        }
    }
    /**
     * ottimizza le condizioni, salta alla label se è vera, altrimenti continua
     * per la sua strada
     * @param expr
     * @param text
     * @param var
     * @param env
     * @param label
     * @throws CodeException 
     */
    public static void ottimizzaIT(Espressione expr, Segmenti text, Variabili var, 
            Environment env, String label, Accumulator acc)throws CodeException{
        if(!expr.returnType(var, false).name.equals("boolean"))
            throw new CodeException("Non è di confronto");
        if(expr instanceof Op2Expr && Aritm.validate(var, (Op2Expr)expr)){
            Op2Expr ope=(Op2Expr)expr;
            Espressione[] evt=ope.getVars();
            switch (ope.getName()) {
                case "&&":
                {
                    env.increment("COND");
                    int hh=env.get("COND");
                    ottimizzaIF(evt[0], text, var, env, "COND"+hh, acc);
                    evt[1].toCode(text, var, env, acc);
                    jmpIfTrue(text, label, acc.getAccReg());
                    text.addLabel("COND"+hh);
                    return;
                }
                case "||":
                {
                    ottimizzaIT(evt[0], text, var, env, label, acc);
                    evt[1].toCode(text, var, env, acc);
                    jmpIfTrue(text, label, acc.getAccReg());
                    return;
                }
                case "->":
                {
                    ottimizzaIF(evt[0], text, var, env, label, acc);
                    evt[1].toCode(text, var, env, acc);
                    jmpIfTrue(text, label, acc.getAccReg());
                    return;
                }
                default:
                    break;
            }
        }
        else if(expr instanceof Op1Expr && Aritm2.validate(var, (Op1Expr)expr)){
            Op1Expr op1e=(Op1Expr)expr;
            if(op1e.getName().equals("!")){
                ottimizzaIF(op1e.getExpr(), text, var, env, label, acc);
                return;
            }
        }
        expr.toCode(text, var, env, acc);
        jmpIfTrue(text, label, acc.getAccReg());
    }
    public static void ottimizzaIF(Espressione expr, Segmenti text, Variabili var, 
            Environment env, String label, Accumulator acc)throws CodeException{
        if(!expr.returnType(var, false).name.equals("boolean"))
            throw new CodeException("Non è di confronto");
        if(expr instanceof Op2Expr){
            Op2Expr ope=(Op2Expr)expr;
            Espressione[] evt=ope.getVars();
            switch (ope.getName()) {
                case "&&":
                {
                    ottimizzaIF(evt[0], text, var, env, label, acc);
                    evt[1].toCode(text, var, env, acc);
                    jmpIfFalse(text, label, acc.getAccReg());
                    return;
                }
                case "||":
                {
                    env.increment("COND");
                    int hh=env.get("COND");
                    ottimizzaIT(evt[0], text, var, env, "COND"+hh, acc);
                    evt[1].toCode(text, var, env, acc);
                    jmpIfFalse(text, label, acc.getAccReg());
                    text.addLabel("COND"+hh);
                    return;
                }
                case "->":
                {
                    env.increment("COND");
                    int hh=env.get("COND");
                    ottimizzaIF(evt[0], text, var, env, "COND"+hh, acc);
                    evt[1].toCode(text, var, env, acc);
                    jmpIfFalse(text, label, acc.getAccReg());
                    text.addLabel("COND"+hh);
                    return;
                }
                default:
                    break;
            }
        }
        else if(expr instanceof Op1Expr && Aritm2.validate(var, (Op1Expr)expr)){
            Op1Expr op1e=(Op1Expr)expr;
            if(op1e.getName().equals("!")){
                ottimizzaIT(op1e.getExpr(), text, var, env, label, acc);
                return;
            }
        }
        expr.toCode(text, var, env, acc);
        jmpIfFalse(text, label, acc.getAccReg());
    }
}
