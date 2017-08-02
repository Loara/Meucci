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
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.parser.Espressione;
import comp.parser.TypeName;

/**
 *
 * @author loara
 */
public class CastExpr extends Espressione{
    private final TypeName type;
    private final Espressione esp;
    public CastExpr(TypeName type, Espressione es){
        this.type=type;
        esp=es;
    }
    @Override
    public TypeElem returnType(Variabili vars, boolean v)throws CodeException{
        return Types.getIstance().find(type, v);
    }
    @Override
    public void validate(Variabili vars)throws CodeException{
        esp.validate(vars);
        //Tolti tutti i possibili controlli sul cast
    }
    @Override
    public void toCode(Segmenti text, Variabili vars, Environment env, Accumulator acc)
            throws CodeException{
        TypeElem t2=esp.returnType(vars, false);
        TypeElem t1=Types.getIstance().find(type, false);
        esp.toCode(text, vars, env, acc);
        if(t1.isNum() && t2.isNum()){
            int dt1=t1.realDim();//tipo di ritorno
            int dt2=t2.realDim();//ritorno espressione
            castWrt(text, acc.getAccReg(), dt2, dt1, !t1.isUnsignedNum());
        }
    }
    public static void castWrt(Segmenti seg, Register reg, int odim, int ndim, boolean sign)
    throws CodeException{
        if(odim >= ndim)
            return;
        if(sign){
            if(ndim==8 && ndim==4){
                seg.addIstruzione("movsxd", reg.getReg(), reg.getReg(4));
            }
            else
                seg.addIstruzione("movsx", reg.getReg(ndim), reg.getReg(odim));
        }
        else{
            if(ndim==8 && odim==4){
                //NON È NECESSARIO IN QUANTO OGNI ISTRUZIONE A 32 BIT
                //AZZERA I 32 BIT PIÙ SIGNIFICATIVI (Intel vol 1 §3.4.2)
            }
            else
                seg.addIstruzione("movzx", reg.getReg(ndim), reg.getReg(odim));
        }
    }
    @Override
    public void println(int i){
        
    }
}
