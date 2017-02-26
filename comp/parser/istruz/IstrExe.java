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

import comp.general.Stack;
import comp.general.VScan;
import comp.parser.Dichiarazione;
import comp.parser.Espressione;
import comp.parser.FineArrayException;
import comp.parser.Istruzione;
import comp.parser.ParserException;
import static comp.parser.expr.ExprGen.toExpr;
import comp.parser.expr.*;
import comp.parser.template.Template;
import comp.parser.template.TemplateEle;
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
                else throw new ParserException("Break errato", t); 
            }
            if(idt.getString().equals("continue")){
                if(t.get() instanceof EolToken){
                    t.nextEx();
                    return new ContinueIstr();
                }
                else throw new ParserException("Continue errato", t);
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
                    else throw new ParserException("Manca ; di return", t);
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
                        throw new ParserException("For errato", t);
                    if(!t.next()){
                        throw new FineArrayException();
                    }
                    Istruzione i, f;
                    if(t.get() instanceof EolToken){
                        i=null;
                    }
                    else{
                        i=toIstr(t, true);
                        if(!(i instanceof ClassisIstr))
                            throw new ParserException("Istruzione non adatta al for", t);
                    }
                    Espressione e;
                    if(t.get() instanceof EolToken)
                        e=null;
                    else{
                        e=toExpr(t);
                    }
                    if(!(t.get() instanceof EolToken)){
                        throw new ParserException("Manca ; nel for", t);
                    }
                    t.nextEx();
                    if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')')
                        f=null;
                    else{
                        f=toIstr(t, false);
                        if(!(f instanceof ClassisIstr))
                            throw new ParserException("Valore non idoneo per for", t);
                        t.previous();
                    }
                    if(!(t.get() instanceof PareToken && ((PareToken)t.get()).s==')')){
                        throw new ParserException("Manca ) nel for", t);
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
            //dichiarazione
            int i=t.getInd();
            TemplateEle[] te=Template.detectTemplate(t);
            if(t.get() instanceof IdentToken){
                String lval=((IdentToken)t.get()).getString();
                t.nextEx();
                Dichiarazione d=new Dichiarazione(idt.getString(), lval, te);//chiaramente
                if(t.get() instanceof UgualToken){
                    UgualToken ug=(UgualToken)t.get();
                    if(ug.getSymb()!=null)
                        throw new ParserException("Variabile "+d.getIdent()+" non inizializzata", t);
                    if(!t.next()){
                        throw new FineArrayException();
                    }
                        Espressione e=toExpr(t);
                        if(!checkEol || t.get() instanceof EolToken){
                            t.next();
                            return new ClassisIstr(d, ug, null, e);
                        }
                        else{
                            throw new ParserException("Dichiarazione errata, manca ;", t);
                        }
                }
                else if(!checkEol || t.get() instanceof EolToken){
                    t.next();
                    return new ClassisIstr(d, null, null, null);
                }
                else{
                    throw new ParserException("Dichiarazione errata", t);
                }
            }
            else{
                t.setInd(i);//ripristinare tutto
            }
            t.previous();//reinserisce l'identtoken nello scan
        }
        if(t.get() instanceof SymbToken && ((SymbToken)t.get()).getString().equals(":destroy")){
            t.nextEx();
            Espressione exp=ExprGen.toExpr(t);
            if(!(exp instanceof IdentArray))
                throw new ParserException("Parametro non adatto al distruttore", t);
            if(checkEol){
                if(!(t.get() instanceof EolToken))
                    throw new ParserException("Manca ;", t);
                else
                    t.nextEx();
            }
            return new DesIstr((IdentArray)exp);
        }
        //multi
        if(t.reqSpace(2) && t.get() instanceof PareToken && ((PareToken)t.get()).s=='{'){
            t.next();
            Stack<Istruzione> i=new Stack<>(Istruzione.class);
            while(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='}')){
                Istruzione e=toIstr(t);
                i.push(e);
            }
            t.next();
            MultiIstr n= new MultiIstr(i.toArray());
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
                    return new ClassisIstr(null, ut, (IdentArray)e, ee);
                }
            }
            throw new ParserException("Assegnazione non valida", t);
        }else if(!checkEol || t.get() instanceof EolToken){
            t.next();
            return new ClassisIstr(null, null, null, e);
        }
        else{
            throw new ParserException("Manca ; di espressione", t);
        }
    }
}
