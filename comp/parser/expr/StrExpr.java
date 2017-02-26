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
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.parser.Espressione;
import comp.parser.TypeName;
import comp.scanner.StrToken;

/**
 *
 * @author loara
 */
public class StrExpr extends Espressione{
    private final StrToken t;
    public StrExpr(StrToken i){
        t=i;
    }
    public StrToken getToken(){
        return t;
    }
    @Override
    public void println(int inter){
        String i="";
        for(int e=0; e<inter; e++){
            i+=" ";
        }
        System.out.println(i+t.toString());
    }
    
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        return Types.getIstance().find(new TypeName("String"), v);
    }
    @Override
    public void validate(Variabili vars){
        
    }
    @Override
    public void substituteAll(Substitutor sub)throws CodeException{
        
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, 
            Accumulator acc)throws CodeException{
        env.increment("STR");
        int i=env.get("STR");
        int len=t.getString().length()+1;
        //String vt="STRVT"+i+":\tdq\t0";//no vtable, tipo esplicito
        String allen="STRL"+i+":\tdd\t"+len;
        String allp="STRP"+i+":\tdq\t0";//puntatore
        String allc="STRS"+i+":\tdb\t\'"+t.getString()+"\',0";
        text.data.add("\talign\t"+Info.pointerdim+", db 0");//Utilizzato da NASM per allineare la memoria
        text.data.add(allen);
        text.data.add("\talign\t"+Info.pointerdim+", db 0");//Utilizzato da NASM per allineare la memoria
        text.data.add(allp);
        text.data.add(allc);
        int p=acc.prenota();
        text.addIstruzione("lea",acc.getAccReg().getReg(),"[STRL"+i+"]");
        text.addIstruzione("lea",acc.getReg(p).getReg(),"[STRS"+i+"]");
        text.addIstruzione("mov","[STRP"+i+"]",acc.getReg(p).getReg());
        acc.libera(p);
    }
}
