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

package comp.parser.template;

import comp.code.CodeException;
import comp.code.Types;
import comp.code.template.TNumbers;
import comp.general.Info;
import comp.general.Stack;
import comp.general.VScan;
import comp.parser.ParserException;
import comp.parser.TypeName;
import comp.scanner.HashToken;
import comp.scanner.IdentToken;
import comp.scanner.IntToken;
import comp.scanner.PareToken;
import comp.scanner.SymbToken;
import comp.scanner.Token;
import comp.scanner.VirgToken;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author loara
 */
public abstract class Template implements Serializable{
    private final String ident;
    public Template(String i){
        ident=i;
    }
    public String getIdent(){
        return ident;
    }
    /**
     * Dice se il parametro è compatibile
     * @param te
     * @return 
     * @throws comp.code.CodeException 
     */
    public abstract boolean isCompatible(TemplateEle te)throws CodeException;
    /**
     * Se non ci sono template ritorna array di lunghezza 0
     * @param t
     * @return 
     * @throws comp.parser.ParserException 
     */
    public static Template[] parseTemp(VScan<Token> t)throws ParserException{
        if(t.get() instanceof PareToken
                && ((PareToken)t.get()).s=='['){
            t.nextEx();
            ArrayList<Template> tt=new ArrayList<>();
            if(t.get() instanceof IdentToken){
                String typ=((IdentToken)t.get()).getString();
                t.nextEx();
                if(!(t.get() instanceof IdentToken))
                    throw new ParserException("Nome template non valido", t);
                String name=((IdentToken)t.get()).getString();
                Info.isForbitten(name, t.get().getRiga());
                t.nextEx();
                switch(typ){
                    case "num":
                        tt.add(parseNum(t, name));
                        break;
                    case "typ":
                        tt.add(parseTyp(t, name));
                        break;
                    default:
                        throw new ParserException("Categoria template non valida ("+typ+")\n"
                                + "Sono valide le seguenti: num typ", t);
                }
            }
            else
                throw new ParserException("Template erroneo", t);
            if(chiusaPare(t))
                return Info.toAr(Template.class, tt);
            do{
                if(!(t.get() instanceof VirgToken))
                    throw new ParserException("Manca ,", t);
                t.nextEx();
                if(t.get() instanceof IdentToken){
                    String typ=((IdentToken)t.get()).getString();
                    t.nextEx();
                    if(!(t.get() instanceof IdentToken))
                        throw new ParserException("Nome template non valido", t);
                    String name=((IdentToken)t.get()).getString();
                    t.nextEx();
                    switch(typ){
                        case "num":
                            tt.add(parseNum(t, name));
                            break;
                        case "typ":
                            tt.add(parseTyp(t, name));
                            break;
                        default:
                            throw new ParserException("Categoria template non valida", t);
                    }
                }
                else
                    throw new ParserException("Template erroneo", t);
            }
            while(!chiusaPare(t));
            return Info.toAr(Template.class, tt);
        }
        else return new Template[0];
    }
    private static boolean chiusaPare(VScan<Token> t)throws ParserException{
        if(t.get() instanceof PareToken
                && ((PareToken)t.get()).s==']'){
            t.nextEx();
            return true;
        }
        return false;
    }
    private static NumTemplate parseNum(VScan<Token> t, String name)throws ParserException{
        long inf=0, sup=0;
        int dim;
        boolean bi=false, bs=false;
        if(t.get() instanceof IntToken){
            dim=(int)((IntToken)t.get()).s;
            if(dim<0 || dim>Info.maxDimExp)
                throw new ParserException("Dimensione parametro template troppo alta", t);
            t.nextEx();
        }
        else throw new ParserException("Manca dimensione di "+name, t);
        if(t.get() instanceof SymbToken && ((SymbToken)t.get()).getString().equals(">")){
            t.nextEx();
            if(t.get() instanceof IntToken){
                bi=true;
                inf=((IntToken)t.get()).s;
            }
            else throw new ParserException("Condizione template erronea", t);
            t.nextEx();
        }
        if(t.get() instanceof SymbToken && ((SymbToken)t.get()).getString().equals("<")){
            t.nextEx();
            if(t.get() instanceof IntToken){
                bs=true;
                if(dim<Info.maxDimExp){
                    long max= 1l << 8*(1l << dim) - 1;
                    long vay=((IntToken)t.get()).s;
                    sup= max<vay ? max : vay;
                }
                else
                    sup=((IntToken)t.get()).s;
            }
            else throw new ParserException("Condizione template erronea", t);
            t.nextEx();
        }
        else if(dim<Info.maxDimExp){
            bs=true;
            sup = 1l << 8*(1l << dim) - 1;
        }
        return new NumTemplate(name, dim, inf, sup, bi, bs);
    }
    private static TypTemplate parseTyp(VScan<Token> t, String name)throws ParserException{
        boolean ref=false, num=false;
        TypeName ext=null;
        if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("reference")){
            t.nextEx();
            ref=true;
        }
        if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("number")){
            t.nextEx();
            num=true;
            if(ref)
                throw new ParserException("Impossibile utilizzare contemporaneamente i seguenti parametri:"
                +"\n number e reference", t);
        }
        if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("extends")){
            t.nextEx();
            if(!ref)
                ref=true;
            if(num)
                throw new ParserException("Impossibile utilizzare contemporaneamente i seguenti parametri:"
                +"\n number e extends", t);
            if(!(t.get() instanceof IdentToken))
                throw new ParserException("Tipo non valido", t);
            ext=new TypeName(t);
        }
        return new TypTemplate(name, ref, num, ext);
    }
    public static TemplateEle detect(VScan<Token> s)throws ParserException{
                if(s.get() instanceof IdentToken){
                    String u=((IdentToken)s.get()).getString();
                    s.nextEx();
                    TemplateEle[] k=detectTemplate(s);
                    if(k.length==0)
                        return new ParamDich(u);//Non viene convertito automaticamente
                    //in TypeDich in quanto può essere template di un numero
                    else
                        return new TypeDich(u, k);
                }
                else if(s.get() instanceof IntToken){
                    IntToken itd=(IntToken)s.get();
                    int exdim;
                    switch(itd.val){
                        case 'b':
                        case 'B':
                            exdim=0;
                            break;
                        case 's':
                        case 'S':
                            exdim=1;
                            break;
                        case 'l':
                        case 'L':
                            exdim=3;
                            break;
                        default:
                            exdim=2;
                    }
                    NumDich nd= new NumDich(itd.s, exdim);
                    s.nextEx();
                    return nd;
                }
                else if(s.get() instanceof HashToken){
                    s.nextEx();
                    if(!(s.get() instanceof IdentToken))
                        throw new ParserException("Funzione template non valida", s);
                    String fname=((IdentToken)s.get()).getString();
                    s.nextEx();
                    if(!(s.get() instanceof PareToken))
                        throw new ParserException("Funzione template non valida", s);
                    else{
                        if(((PareToken)s.get()).s!='(')
                            throw new ParserException("Funzione template non valida", s);
                        s.nextEx();
                        if(s.get() instanceof PareToken && ((PareToken)s.get()).s==')'){
                            s.nextEx();
                            return FunzDich.istance(fname, new TemplateEle[0], s);
                        }
                        Stack<TemplateEle> ss=new Stack<>(TemplateEle.class);
                        ss.push(detect(s));
                        while(!(s.get() instanceof PareToken) ||
                                ((PareToken)s.get()).s!=')'){
                            if(!(s.get() instanceof VirgToken))
                                throw new ParserException("Funzione template non valida", s);
                            s.nextEx();
                            ss.push(detect(s));
                        }
                        s.nextEx();
                        return FunzDich.istance(fname, ss.toArray(), s);
                    }
                }
                else throw new ParserException("Parametro non valido", s);
    }
    public static TemplateEle[] detectTemplate(VScan<Token> s)throws ParserException{
            if(Info.isTemplatePare(s, true)){
                Stack<TemplateEle> ss=new Stack<>(TemplateEle.class);
                s.nextEx();
                ss.push(detect(s));
                if(!Info.isTemplatePare(s, false)){
                    do{
                        if(!(s.get() instanceof VirgToken))
                            throw new ParserException("Manca ,", s);
                        s.nextEx();
                        ss.push(detect(s));
                    }
                    while(!Info.isTemplatePare(s, false));
                    s.nextEx();
                }
                else{
                    s.nextEx();
                }
                return ss.toArray();
            }
            return new TemplateEle[0];
    }
    public static TemplateEle[] conversion(Template[] params){
        TemplateEle[] p=new TemplateEle[params.length];
        for(int i=0; i<p.length; i++){
            p[i]=new ParamDich(params[i].getIdent());
        }
        return p;
    }
    public static void addTemplateConditions(Template[] temp)throws CodeException{
        for(Template t:temp){
            if(t instanceof TypTemplate){
                Types.getIstance().loadTemplate((TypTemplate)t);
            }
            else{
                TNumbers.getIstance().add((NumTemplate)t);
            }
        }
    }
    public static void removeTemplateConditions(Template[] temp){
        Types.getIstance().removeAllTemplate();
        for(Template t:temp){
            if(t instanceof TypTemplate){
            }
            else{
                TNumbers.getIstance().remove((NumTemplate)t);
            }
        }
    }
}