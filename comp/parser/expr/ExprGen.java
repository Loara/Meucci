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

import comp.general.Info;
import comp.general.Lingue;
import comp.general.Stack;
import comp.general.VScan;
import comp.parser.Espressione;
import comp.parser.FineArrayException;
import comp.parser.ParserException;
import comp.parser.TypeName;
import comp.parser.template.FunzDich;
import comp.parser.template.Template;
import comp.parser.template.TemplateEle;
import comp.scanner.CharToken;
import comp.scanner.DotToken;
import comp.scanner.HashToken;
import comp.scanner.IdentToken;
import comp.scanner.IntToken;
import comp.scanner.PareToken;
import comp.scanner.RealToken;
import comp.scanner.StrToken;
import comp.scanner.SymbToken;
import comp.scanner.Token;
import comp.scanner.VirgToken;

/**
 * Nota: senza parentesi, le operazioni unarie hanno la precedenza su quelle binarie e l'associatività è
 * da sinistra a destra.
 * @author loara
 */
public class ExprGen {
    //tiene conto anche di op2
    public static Espressione toExpr(VScan<Token> t)throws ParserException{
        if(t.isEnded())
            throw new FineArrayException();
        Espressione e1=IdentExpr(t);
        while(t.get() instanceof SymbToken){
            SymbToken st=(SymbToken)t.get();
            t.nextEx();
            Espressione e=IdentExpr(t);//Associatività da sinistra a destra
            e1=new Op2Expr(e1, st, e);
        }
        return e1;
    }
    private static Espressione IdentExpr(VScan<Token> t)throws ParserException{
        Espressione ev=toUnaryExpr(t);
        if(!(t.get() instanceof DotToken)){
            if(ev instanceof IdentExpr || ev instanceof NumExpr || ev instanceof TemplExpr){
                return new IdentArray(ev, null, null);
            }
            else
                return ev;
        }
        Stack<IdentEle> se=new Stack<>(IdentEle.class);
        Stack<Boolean> be=new Stack<>(Boolean.class);
        do{
            t.nextEx();
            if(t.get() instanceof DotToken){//doubledot
                be.push(Boolean.TRUE);
                t.nextEx();
            }
            else
                be.push(Boolean.FALSE);
            if(t.get() instanceof IdentToken){
                IdentToken id=(IdentToken)t.get();
                Info.isForbitten(id.getString(), id.getRiga());
                t.nextEx();
                if(t.get() instanceof PareToken && ((PareToken)t.get()).s=='['){
                    t.nextEx();
                    Stack<Espressione> ste=new Stack<>(Espressione.class);
                    ste.push(toExpr(t));
                    while(t.get() instanceof VirgToken){
                        t.nextEx();
                        ste.push(toExpr(t));
                    }
                    if(t.get() instanceof PareToken && ((PareToken)t.get()).s==']'){
                        se.push(new IdentEle(id.getString(), ste.toArray()));
                        t.nextEx();
                    }
                    else throw new ParserException(Lingue.getIstance().format("m_par_sqrbrk"), t);
                }
                else{
                    se.push(new IdentEle(id.getString(), null));
                }
            }
            else throw new ParserException(Lingue.getIstance().format("m_par_invnam"), t);
        }
        while(t.get() instanceof DotToken);
        return new IdentArray(ev, se.toArray(), Info.conversion(be.toArray()));
    }
    //Fà un albero di espressioni del tipo valore o op1 o funz
    private static Espressione toUnaryExpr(VScan<Token> t)throws ParserException{
        if(t.isEnded())
            throw new FineArrayException();//Non si sa mai
        //val
        if(t.get() instanceof IntToken){
            NumExpr e= new NumExpr((IntToken)t.get());
            t.next();
            return e;
        }
        if(t.get() instanceof StrToken){
            StrExpr e= new StrExpr((StrToken)t.get());
            t.next();
            return e;
        }
        if(t.get() instanceof CharToken){
            CharExpr c=new CharExpr((CharToken)t.get());
            t.next();
            return c;
        }
        if(t.get() instanceof HashToken){
            TemplateEle te=Template.detect(t);
            if(te !=null && te instanceof FunzDich){
                return new TemplExpr((FunzDich)te);
            }
            else throw new ParserException(Lingue.getIstance().format("m_par_invtem"), t);
        }
        if(t.get() instanceof RealToken){
            RealExpr re=new RealExpr((RealToken)t.get());
            t.next();
            return re;
        }
        //op1
        if(t.get() instanceof SymbToken){
            SymbToken s=(SymbToken)t.get();
            t.nextEx();
            if(s.getString().equals(":new") || s.getString().equals(":stack")
                    || s.getString().equals(":static")){
                TypeName td=new TypeName(t);
                    if(t.get() instanceof PareToken && ((PareToken)t.get()).s=='('){
                        t.nextEx();
                        if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')'){
                            t.next();
                            switch (s.getString()) {
                                case ":new":
                                    return new NewExpr(td, new Espressione[0]);
                                case ":static":
                                    return new StaticExpr(td, new Espressione[0]);
                                default:
                                    return new StackExpr(td, new Espressione[0]);
                            }
                        }
                        else{
                            Stack<Espressione> stack=new Stack<>(Espressione.class);
                            stack.push(toExpr(t));
                            while(t.get() instanceof VirgToken){
                                t.nextEx();
                                stack.push(toExpr(t));
                            }
                            if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')'){
                                t.next();
                                switch (s.getString()) {
                                    case ":new":
                                        return new NewExpr(td, stack.toArray());
                                    case ":static":
                                        return new StaticExpr(td, stack.toArray());
                                    default:
                                        return new StackExpr(td, stack.toArray());
                                }
                            }
                            else throw new ParserException(Lingue.getIstance().format("m_par_erralc"), t);
                        }
                    }
                    else throw new ParserException(Lingue.getIstance().format("m_par_erralc"), t);
            }
            if((s.getString().equals("+")||s.getString().equals("-"))){
                long j=s.getString().equals("+") ? 1 : -1;
                if(t.get() instanceof IntToken){
                    IntToken i=(IntToken)t.get();
                    NumExpr n=new NumExpr(new IntToken(j*i.s, i.getRiga(), i.val, i.unsigned));
                    t.nextEx();
                    return n;
                }
                if(t.get() instanceof RealToken){
                    RealToken r=(RealToken)t.get();
                    if(j==1)
                        return new RealExpr(r);
                    else
                        return new RealExpr(new RealToken(r.numerEnc | 0x8000000000000000L, 0));
                }
            }
            Espressione i=IdentExpr(t);
            return new Op1Expr(s, i);
        }
        //parens e cast
        if(t.get() instanceof PareToken && ((PareToken)t.get()).s=='('){
            t.nextEx();
            if(t.get() instanceof IdentToken){
                int rety=t.getInd();
                TypeName tn=new TypeName(t);
                if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')'){
                    if(!(t.get(1) instanceof SymbToken || t.get(1) instanceof VirgToken
                            || t.get(1) instanceof DotToken)){
                        //si escludono identificatori semplice tra parentesi
                        //in operazioni o funzioni, e IdentArray
                        t.nextEx();
                        Espressione ex=IdentExpr(t);
                        return new CastExpr(tn, ex);
                    }
                }
                t.setInd(rety);
            }
            Espressione in;
            in=toExpr(t);//le parentesi modificano l'associatività
            if(t.isEnded())
                throw new ParserException(Lingue.getIstance().format("m_par_parncl"), t);//è finito
                        //prima di chiudere la parentesi
            if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')'){
                t.nextEx();
                return in;
            }
            else throw new ParserException(Lingue.getIstance().format("m_par_parncl"), t);
        }
        /*
        funz o IdentExpr
        */
        if(t.get() instanceof IdentToken){
            IdentToken e=(IdentToken)t.get();
            t.nextEx();
            TemplateEle[] pp=Template.detectTemplate(t);
            if(!(t.get() instanceof PareToken) 
                ||((PareToken)t.get()).s!='('){
                if(pp.length>0)
                    throw new ParserException(Lingue.getIstance().format("m_par_postem"), t);
                return new IdentExpr(e);
            }
            t.nextEx();
            if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')'){
                t.nextEx();
                return new FunzExpr(e, pp, new Espressione[0]);
            }
            Stack<Espressione> are=new Stack<>(Espressione.class);
            are.push(toExpr(t));
            while(t.get() instanceof VirgToken){
                t.nextEx();
                Espressione e2=toExpr(t);
                are.push(e2);
            }
            if(t.isEnded()){
                throw new ParserException(Lingue.getIstance().format("m_par_parncl"), t.get(-1));
            }
            if(t.get() instanceof PareToken && ((PareToken)t.get()).s==')'){
                t.nextEx();
                return new FunzExpr(e, pp, are.toArray());
            }
            else throw new ParserException(Lingue.getIstance().format("m_par_parncl"), t);
        }
        throw new ParserException(Lingue.getIstance().format("m_par_uknstr"), t);
    }
}
