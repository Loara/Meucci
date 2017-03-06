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
package comp.parser;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Funz;
import comp.code.Funz.FElement;
import comp.code.Meth;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.Substitutor;
import comp.code.vars.Variabili;
import comp.general.VScan;
import comp.parser.expr.FunzExpr;
import comp.parser.expr.IdentArray;
import comp.parser.istruz.ClassisIstr;
import comp.parser.template.Template;
import comp.parser.template.TemplateEle;
import comp.scanner.IdentToken;
import comp.scanner.Token;

/**
 * Costruttore, và memorizzato come FunzEle
 * @author loara
 */
public class Costructor extends Callable{
    private TypeName classname;
    private FunzExpr costruct;
    /**
     * 
     * @param t
     * @param type Nome della classe in cui è dichiarato il costruttore
     * @param params Dichiarazione template della classem madre
     * @param modulo
     * @throws ParserException 
     */
    public Costructor(VScan<Token> t, String type, Template[] params, String modulo)throws ParserException{
        super(t, modulo);
        if(temp.length!=0)
            throw new ParserException("Un costruttore non può avere parametri template", super.nome);
        temp=params;
        if(!(nome instanceof IdentToken) || !((IdentToken)nome).getString().equals("init"))
            throw new ParserException("Non è un costruttore",t);
        nome=new IdentToken(Meth.costructorName(type), nome.getRiga());
        classname=new TypeName(type, Template.conversion(params));
        FunzParam[] dd=new FunzParam[dichs.length+1];
        dd[0]=new FunzParam(new TypeName(type, Template.conversion(params)), "this");
        System.arraycopy(dichs, 0, dd, 1, dichs.length);
        dichs=dd;
        if(istr.m.length==0){
            costruct=null;
        }
        else if(istr.m[0] instanceof ClassisIstr && ((ClassisIstr)istr.m[0]).isExpression()){
            Espressione ei=((ClassisIstr)istr.m[0]).getExpr();
            if(ei instanceof FunzExpr && ((FunzExpr)ei).getName().equals("super")){
                costruct=(FunzExpr)ei;
                istr.m[0]=null;//già contemplata
                if(costruct.template().length!=0)
                    throw new ParserException("Impossibile specificare template per super", t);
            }
            else
                costruct=null;
        }
        else
            costruct=null;
    }
    public String className(){
        return classname.getName();
    }
    @Override
    public String getName(){
        return ((IdentToken)nome).getString();
    }
    @Override
    public void validate(Environment env, Dichiarazione[] varSt)throws CodeException{
        Template.addTemplateConditions(temp);
        Variabili vs=new Variabili(dichs, varSt, true, null);
        Environment.ret=Types.getIstance().find(retType, true);
        TypeElem te=Types.getIstance().find(classname, true);
        if(te.extend==null){
            if(costruct!=null){
                throw new CodeException("Chiamata erronea di costruttore inesistente");
            }
        }
        else{
            if(costruct==null){
                throw new CodeException("Non è specificata alcuna chiamata al costruttore del sovrattipo");
            }
            TypeElem[] parames;
            Espressione[] expre=costruct.getValues();
            parames=new TypeElem[expre.length+1];
            parames[0]=te;
            for(int i=0; i<expre.length; i++){
                parames[i+1]=expre[i].returnType(vs, true);
            }
            Funz.getIstance().request(Meth.costructorName(te.extend), parames, 
                    true, classname.templates());
        }
        istr.validate(vs, env);
        Template.removeTemplateConditions(temp);
    }
    @Override
    public void preCode(Segmenti text, Variabili var, Environment env,
            Accumulator acc)throws CodeException{
        //TemplateEle[] telemy=Template.conversion(temp); Questa linea è erronea, in quanto
        //il campo temp non può ovviamente essere sostituito
        TypeElem te=Types.getIstance().find(classname, true);
        if(te.extend==null){
            if(costruct!=null)
                throw new CodeException("Chiamata erronea di costruttore inesistente");
        }
        else{
            if(costruct==null){
                throw new CodeException("Non è specificata alcuna chiamata al costruttore del sovrattipo");
            }
            TypeElem[] parames;
            Espressione[] expre=costruct.getValues();
            parames=new TypeElem[expre.length+1];
            Espressione[] calling=new Espressione[expre.length+1];
            parames[0]=te;
            calling[0]=new IdentArray("this");
            for(int i=0; i<expre.length; i++){
                parames[i+1]=expre[i].returnType(var, false);
                calling[i+1]=expre[i];
            }
            FElement fel=Funz.getIstance().request(Meth.costructorName(te.extend.getName()), 
                    parames, false, te.extend.templates());
            if(fel.isExternFile())
                Funz.getIstance().ext.add(fel.modname);
            FunzExpr.perfCall(fel.modname, Types.getIstance().find("void")
                    , calling, text, var, env, acc);
        }
    }
}

