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
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.parser.Espressione;
import comp.parser.Funzione;
import comp.parser.template.TemplateEle;

/**
 * allocazione oggetti dinamica
 * 
 * L'allocazione segue le seguenti regole:
 * - chiama i parametri (se si incorre in un eccezione l'allocazione termina)
 * - alloca memoria (dinamica, stack o statica)
 * - collega la vtable alla memoria allocata
 * - chiama il costruttore
 * - mette nel registro accumulatore il puntatore all'oggetto
 * @author loara
 */
public class NewExpr extends Espressione{
    private final String name;
    private final Espressione[] vals;
    private final TemplateEle[] temp;
    public NewExpr(FunzExpr f){
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
        Funz.getIstance().esiste(Funzione.funzName("allocate"), new TypeElem[]{Types.getIstance().find("uint")});
    }
    @Override
    public void toCode(Segmenti seg, Variabili var, Environment env, Accumulator acc)
        throws CodeException{
        FElement cos=Funz.getIstance().requestCostructor(name, temp, 
                Info.paramTypes(vals, var, false), false);
        FElement allc=Funz.getIstance().request(Funzione.funzName("allocate"), 
                new TypeElem[]{Types.getIstance().find("uint")}, false);
        TypeElem tp=Types.getIstance().find(cos.trequest[0], false);
        if(allc.isExternFile())
            Funz.getIstance().ext.add(allc.modname);
        if(cos.isExternFile())
            Funz.getIstance().ext.add(cos.modname);
        FunzExpr.allc1(seg, var, env, acc, vals);
        FunzExpr.perfCall(allc, new Espressione[]{new NumExpr(tp.dimension(false), true, 4)}
                , seg, var, env, acc);
        FunzExpr.allc2(seg, var, env, acc, vals, cos, tp);
    }
    @Override
    public void println(int i){
        
    }
}
