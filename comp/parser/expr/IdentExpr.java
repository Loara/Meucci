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
import comp.code.template.TNumbers;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.parser.Espressione;
import comp.parser.TypeName;
import comp.parser.template.NumDich;
import comp.parser.template.ParamDich;
import comp.scanner.IdentToken;

/**
 * Semplice variabile, da integrare con {@link IdentArray}. Non utilizzabile in altri
 * contesti
 * @author loara
 */
public class IdentExpr extends Espressione{
    private String val;
    //public long valNum; Non serve più, la sostituzione la effettua l'IdentArray
    public IdentExpr(IdentToken it){
        val=it.getString();
    }
    public IdentExpr(String s){
        val=s;
    }
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        String u;
        if(val==null || TNumbers.getIstance().isIn(val)){
            switch(TNumbers.getIstance().expDim(val)){
                case 0:
                    u="ubyte";
                    break;
                case 1:
                    u="ushort";
                    break;
                case 2:
                    u="uint";
                    break;
                case 3:
                    u="ulong";
                    break;
                default:
                    throw new CodeException("Non supportato");
            }
        }
        else{
            switch(val){
                case "true":
                case "false":
                    u="boolean";
                    break;
                case "null":
                    u=":null";
                    break;
                default:
                    return var.getType(val);
            }
        }
        return Types.getIstance().find(new TypeName(u), v);
    }
    @Override
    public void validate(Variabili vars)throws CodeException{
        if(val==null)
            return;
        if(TNumbers.getIstance().isIn(val))
            return;
        switch(val){
            case "true":
            case "false":
            case "null":
                return;
            default:
                vars.testIsIn(val);
        }
    }
    @Override
    public void toCode(Segmenti text, Variabili vars, Environment env, Accumulator acc)
            throws CodeException{
        //Non è necessario utilizzare il TNumber, in quanto la traduzione è
        //già effettuata
        switch(val){
            case "true":
                text.addIstruzione("mov", acc.getAccReg().getReg(1), "1");
                break;
            case "false":
                text.addIstruzione("xor", acc.getAccReg().getReg(1), acc.getAccReg().getReg(1));
                break;
            case "null":
                text.addIstruzione("xor", acc.getAccReg().getReg(), acc.getAccReg().getReg());
                break;
            default:
                if(TNumbers.getIstance().isIn(val)){
                    NumDich nd=TNumbers.getIstance().obtain(new ParamDich(val));
                    text.addIstruzione("mov", acc.getAccReg().getReg(1 << nd.expDim()), 
                            String.valueOf(nd.getNum()));
                }
                else
                    vars.getVar(text, acc, val);
        }
    }
    public boolean identEq(String t){
        return val.equals(t);
    }
    @Override
    public void println(int y){
        
    }
    public String val(){
        return val;
    }
}
