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
package comp.parser.istruz;

import comp.general.Info;
import comp.general.Lingue;
import comp.general.Stack;
import comp.general.VScan;
import comp.parser.Dichiarazione;
import comp.parser.Espressione;
import comp.parser.FineArrayException;
import comp.parser.Istruzione;
import comp.parser.ParserException;
import comp.parser.TypeName;
import static comp.parser.expr.ExprGen.toExpr;
import comp.parser.expr.*;
import comp.scanner.ASMToken;
import comp.scanner.EolToken;
import comp.scanner.IdentToken;
import comp.scanner.PareToken;
import comp.scanner.SymbToken;
import comp.scanner.Token;
import comp.scanner.UgualToken;

/**
 *
 * @author loara
 */
public class IstrExe {
    public static Istruzione toIstr(VScan<Token> t)throws ParserException{
        return toIstr(t, true);
    }
    /**
     * 
     * @param t
     * @param checkEol se true verifica che le ClassisIstr terminano con ;
     * @return
     * @throws ParserException 
     */
    public static Istruzione toIstr(VScan<Token> t, boolean checkEol)throws ParserException{
        if(t.isEnded())
            throw new FineArrayException();
        if(t.get() instanceof ASMToken){
            ASMToken at=(ASMToken)t.get();
            t.next();
            return new ASMIstr(at);
        }
        if(t.get() instanceof IdentToken){
            IdentToken idt=(IdentToken)t.get();
            t.nextEx();
            if(idt.getString().equals("break")){
                if(t.get() instanceof EolToken){
                    t.nextEx();
                    return new BreakIstr();
                }
                else throw new ParserException(Lingue.getIstance().format("m_par_generr", "break"), t); 
            }
            if(idt.getString().equals("continue")){
                if(t.get() instanceof EolToken){
                    t.nextEx();
                    return new ContinueIstr();
                }
                else throw new ParserException(Lingue.getIstance().format("m_par_generr", "continue"), t);
            }
            if(idt.getString().equals("return")){
                if(t.get() instanceof EolToken){
                    t.nextEx();
                    return new ReturnIstruz(null);
                }
                else{
                    Espressione e=toExpr(t);
                    if(t.get() instanceof EolToken){
                        t.next();
                        return new ReturnIstruz(e);
                    }
                    else throw new ParserException(Lingue.getIstance().format("m_par_dotcom"), t);
                }
            }
            //if
            if(idt.getString().equals("if")){
                IfIstr fi=new IfIstr();
                Espressione i=toExpr(t);
                Istruzione d=toIstr(t);
                fi.con=i;
                fi.ifi=d;
                if(t.get() instanceof IdentToken && ((IdentToken)t.get())
                            .getString().equals("else")){
                    t.nextEx();
                    Istruzione dd=toIstr(t);
                    fi.ele=dd;
                }
                return fi;
            }
            if(idt.getString().equals("while")){
                    Espressione e=toExpr(t);//considera le parentesi
                    Istruzione i=toIstr(t);
                    WhileIstr wi=new WhileIstr();
                    wi.e=e;
                    wi.d=i;
                    return wi;
            }
            if(idt.getString().equals("for")){
                if(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='('))
                    throw new ParserException(Lingue.getIstance().format("m_par_generr", "for"), t);
                if(!t.next()){
                    throw new FineArrayException();
                }
                Istruzione i, f;
                if(t.get() instanceof EolToken){
                    i=null;
                    t.nextEx();
                }
                else{
                    i=toIstr(t, true);
                    if(!(i instanceof ClassisIstr))
                        throw new ParserException(Lingue.getIstance().format("m_par_fisnad", "for"), t);
                }
                Espressione e;
                if(t.get() instanceof EolToken){
                    e=null;
                    t.nextEx();
                }
                else{
                    e=toExpr(t);
                }
                if(!(t.get() instanceof EolToken)){
                    throw new ParserException(Lingue.getIstance().format("m_par_dotcom"), t);
                }
                t.nextEx();
                if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')')
                    f=null;
                else{
                    f=toIstr(t, false);
                    if(!(f instanceof ClassisIstr))
                        throw new ParserException(Lingue.getIstance().format("m_par_fisnad", "for"), t);
                    t.previous();
                }
                if(!(t.get() instanceof PareToken && ((PareToken)t.get()).s==')')){
                    throw new ParserException(Lingue.getIstance().format("m_par_parncl"), t);
                }
                t.nextEx();
                Istruzione doi=toIstr(t);
                ForIstr fi=new ForIstr();
                fi.frist=(ClassisIstr)i;
                fi.wh=e;
                fi.after=(ClassisIstr)f;
                fi.doi=doi;
                return fi;
            }
            //try
            if(idt.getString().equals("try")){
                Istruzione u=toIstr(t, true);
                if(!(u instanceof MultiIstr))
                    throw new ParserException(Lingue.getIstance().format("m_par_fisnad", "try"), t);
                MultiIstr mi=(MultiIstr)u;
                Stack<String> en=new Stack<>(String.class);
                Stack<MultiIstr> mu=new Stack<>(MultiIstr.class);
                while(t.get() instanceof IdentToken && 
                        ((IdentToken)t.get()).getString().equals("catch")){
                    t.nextEx();
                    if(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='('))
                        throw new ParserException(Lingue.getIstance().format("m_par_generr", "catch"), t);
                    t.nextEx();
                    if(t.get() instanceof IdentToken)
                        en.push(((IdentToken)t.get()).getString());
                    else
                        throw new ParserException(Lingue.getIstance().format("m_par_ejxnvl"), t);
                    t.nextEx();
                    if(!(t.get() instanceof PareToken && ((PareToken)t.get()).s==')'))
                        throw new ParserException(Lingue.getIstance().format("m_par_generr", "catch"), t);
                    t.nextEx();
                    u=IstrExe.toIstr(t, true);
                    if(!(u instanceof MultiIstr))
                       throw new ParserException(Lingue.getIstance().format("m_par_fisnad", "catch"), t);
                    mu.push((MultiIstr)u);
                }
                MultiIstr def;
                if(t.get() instanceof IdentToken && ((IdentToken)t.get())
                        .getString().equals("default")){
                    t.nextEx();
                    u=IstrExe.toIstr(t, true);
                    if(!(u instanceof MultiIstr))
                       throw new ParserException(Lingue.getIstance().format("m_par_fisnad", "default"), t);
                    def=(MultiIstr)u;
                }
                else
                    def=null;
                return new TryIstr(mi, en.toArray(), mu.toArray(), def, idt.getRiga());
            }
            //throw
            if(idt.getString().equals("throw")){
                ThrowIstr ti;
                if(t.get() instanceof IdentToken)
                    ti=new ThrowIstr(((IdentToken)t.get()).getString());
                else
                    throw new ParserException(Lingue.getIstance().format("m_par_ejxnvl"), idt.getRiga());
                t.nextEx();
                if(checkEol){
                    if(t.get() instanceof EolToken){
                        t.nextEx();
                        return ti;
                    }
                    else throw new ParserException(Lingue.getIstance().format("m_par_dotcom"), t);
                }
                else return ti;
            }
            t.previous();
        }
        
        if(t.get() instanceof SymbToken){
            SymbToken st=(SymbToken)t.get();
            if(st.getString().equals(":destroy")){
                t.nextEx();
                Espressione exp=ExprGen.toExpr(t);
                if(checkEol){
                    if(!(t.get() instanceof EolToken))
                        throw new ParserException(Lingue.getIstance().format("m_par_dotcom"), t);
                    else
                        t.nextEx();
                }
                return new DesIstr((IdentArray)exp);
            }
            if(st.getString().equals(":super")){
                t.nextEx();
                Espressione exp=ExprGen.toExpr(t);
                if(!(exp instanceof FunzExpr))
                    throw new ParserException("bula", t);
                if(checkEol){
                    if(!(t.get() instanceof EolToken))
                        throw new ParserException(Lingue.getIstance().format("m_par_dotcom"), t);
                    else
                        t.nextEx();
                }
                return new SuperIstr((FunzExpr)exp);
            }
        }
        //multi
        if(t.reqSpace(2) && t.get() instanceof PareToken && ((PareToken)t.get()).s=='{'){
            t.nextEx();
            Stack<Dichiarazione> dec = new Stack<>(Dichiarazione.class);
            if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString()
                    .equals("declare")){
                t.nextEx();
                if(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='{'))
                    throw new ParserException(Lingue.getIstance().format("m_par_decmnc"), t);
                t.nextEx();
                while(!(t.get() instanceof PareToken && 
                        ((PareToken)t.get()).s=='}')){
                    TypeName tn = new TypeName(t);
                    do{
                        if(t.get() instanceof IdentToken){
                            String name = ((IdentToken)t.get()).getString();
                            Info.isForbitten(name, t.get().getRiga());
                            t.nextEx();
                            dec.push(new Dichiarazione(tn, name));
                        }
                        else throw new ParserException(Lingue.getIstance().format("m_par_invnam"), t);
                    }
                    while(!(t.get() instanceof EolToken));
                    t.nextEx();
                }
                t.nextEx();
            }
            Stack<Istruzione> i=new Stack<>(Istruzione.class);
            while(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='}')){
                Istruzione e=toIstr(t);
                i.push(e);
            }
            t.next();
            MultiIstr n= new MultiIstr(i.toArray(), dec.toArray());
            return n;
        }
        //Espressione - assegnazione
        Espressione e=toExpr(t);
        if(e instanceof IdentArray){
            if(t.get() instanceof UgualToken){
                UgualToken ut=(UgualToken)t.get();
                t.nextEx();
                Espressione ee=toExpr(t);
                if(!checkEol || t.get() instanceof EolToken){
                    t.next();
                    return new ClassisIstr(ut, (IdentArray)e, ee);
                }
            }
            throw new ParserException(Lingue.getIstance().format("m_par_invass"), t);
        }
        else if(!checkEol || t.get() instanceof EolToken){
            t.next();
            return new ClassisIstr(null, null, e);
        }
        else{
            throw new ParserException(Lingue.getIstance().format("m_par_dotcom"), t);
        }
    }
}
