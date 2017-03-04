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
package comp.code.immop;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Segmenti;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.parser.expr.Op1Expr;
import comp.code.TypeElem;

/**
 *
 * @author loara
 */
public class Aritm2 {
    public static final String[] Ar={"++", "--", "~", "-"}, Lr={"!"}, XS={":sqrt"}, Ys={":Ilog"};
    public static final String[] ArV={"inc", "dec", "not", "neg"}, XSV={"sqrtsd"}, YsV={"bsr"};
    public static boolean analyze(Segmenti text
            , Variabili vars, Environment env, Accumulator acc, Op1Expr op)throws CodeException
    {
        if(Info.varNum(op.getExpr().returnType(vars, false).name)){
            int i=Aritm.isIn(op.getName(), Ar);
            if(i!=-1){
                initAr(text, vars, env, acc, op, false, ArV[i]);
                return true;
            }
        }
        if(op.getExpr().returnType(vars, false).isUnsignedNum()){
            int i=Aritm.isIn(op.getName(), Ys);
            if(i!=-1){
                int d1=op.getExpr().returnType(vars, false).realDim();
                op.getExpr().toCode(text, vars, env, acc);
                if(d1==1){
                    text.addIstruzione("movzx", acc.getAccReg().getReg(2), acc.getAccReg().getReg(1));
                    d1=2;
                }
                text.addIstruzione(YsV[i],acc.getAccReg().getReg(d1), acc.getAccReg().getReg(d1));
                return true;
            }
            else return false;
        }
        else if("boolean".equals(op.getExpr().returnType(vars, false).name)){
            int i=Aritm.isIn(op.getName(), Lr);
            if(i!=-1){
                op.getExpr().toCode(text, vars, env, acc);
                text.addIstruzione("xor", acc.getAccReg().getReg(1), "1");
                return true;
            }
            return false;
        }
        else if("real".equals(op.getExpr().returnType(vars, false).name)){
            if(":sqrt".equals(op.getName())){
                op.getExpr().toCode(text, vars, env, acc);
                text.addIstruzione("sqrtsd", acc.getXAccReg().getReg(), acc.getXAccReg().getReg());
                return true;
            }
            return false;
        }
        else return false;
    }
        private static void initAr(Segmenti text, Variabili vars, Environment env, 
                Accumulator acc, Op1Expr op, boolean cmp, String oper)throws CodeException{
            int d1=op.getExpr().returnType(vars, false).realDim();
            op.getExpr().toCode(text, vars, env, acc);
            if(cmp){
                text.addIstruzione("test",acc.getAccReg().getReg(1),acc.getAccReg().getReg(1));
            //più corto rispetto a cmp
                text.addIstruzione("set"+oper,acc.getAccReg().getReg(1), null);
            }
            else{
                text.addIstruzione(oper,acc.getAccReg().getReg(d1), null);
            }
    }
    public static TypeElem returnType(Op1Expr op, Variabili vars, boolean v)throws CodeException{
        String s=op.getName();
        TypeElem ty=op.getExpr().returnType(vars, v);
        if(Aritm.isIn(s, Ar)!=-1){
            if(ty.isNum()){
                return ty;
            }
            else return null;
        }
        else if(Aritm.isIn(s, Ys)!=-1){
            if(ty.isUnsignedNum()){
                return Types.getIstance().find("ubyte");
            }
            else return null;
        }
        else if(Aritm.isIn(s, Lr)!=-1){
            if(ty.name.equals("boolean"))
                return Types.getIstance().find("boolean");
            else return null;
        }
        else if(Aritm.isIn(s, XS)!=-1){
            if(ty.name.equals("real"))
                return Types.getIstance().find("real");
            else return null;
        }
        return null;
    }
    public static boolean validate(Variabili var, Op1Expr op)throws CodeException{
        String s=op.getName();
        TypeElem ty=op.getExpr().returnType(var, true);
        if(Aritm.isIn(s, Ar)!=-1){
            return ty.isNum();
        }
        else if(Aritm.isIn(s, Ys)!=-1){
            return ty.isUnsignedNum();
        }
        else if(Aritm.isIn(s, Lr)!=-1){
            return ty.name.equals("boolean");
        }
        else if(Aritm.isIn(s, XS)!=-1){
            return ty.name.equals("real");
        }
        return false;
    }
}
