/*
 * Copyright (C) 2016 loara
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
import comp.code.FElement;
import comp.code.Meth;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.vars.VarStack.VarEle;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.parser.Espressione;
import comp.parser.TypeName;
import comp.parser.template.TemplateEle;

/**
 * allocazione oggetti statica
 * @author loara
 */
public class StackExpr extends Espressione{
    private final String name;
    private final Espressione[] vals;
    private final TemplateEle[] temp;
    public StackExpr(FunzExpr f){
        name=f.getName();
        vals=f.getValues();
        temp=f.template();
    }
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        FElement fee=Funz.getIstance().requestCostructor(name, 
                temp, Info.paramTypes(vals, var, v), v);
        return Types.getIstance().find(fee.trequest[0], v);
    }
    @Override
    public void validate(Variabili var)throws CodeException{
        Funz.getIstance().esisteCostructor(name, temp, Info.paramTypes(vals, var, true));
    }
    @Override
    public void toCode(Segmenti seg, Variabili var, Environment env, Accumulator acc)
        throws CodeException{
        FElement cos=Funz.getIstance().requestCostructor(name, temp, 
                Info.paramTypes(vals, var, false), false);
        TypeElem tp=Types.getIstance().find(cos.trequest[0], false);
        if(cos.isExternFile())
            Funz.getIstance().ext.add(cos.modname);
        FunzExpr.allc1(seg, var, env, acc, vals);
        VarEle ve=var.allocStack(tp.dimension(false), true);
        seg.addIstruzione("lea", acc.getAccReg().getReg(), var.getVarStack().varPos(ve));
        FunzExpr.allc2(seg, var, env, acc, vals, cos, tp);
        /*
        //analogo a :new
        var.getVarStack().pushAll(seg);
        //Bisogna stare attenti ora che possono essere generate eccezioni:
        //Il blocco non deve essere settato subito per permettere l'esecuzione
        //del distruttore, ma deve essere garantita la corretta terminazione
        //del costruttore per poter invocare il distruttore
        VarEle vel=var.allocStack(tp.dimension(false), false);
        seg.addIstruzione("lea",Register.AX.getReg(),var.getVarStack().varPos(vel));
        if(tp.explicit){
            if(Environment.template || type.templates().length!=0 || tp.external)
                Funz.getIstance().ext.add(cos.modname);
            seg.addIstruzione("push",Register.AX.getReg(), null);
            seg.addIstruzione("push",Register.AX.getReg(), null);
        }
        else{
            if(Environment.template||type.templates().length!=0||tp.external){
                if(!tp.explicit)
                    Funz.getIstance().ext.add("_INIT_"+Meth.className(type));
                Funz.getIstance().ext.add(cos.modname);
            }
            seg.addIstruzione("push",Register.AX.getReg(), null);
            seg.addIstruzione("push",Register.AX.getReg(), null);
            if(!tp.explicit){
                seg.addIstruzione("push",Register.AX.getReg(), null);
                //3 volte, la seconda per il costruttore vero, la terza il valore di ritorno
                seg.addIstruzione("call","_INIT_"+Meth.className(type), null);
                    //inizializzatore (collega l'oggetto alla vtable)
            }
        }
        for(Espressione esp:exp){
            esp.toCode(seg, var, env, acc);
            seg.addIstruzione("push",acc.getAccReg().getReg(),null);
        }
        seg.addIstruzione("call",cos.modname,null);//costruttore vero
        seg.addIstruzione("pop",acc.getAccReg().getReg(), null);//vero valore di ritorno
        vel.destroyable = !tp.explicit;
        acc.popAll(seg);
        */
    }
    @Override
    public void println(int i){
        
    }
}
