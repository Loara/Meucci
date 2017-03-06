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
import comp.parser.Espressione;
import comp.parser.TypeName;
import comp.scanner.IntToken;

/**
 *
 * @author loara
 */
public class NumExpr extends Espressione{
    private final long nu;
    private final boolean unsigned;
    private final int dim;
    public NumExpr(IntToken i){
        nu=i.s;
        unsigned=i.unsigned;
        switch(i.val){
            case 'L':
            case 'l':
                dim=8;
                break;
            case 'S':
            case 's':
                dim=2;
                break;
            case 'B':
            case 'b':
                dim=1;
                break;
            default:
                dim=4;
        }
    }
    public NumExpr(long num){
        nu=num;
        unsigned=true;
        dim=8;
    }
    @Override
    public void println(int inter){
        String i="";
        for(int e=0; e<inter; e++){
            i+=" ";
        }
        System.out.println(i);
    }
    @Override
    public TypeElem returnType(Variabili val, boolean v)throws CodeException{
        String p="";
        if(unsigned)
            p="u";
        switch(dim){
            case 1:
                p+="byte";
                break;
            case 8:
                p+="long";
                break;
            case 2:
                p+="short";
                break;
            default:
                p+="int";
        }
        return Types.getIstance().find(new TypeName(p), v);
    }
    public boolean unsigned(){
        return unsigned;
    }
    public long value(){
        return nu;
    }
    public int getDim(){
        return dim;
    }
    @Override
    public void validate(Variabili vars){
        //Sempre valido
    }
    @Override
    public void toCode(Segmenti text
            , Variabili var, Environment env, Accumulator acc)throws CodeException{
        text.addIstruzione("mov", acc.getAccReg().getReg(dim), String.valueOf(nu));
        /*
        RICORDATI: solo l'istruzione MOV permette l'utilizzo di valori immediati
        a 64 bit. Le altre rimangono a 32 
        Da modificare
        */
    }
}
