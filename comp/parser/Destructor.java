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
import comp.code.FElement;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.general.Lingue;
import comp.general.VScan;
import comp.parser.template.Template;
import comp.parser.template.TemplateEle;
import comp.scanner.IdentToken;
import comp.scanner.Token;

/**
 * I Distruttori non esistono per le classi esplicite
 * @author loara
 */
public class Destructor extends Callable{
    private String classname;
    private TemplateEle[] temParam;
    /**
     * 
     * @param t
     * @param type Nome della classe in cui è dichiarato il costruttore
     * @param params Dichiarazione template della classem madre
     * @param modulo
     * @throws ParserException 
     */
    public Destructor(VScan<Token> t, String type, Template[] params, String modulo)throws ParserException{
        super(t, modulo);
        if(temp.length!=0)
            throw new ParserException(Lingue.getIstance().format("m_par_temdis"), super.nome);
        if(dichs.length!=0)
            throw new ParserException(Lingue.getIstance().format("m_par_pardis"), super.nome);
        temp=params;
        classname=type;
        dichs=new FunzParam[1];
        dichs[0]=new FunzParam(new TypeName(type, Template.conversion(params)), "this");
        temParam=Template.conversion(params);
    }
    public String className(){
        return classname;
    }
    @Override
    public String getName(){
        return ((IdentToken)nome).getString();
    }
    @Override
    public String memName(){
        return "end_"+classname;
    }
    @Override
    public void validate(Environment env, Dichiarazione[] varSt)throws CodeException{
        Template.addTemplateConditions(temp);
        Variabili vs=new Variabili(dichs, varSt, true, null);
        Environment.ret=Types.getIstance().find(retType, true);
        Environment.template=true;
        Environment.errors=errors;
        TemplateEle[] telem=Template.conversion(temp);
        TypeName tne=new TypeName(classname, telem);
        TypeElem te=Types.getIstance().find(tne, true);
        istr.validate(vs, env);
        if(te.extend!=null){
            TypeElem[] parames=new TypeElem[1];
            parames[0]=te;
            Funz.getIstance().request(memName(), parames, true, telem);
        }
        Template.removeTemplateConditions(temp);
    }
    @Override
    public void postCode(Segmenti text, Variabili var, Environment env,
            Accumulator acc)throws CodeException{
        TypeName tne=new TypeName(classname, temParam);//già sostituito
        TypeElem te=Types.getIstance().find(tne, false);
        if(te.extend!=null){
            FElement fe=Funz.getIstance().requestDestructor(te.extend, false);
            if(fe.isExternFile())
                Funz.getIstance().ext.add(fe.modname);
            text.addIstruzione("mov", Register.AX.getReg(), "[rbp+16]");
            text.addIstruzione("push", Register.AX.getReg(), null);
            text.addIstruzione("call", fe.modname, null);
        }
    }
}

