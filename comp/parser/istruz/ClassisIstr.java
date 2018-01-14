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
import comp.parser.Espressione;
import comp.parser.Istruzione;
import comp.parser.ParserException;
import comp.parser.expr.IdentArray;
import comp.parser.expr.Op2Expr;
import comp.scanner.SymbToken;
import comp.scanner.UgualToken;

/**
 * Istruzione che racchiude espressione, dichiarazione e definizione.
 * @author loara
 */
public class ClassisIstr extends Istruzione{
    //possono essere nulli
    //private final Dichiarazione d;
    //Ora le dichiarazioni devono stare all'inizio del blocco (MultiIstr)
    private final IdentArray e;
    private final Espressione epr;
    private final SymbToken sb;
    public ClassisIstr(UgualToken ug, IdentArray ie, Espressione ex)throws ParserException{
        e=ie;
        epr=ex;
        if(ug==null || ug.getSymb()==null)
            sb=null;
        else
            sb=new SymbToken(ug.getSymb(), 0);
    }
    @Override
    public void validate(Variabili var, Environment env)throws CodeException{
        if(e!=null){
            if(epr==null)
                throw new CodeException("Istruzione erronea");
            if(sb!=null){
                Espressione vt=new Op2Expr(e, sb, epr);
                if(!vt.returnType(var, true).ifEstende(e.returnType(var, true), true)){
                    throw new CodeException(vt.returnType(var, true).name+" non estende "+
                            e.returnType(var, true).name);
                }
                vt.validate(var);
                return;
            }
            else if(!epr.returnType(var, true).ifEstende(e.returnType(var, true), true)){
                throw new CodeException("Tipi incompatibili:\n"
                        + epr.returnType(var, true).name+" non estende "+e.returnType(var, true).name);
            }
            epr.validate(var);
            e.canWrite(var, true);
        }
        else
            epr.validate(var);
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc)throws CodeException{
        if(e!=null){
            if(epr!=null){
                if(sb!=null){
                    Op2Expr ope=new Op2Expr(e, sb, epr);//Non è necessario richiamare
                    //il substituteall
                    if(!ope.returnType(var, false).ifEstende(e.returnType(var, false), false))
                        throw new CodeException("Impossibile effettuare l'assegnazione");
                    ope.toCode(text, var, env, acc);
                    e.setVar(text, var, env, acc);
                }
                else{
                    if(!epr.returnType(var, false).ifEstende(e.returnType(var, false), false))
                        throw new CodeException("impossibile effettuare l'assegnazione\n"+
                                epr.returnType(var, false).name+" non estende "+e.returnType(var, false).name);
                    epr.toCode(text, var, env, acc);
                    e.setVar(text, var, env, acc);
                    //Utilizza ottimizzazione di CodeMap
                }
            }
            else
                throw new CodeException("Istruzione erronea");
        }
        else
            epr.toCode(text, var, env, new Accumulator());
    }
    public boolean isExpression(){
        return e==null;
    }
    public Espressione getExpr(){
        return epr;
    }
}
