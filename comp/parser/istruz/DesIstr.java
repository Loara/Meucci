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
package comp.parser.istruz;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Funz;
import comp.code.Funz.FElement;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.parser.Istruzione;
import comp.parser.expr.IdentArray;

/**
 * Utilizza la vtable per trovare eventuale distruttore
 * @author loara
 */
public class DesIstr extends Istruzione{
    private final IdentArray exp;
    public DesIstr(IdentArray data){
        exp=data;
    }
    @Override
    public void validate(Variabili var, Environment env)throws CodeException{
        exp.validate(var);
        exp.canWrite(var, true);
        TypeElem te=exp.returnType(var, true);
        if(!te.reference)
            throw new CodeException("Tipo non refernce");
        Funz.getIstance().request("free", new TypeElem[]{Types.getIstance().find("pt")}, false);
    }
    @Override
    public void substituteAll(Substitutor sub)throws CodeException{
        exp.substituteAll(sub);
    }
    @Override
    public void toCode(Segmenti seg, Variabili var, Environment env, Accumulator acc)throws CodeException{
        TypeElem te=exp.returnType(var, false);
        if(!te.reference)
            throw new CodeException("Tipo non reference");
        exp.canWrite(var, false);
        exp.toCode(seg, var, env, acc);
        int rd=acc.saveAccumulator();
        int ii;
        seg.addIstruzione("push", acc.getReg(rd).getReg(), null);
        if(!te.explicit){
            seg.addIstruzione("mov", acc.getAccReg().getReg(), "["+acc.getReg(rd).getReg()+"]");//indirizzo vtable
            seg.addIstruzione("test", acc.getAccReg().getReg(), acc.getAccReg().getReg());
            env.increment("DES");
            ii=env.get("DES");
            seg.addIstruzione("jz", "DES"+ii, null);
            seg.addIstruzione("push", acc.getReg(rd).getReg(), null);//2 volte
            seg.addIstruzione("call", "["+acc.getAccReg().getReg()+"]", null);//distruttore ad offset zero
            seg.addLabel("DES"+ii);
        }
        FElement fe=Funz.getIstance().request("free", new TypeElem[]{Types.getIstance().find("pt")},
                false);
        if(Environment.template||fe.isExternFile())
            Funz.getIstance().ext.add(fe.modname);
        seg.addIstruzione("call", fe.modname, null);
        acc.restoreAccumulator(rd);        
    }
    @Override
    public void println(int i){
        
    }
}
