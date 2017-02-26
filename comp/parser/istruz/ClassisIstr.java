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
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.parser.Dichiarazione;
import comp.parser.Espressione;
import comp.parser.Istruzione;
import comp.general.Info;
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
    private final Dichiarazione d;
    private final IdentArray e;
    private final Espressione epr;
    private final SymbToken sb;
    public ClassisIstr(Dichiarazione di, UgualToken ug, IdentArray ie, Espressione ex)throws ParserException{
        d=di;
        e=ie;
        epr=ex;
        if(ug==null || ug.getSymb()==null)
            sb=null;
        else
            sb=new SymbToken(ug.getSymb(), 0);
        if(di!=null){
            Info.isForbitten(di.getIdent(), di.getTokIdent().getRiga());
        }
    }
    public TypeElem effectiveType(Variabili var, boolean v)throws CodeException{
        if(d!=null){
            if(!d.getType().equals("auto"))
                return Types.getIstance().find(d.getRType(), v);
            else{
                if(epr==null)
                    throw new CodeException("Modificatore auto utilizzato impropriamente");
                return epr.returnType(var, v);
            }
        }
        else return null;
    }
    @Override
    public void println(int ee){
        String i="";
        for(int j=0; j<ee; j++){
            i+=" ";
        }
        if(d!=null){
            System.out.println(i+"Dichiarazione: "+d.toString());
            if(epr!=null)
                epr.println(ee+Info.inde);
        }
        else if(e!=null){
            System.out.println(i+"Assegnazione: ");
            if(epr!=null)
                epr.println(ee+Info.inde);
        }
        else{
            System.out.println(i+"Espressione:");
            epr.println(ee+Info.inde);
        }
    }
    @Override
    public void substituteAll(Substitutor sub)throws CodeException{
        if(d!=null){
            d.substitute(sub);
            if(epr!=null)
                epr.substituteAll(sub);
        }
        else if(e!=null){
            e.substituteAll(sub);
            epr.substituteAll(sub);
        }
        else
            epr.substituteAll(sub);
    }
    @Override
    public void validate(Variabili var, Environment env)throws CodeException{
        if(d!=null){
            TypeElem dt=Types.getIstance().find(d.getRType(), true);//per vedere se esiste
            var.addGhostVar(new Dichiarazione(effectiveType(var, true).name, d.getIdent()));
            if(epr!=null){
                if(sb!=null){
                    Espressione vt=new Op2Expr(d.getTokIdent(), sb, epr);
                    if(!vt.returnType(var, true).ifEstende(dt, true))
                        throw new CodeException(vt.returnType(var, true)+" non estende "+d.getType());
                    vt.validate(var);
                    return;
                }
                else if(!epr.returnType(var, true).ifEstende(dt, true))
                    throw new CodeException(epr.returnType(var, true).name+" non estende "+d.getType());
                epr.validate(var);
            }
        }
        else if(e!=null){
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
        if(d!=null){
            if(epr!=null){
                if(sb!=null)
                    throw new CodeException("Variabile non inizializzata");
                else{
                    if(!epr.returnType(var, false).ifEstende(d.getRType(), false))
                        throw new CodeException("impossibile effettuare l'assegnazione");
                    epr.toCode(text, var, env, acc);
                    var.addVarStack(d);
                    var.setVar(text, acc, d.getIdent());//fa in automatico
                    //anche il caso xmm. Utilizza ottimizzazione codeMap
                }
            }
            else  
                var.addVarStack(d);
        }
        else if(e!=null){
            if(epr!=null){
                if(sb!=null){
                    Op2Expr ope=new Op2Expr(e, sb, epr);//Non è necessario richiamare
                    //il substituteall
                    if(!ope.returnType(var, false).ifEstende(e.returnType(var, false), false))
                        throw new CodeException("Impossibile effettuare l'assegnazione");
                    ope.toCode(text, var, env, acc);
                    var.setVar(e, text, env, acc);
                }
                else{
                    if(!epr.returnType(var, false).ifEstende(e.returnType(var, false), false))
                        throw new CodeException("impossibile effettuare l'assegnazione\n"+
                                epr.returnType(var, false).name+" non estende "+e.returnType(var, false).name);
                    epr.toCode(text, var, env, acc);
                    var.setVar(e, text, env, acc);
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
        return d==null && e==null;
    }
    public Espressione getExpr(){
        return epr;
    }
}
