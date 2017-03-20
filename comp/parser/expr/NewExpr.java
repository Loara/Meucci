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
import comp.code.Funz.FElement;
import comp.code.Meth;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.parser.Espressione;
import comp.parser.TypeName;

/**
 * allocazione oggetti statica
 * @author loara
 */
public class NewExpr extends Espressione{
    private final TypeName type;
    private final Espressione[] exp;
    public NewExpr(TypeName ty, Espressione[] data){
        type=ty;
        exp=data;
    }
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        return Types.getIstance().find(type, v);
    }
    @Override
    public void validate(Variabili var)throws CodeException{
        TypeElem tp=Types.getIstance().find(type, true);
        TypeElem[] esp1=new TypeElem[exp.length];
        esp1[0]=tp;
        for(int i=0; i<exp.length; i++){
            exp[i].validate(var);
            esp1[i]=exp[i].returnType(var, true);
        }
        Funz.getIstance().requestCostructor(type, esp1, true);
        Funz.getIstance().request("allocate", new TypeElem[]{Types.getIstance().find("uint")}, true);
    }
    @Override
    public void toCode(Segmenti seg, Variabili var, Environment env, Accumulator acc)
        throws CodeException{
        TypeElem tp=Types.getIstance().find(type, false);
        TypeElem[] esp1=new TypeElem[exp.length];
        for(int i=0; i<exp.length; i++)
            esp1[i]=exp[i].returnType(var, false);
        FElement cos=Funz.getIstance().requestCostructor(type, esp1, false);
        FElement allc=Funz.getIstance().request("allocate", 
                new TypeElem[]{Types.getIstance().find("uint")}, false);
        if(Environment.template||allc.isExternFile())
            Funz.getIstance().ext.add(allc.modname);
        acc.pushAll(seg);
        Register a=acc.getAccReg();
        seg.addIstruzione("mov", a.getReg(4), String.valueOf(tp.dimension(false)));
        seg.addIstruzione("push", a.getReg(), null);
        seg.addIstruzione("call",allc.modname,null);
        if(tp.explicit){
            if(Environment.template || tp.external || type.templates().length!=0)
                Funz.getIstance().ext.add(cos.modname);
            seg.addIstruzione("push",Register.AX.getReg(), null);
            seg.addIstruzione("push",Register.AX.getReg(), null);
        }
        else{
            if(Environment.template || tp.external || type.templates().length!=0){
                if(!tp.explicit)
                    Funz.getIstance().ext.add("_INIT_"+Meth.className(type));
                Funz.getIstance().ext.add(cos.modname);
            }
            seg.addIstruzione("push",Register.AX.getReg(), null);
            seg.addIstruzione("push",Register.AX.getReg(), null);//3 volte, la seconda per il costruttore vero, la terza il valore di ritorno
            if(!tp.explicit){
                seg.addIstruzione("push",Register.AX.getReg(), null);
                seg.addIstruzione("call","_INIT_"+Meth.className(type), null);
                //Non necessario il salvaTutto, in quanto non vi sono accessi in memoria
            }
        }
        for(Espressione esp:exp){
            esp.toCode(seg, var, env, acc);
            seg.addIstruzione("push",acc.getAccReg().getReg(),null);
        }
        seg.addIstruzione("call",cos.modname,null);//costruttore vero, non genera eccezioni
        seg.addIstruzione("pop",a.getReg(), null);//vero valore di ritorno
        acc.popAll(seg);
    }
    @Override
    public void println(int i){
        
    }
}
