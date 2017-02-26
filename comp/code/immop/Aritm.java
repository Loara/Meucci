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
package comp.code.immop;
import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.parser.Espressione;
import comp.parser.expr.IdentArray;
import comp.parser.expr.NumExpr;
import comp.parser.expr.Op2Expr;

/**
 *
 * @author loara
 */
public class Aritm {
    public static final String[] Car2={"+", "-", "&", "|", ":xor"}, CarA2={"*", "/", ":mod"}, Sar2={"<<", ">>"},
            Bar2={"==", ">", "<", ">=", "<=", "!="}, bol2={"&&", "||", "->"},
            pt={"==", "!="}, XO2={"+", "-", "*", "/"};
    public static final String[] Car2V={"add", "sub", "and", "or", "xor"},CarA2V={"imul", "idiv", "idiv"}, 
            Sar2V={"sal", "sar", "shr"}, Bar2V={"e", "g", "l", "ge", "le", "ne"},
            XO2V={"addsd", "subsd", "mulsd", "divsd"};
    //bol2V={"z", "nz"}, non esiste
    public static int isIn(String t, String[] ar){
        for(int i=0; i<ar.length; i++)
            if(ar[i].equals(t))
                return i;
        return -1;
    }
    public static boolean analyze(Segmenti text
            , Variabili vars, Environment env, Accumulator acc, Op2Expr op)
            throws CodeException
    {
        TypeElem r1=op.getVars()[0].returnType(vars, false), r2=op.getVars()[1].returnType(vars, false);
        String ops=op.getName();
        boolean IA1=(op.getVars()[1] instanceof IdentArray && 
                ((IdentArray)op.getVars()[1]).isNum());
        int i=isIn(ops, Car2);
        if(i!=-1){
            if(r1.number && r2.number){
                int md=r1.realDim();
                if(md<r2.realDim())
                    md=r2.realDim();
                if(IA1){
                    long num=((IdentArray)op.getVars()[1]).numValue();
                    op.getVars()[0].toCode(text, vars, env, acc);
                    text.addIstruzione(Car2V[i], acc.getAccReg().getReg(md), String.valueOf(num));
                    return true;
                }
                int d=Aritm.initDataCl(text, vars, env, acc, op);
                text.addIstruzione(Car2V[i], acc.getAccReg().getReg(md), acc.getReg(d).getReg(md));
                acc.libera(d);
                return true;
            }
        }
        if(mult(text, vars, env, acc, op))
            return true;
        i=isIn(ops, Bar2);
            if(i!=-1 && r1.number && r2.number){
                int md=r1.realDim();
                if(md<r2.realDim())
                    md=r2.realDim();
            if(IA1){
                long num=((IdentArray)op.getVars()[1]).numValue();
                op.getVars()[0].toCode(text, vars, env, acc);
                text.addIstruzione("cmp", acc.getAccReg().getReg(md), String.valueOf(num));
                text.addIstruzione("set"+Bar2V[i], acc.getAccReg().getReg(1), null);
                return true;
            }
            int d=Aritm.initDataCl(text, vars, env, acc, op);
            text.addIstruzione("cmp", acc.getAccReg().getReg(md), acc.getReg(d).getReg(md));
            text.addIstruzione("set"+Bar2V[i], acc.getAccReg().getReg(1), null);
            acc.libera(d);
            return true;
        }
        if(i!=-1 && "real".equals(r1.name) && "real".equals(r2.name)){
            int d=Aritm.initDataClX(text, vars, env, acc, op);
            text.addIstruzione("comisd", acc.getXAccReg().getReg(), acc.getXReg(d).getReg());
            text.addIstruzione("set"+Bar2V[i], acc.getAccReg().getReg(1), null);
            acc.xlibera(d);
            return true;
        }
        i=isIn(ops, Sar2);
        if(i!=-1){
            if(!r1.number || !"ubyte".equals(r2.name))
                return false;
            int d=Aritm.initDataCl(text, vars, env, acc, op);
            int md=r1.realDim();
            if(i==1 && Info.unsignNum(r1.name))
                text.addIstruzione(Sar2V[2], acc.getAccReg().getReg(md), acc.getReg(d).getReg(1));
            else
                text.addIstruzione(Sar2V[i], acc.getAccReg().getReg(md), acc.getReg(d).getReg(1));
            acc.libera(d);
            return true;
        }
        i=isIn(ops, bol2);
        if(i!=-1){
            if("boolean".equals(r1.name) || !"boolean".equals(r2.name))
                return false;
            op.getVars()[0].toCode(text, vars, env, acc);
            env.increment("BOLC");
            int kk=env.get("BOLC");
            if(i==2){
                //a -> b = -a || b
                text.addIstruzione("xor", acc.getAccReg().getReg(1), "1");
                text.addIstruzione("test", acc.getAccReg().getReg(1), acc.getAccReg().getReg(1));
                text.addIstruzione("jnz", "BLOC"+kk, null);
                op.getVars()[1].toCode(text, vars, env, acc);
                text.addLabel("BLOC"+kk);
            }
            else{
                text.addIstruzione("test", acc.getAccReg().getReg(1), String.valueOf(1));
                if(i==0)
                    text.addIstruzione("jz", "BOLC"+kk, null);
                else
                    text.addIstruzione("jnz", "BOLC"+kk, null);
                op.getVars()[1].toCode(text, vars, env, acc);
                text.addLabel("BOLC"+kk);
            }
            return true;
        }
        i=isIn(ops, pt);
        if(i!=-1){
            if(!genericReference(r1) || !genericReference(r2)){
                return false;
            }
            String t="";
            if(i==1)
                t="n";
            if(r2.name.equals(":null")){
                op.getVars()[0].toCode(text, vars, env, acc);
                text.addIstruzione("test", acc.getAccReg().getReg(), acc.getAccReg().getReg());
                text.addIstruzione("set"+t+"z", acc.getAccReg().getReg(1), null);
                return true;
            }
            int d=Aritm.initDataCl(text, vars, env, acc, op);
            text.addIstruzione("cmp", acc.getAccReg().getReg(), acc.getReg(d).getReg());
            text.addIstruzione("set"+t+"e", acc.getAccReg().getReg(1), null);
            acc.libera(d);
            return true;
        }
        i=isIn(ops, XO2);
        if(i!=-1){
            if("real".equals(r1.name)&&"real".equals(r2.name)){
                int d=Aritm.initDataClX(text, vars, env, acc, op);
                text.addIstruzione(XO2V[i], acc.getXAccReg().getReg(), acc.getXReg(d).getReg());
                acc.xlibera(d);
                return true;
            }            
        }
        return false;
    }
    public static boolean validate(Variabili vars, Op2Expr op)throws CodeException{
        TypeElem t1=op.getVars()[0].returnType(vars, true), t2=op.getVars()[1].returnType(vars, true);
        String val=op.getName();
        int i=isIn(val, CarA2);
        if(i!=-1 && t1.number && t2.number)
            return true;
        i=isIn(val, Car2);
        if(i!=-1 && t1.number && t2.number)
            return true;
        i=isIn(val, Sar2);
        if(i!=-1 && t1.number && "ubyte".equals(t2.name))
            return true;
        i=isIn(val, Bar2);
        if(i!=-1 && t1.number && t2.number)
            return true;
        i=isIn(val, bol2);
        if(i!=-1 && "boolean".equals(t1.name) && "boolean".equals(t2.name))
            return true;
        i=isIn(val, pt);
        if(i!=-1){
            if(genericReference(t1) && genericReference(t2))
                return true;
        }
        i=isIn(val, XO2);
        return i!=-1 && "real".equals(t1.name) && "real".equals(t2.name);
    }
    private static boolean mult(Segmenti text, Variabili vars, Environment env, 
            Accumulator acc, Op2Expr op)throws CodeException{
        String ops=op.getName();
        TypeElem rt1=op.getVars()[0].returnType(vars, false), 
                rt2=op.getVars()[1].returnType(vars, false);
        int i=isIn(ops, CarA2);
        if(i!=-1){
            if(Info.varNum(rt1.name) && Info.varNum(rt2.name)){
                String code=CarA2V[i];
                boolean uns=Info.unsignNum(rt1.name)&&Info.unsignNum(rt2.name);
                int md=rt1.realDim();
                if(md<rt2.realDim())
                    md=rt2.realDim();
                if(op.getVars()[1] instanceof NumExpr){
                    long num=((NumExpr)op.getVars()[1]).value();
                    if(Long.bitCount(num)==1){
                        op.getVars()[0].toCode(text, vars, env, acc);
                        int n=Long.numberOfTrailingZeros(num);
                        switch(i){
                            case 0:
                                text.addIstruzione("shl", acc.getAccReg().getReg(md), 
                                    String.valueOf(n));
                                break;
                            case 1:
                                if(uns)
                                    text.addIstruzione("shr", acc.getAccReg().getReg(md), 
                                            String.valueOf(n));
                                else
                                    text.addIstruzione("sar", acc.getAccReg().getReg(md), 
                                            String.valueOf(n));
                                break;
                            default://2
                                text.addIstruzione("and", acc.getAccReg().getReg(md),
                                        String.valueOf(num-1));
                        }
                        return true;
                    }
                    //di solito con altri numeri non è banale: ricondursi al caso generale
                }
                int d=Aritm.initDataCl(text, vars, env, acc, op);
                acc.moveFrom(text, Register.AX, Register.DX);
                text.addIstruzione("mov", Register.AX.getReg(md), acc.getAccReg().getReg(md));
                if(uns){
                    text.addIstruzione("xor", "rdx", "rdx");
                    text.addIstruzione(code.substring(1), acc.getReg(d).getReg(md), null);
                }
                else{
                    int u=rt1.realDim();
                    switch(u){
                        case 1:
                            text.addIstruzione("cbw", null, null);//niente break
                            //in quanto non esiste un istruzione per settare
                            //direttamente dl
                        case 2:
                            text.addIstruzione("cwd", null, null);
                            break;
                        case 4:
                            text.addIstruzione("cdq", null, null);
                            break;
                        default:
                            text.addIstruzione("cqo", null, null);
                    }
                    text.addIstruzione(code, acc.getReg(d).getReg(md), null);
                }
                if(i==2)
                    text.addIstruzione("mov", acc.getAccReg().getReg(md), Register.DX.getReg(md));
                else
                    text.addIstruzione("mov", acc.getAccReg().getReg(md), Register.AX.getReg(md));
                acc.libera(d);
                return true;
            }
        }
        return false;
    }
    
