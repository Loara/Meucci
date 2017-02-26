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
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.parser.Espressione;
import comp.parser.TypeName;

/**
 * allocazione oggetti statica
 * @author loara
 */
public class StackExpr extends Espressione{
    private TypeName type;
    private final Espressione[] exp;
    public StackExpr(TypeName t, Espressione[] data){
        type=t;
        exp=data;//temporaneo
    }
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        return Types.getIstance().find(type, v);
    }
    @Override
    public void validate(Variabili var)throws CodeException{
        TypeElem tp=Types.getIstance().find(type, true);
        TypeElem[] esp1=new TypeElem[exp.length+1];
        esp1[0]=tp;
        for(int i=0; i<exp.length; i++){
            exp[i].validate(var);
            esp1[i+1]=exp[i].returnType(var, true);
        }
        Funz.getIstance().request(Meth.costructorName(type), esp1, false, type.templates());
    }
    @Override
    public void substituteAll(Substitutor sub)throws CodeException{
        type=sub.recursiveGet(type);
        for(Espressione es:exp){
            es.substituteAll(sub);
        }
    }
    @Override
    public void toCode(Segmenti seg, Variabili var, Environment env, Accumulator acc)
        throws CodeException{
        TypeElem tp=Types.getIstance().find(type, false);
        TypeElem[] esp1=new TypeElem[exp.length+1];
        esp1[0]=tp;
        for(int i=0; i<exp.length; i++)
            esp1[i+1]=exp[i].returnType(var, false);
        FElement cos=Funz.getIstance().request(Meth.costructorName(type), esp1, false, type.templates());
        //analogo a :new
        acc.pushAll(seg);
        int l=var.allocStack(tp.dimension(false), !tp.explicit);
        seg.addIstruzione("lea",Register.AX.getReg(),"[rbp-"+l+"]");
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
        acc.popAll(seg);
    }
    @Override
    public void println(int i){
        
    }
}
