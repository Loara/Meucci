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

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Funz;
import comp.code.Segmenti;
import comp.code.vars.Variabili;
import comp.code.immop.Aritm;
import comp.parser.Espressione;
import comp.scanner.IdentToken;
import comp.scanner.SymbToken;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.Substitutor;
import comp.parser.TypeName;

/**
 *
 * @author loara
 */
public class Op2Expr extends Espressione{
    protected SymbToken symb;
    protected Espressione epr, epr2;
    public Op2Expr(Espressione ee, SymbToken t, Espressione e){
        symb=t;
        epr=ee;
        epr2=e;
    }
    public Op2Expr(IdentToken ele, SymbToken sb, Espressione e){
        symb=sb;
        epr=new IdentExpr(ele);
        epr2=e;
    }
    @Override
    public void println(int inter){
        String i="";
        for(int e=0; e<inter; e++){
            i+=" ";
        }
        System.out.println(i+symb.toString());
        System.out.println(i+"Elemento1:");
        epr.println(inter+2);
        System.out.println(i+"Elemento2:");
        epr2.println(inter+2);
    }
    private String modname;
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        String reImm=Aritm.retType(var, this, v);
        if(reImm!=null)
            return Types.getIstance().find(new TypeName(reImm), v);
        if(modname==null)
            setModname(var, v);
        return Funz.getIstance().request(modname).Return(v);
    }
    @Override
    public void substituteAll(Substitutor sub)throws CodeException{
        epr.substituteAll(sub);
        epr2.substituteAll(sub);
    }
    private void setModname(Variabili var, boolean v)throws CodeException{
        TypeElem[] tr=new TypeElem[2];
        tr[0]=epr.returnType(var, v);
        tr[1]=epr2.returnType(var, v);
        Funz.FElement fe=Funz.getIstance().request(symb.getString(), tr, v);
        modname=fe.modname;
        if(!v && (Environment.template||fe.isExternFile()))
            Funz.getIstance().ext.add(modname);
    }
    public String getName(){
        return symb.getString();
    }
    public Espressione[] getVars(){
        return new Espressione[]{epr, epr2};
    }
    @Override
    public void validate(Variabili var)throws CodeException{
        epr.validate(var);
        epr2.validate(var);
        boolean b=Aritm.validate(var, this);
        if(!b)
            Funz.getIstance().request(symb.getString(), new TypeElem[]{epr.returnType(var, true), 
                epr2.returnType(var, true)}, true);
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, 
            Accumulator acc)throws CodeException{
        if(Aritm.analyze(text, var, env, acc, this))
            return;
        if(modname==null)
            setModname(var, false);
        acc.pushAll(text);
        FunzExpr.perfCall(modname, returnType(var, false), 
                new Espressione[]{epr, epr2}, text, var, env, acc);
        acc.popAll(text);
    }
}