    /*
    il primo elemento è in acc.getAccReg
    il secondo è puntato dal valore di ritorno
    */
    private static int initDataCl(Segmenti text
            , Variabili vars, Environment env, Accumulator acc, Op2Expr op)throws CodeException{
        int i;
        Espressione e1=op.getVars()[0], e2=op.getVars()[1];
        e1.toCode(text, vars, env, acc);
        i=acc.saveAccumulator();
        e2.toCode(text, vars, env, acc);
        acc.restoreAccumulatorB(i);
        return i;
    }
    private static int initDataClX(Segmenti text
            , Variabili vars, Environment env, Accumulator acc, Op2Expr op)throws CodeException{
        int i;
        Espressione e1=op.getVars()[0], e2=op.getVars()[1];
        e1.toCode(text, vars, env, acc);
        i=acc.xsaveAccumulator();
        e2.toCode(text, vars, env, acc);
        acc.xrestoreAccumulatorB(i);
        return i;
    }
    public static String retType(Variabili vars, Op2Expr op, boolean v)throws CodeException{
        TypeElem t1=op.getVars()[0].returnType(vars, v);
        TypeElem t2=op.getVars()[1].returnType(vars, v);
        if(t1.number&&t2.number){
            if(isIn(op.getName(), Car2)!=-1 || isIn(op.getName(), CarA2)!=-1)
                return retTypeNum(t1.name, t2.name);
            if(isIn(op.getName(), Bar2)!=-1)
                return "boolean";
            if("ubyte".equals(t2.name) && isIn(op.getName(), Sar2)!=-1)
                return retTypeNum(t1.name, t2.name);
            return null;
        }
        if("boolean".equals(t1.name)&&"boolean".equals(t2.name)){
            if(isIn(op.getName(), bol2)!=-1)
                return "boolean";
            return null;
        }
        if(genericReference(t1) && genericReference(t2)){
            if(isIn(op.getName(), pt)!=-1)
                return "boolean";
            return null;
        }
        if("real".equals(t1.name)&&"real".equals(t2.name)){
            if(isIn(op.getName(), XO2)!=-1)
                return "real";
            return null;
        }
        return null;
    }
    /*
    tipo di ritorno per operazioni con interi:  (promemoria):
    - Se almeno un operando è signed, il risultato è signed
    - almeno uno long, risultato long
    - almeno uno int, risultato int
    - analogo con unsigned
    */
    private static String retTypeNum(String n1, String n2){
        if(n1.equals(n2))
            return n1;
        String pref="";
        if(n1.startsWith("u") && n2.startsWith("u"))
            pref="u";
        if(n1.endsWith("long") || n2.endsWith("long"))
            return pref+"long";
        if(n1.endsWith("int") || n2.endsWith("int"))
            return pref+"int";
        if(n1.endsWith("short") || n2.endsWith("short"))
            return pref+"short";
        return pref+"byte";
    }
    private static boolean genericReference(TypeElem te){
        return te.reference || "pt".equals(te.name);//:null è reference
    }
}