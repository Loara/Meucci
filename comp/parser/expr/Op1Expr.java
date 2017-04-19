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
package comp.parser.expr;

import comp.code.*;
import comp.code.Funz.FElement;
import comp.code.vars.Variabili;
import comp.code.immop.Aritm2;
import comp.general.Info;
import comp.parser.Espressione;
import comp.parser.istruz.TryIstr;
import comp.scanner.SymbToken;

/**
 *
 * @author loara
 */
public class Op1Expr extends Espressione{
    protected SymbToken symb;
    protected Espressione epr;
    public Op1Expr(SymbToken t, Espressione e){
        symb=t;
        epr=e;
    }
    @Override
    public void println(int inter){
        String i="";
        for(int e=0; e<inter; e++){
            i+=" ";
        }
        System.out.println(i+symb.toString());
        System.out.println(i+"Elemento:");
        epr.println(inter+2);
    }
    private Funz.FElement request(Variabili var, boolean v)throws CodeException{
            TypeElem[] tr=new TypeElem[1];
            tr[0]=epr.returnType(var, v);
            return Funz.getIstance().request(symb.getString(), tr, v);
    }
    private FElement modname(Variabili var)throws CodeException{
            Funz.FElement fe=request(var, false);
            if(fe.isExternFile())
                Funz.getIstance().ext.add(fe.modname);
            return fe;
    }
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        TypeElem reimm=Aritm2.returnType(this, var, v);
        if(reimm!=null)
            return reimm;
        return request(var, v).Return(v);
    }
    public String getName(){
        return symb.getString();
    }
    public Espressione getExpr(){
        return epr;
    }
    @Override
    public void validate(Variabili var)throws CodeException{
        epr.validate(var);
        if(!Aritm2.validate(var, this)){
            FElement fe=Funz.getIstance().request(symb.getString(), 
                    new TypeElem[]{epr.returnType(var, true)}, true);
        if(!Info.containedIn(fe.errors, Environment.errors))
            throw new CodeException("Errori di "+fe.name+" non gestiti correttamente");
        }
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc)throws CodeException{
        if(Aritm2.analyze(text, var, env, acc, this))
            return;
        FElement modname=modname(var);
        if(!TryIstr.checkThrows(modname.errors, env))
            throw new CodeException("Errori di "+modname.name+" non gestiti correttamente");
        var.getVarStack().pushAll(text);
        FunzExpr.perfCall(modname, new Espressione[]{epr}, text, var, env, acc);
        //popAll automatico
    }
}
