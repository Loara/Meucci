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
import comp.code.Segmenti;
import comp.code.Types;
import comp.code.vars.Variabili;
import comp.general.VScan;
import comp.parser.template.Template;
import comp.scanner.IdentToken;
import comp.scanner.Token;

/**
 * Costruttore, dalla versione 3 và memorizzata all'esterno del tipo, 
 * si usa la parola chiave "costructor" PRIMA di shadow e senza il tipo di ritorno,
 * seguito dal nome del costruttore e dai 
 * parametri template. Il primo parametro 
 * del costruttore (e non più il nome) determinano l'oggetto creato.
 * 
 * È sempre possibile settare come nome del costruttore il nome dell'oggetto da creare
 * e sfruttare l'overloading, però quando si utilizzano parametri template si consiglia
 * di cambiare nome.
 * 
 * Come FElement al nome si deve aggiungere il prefisso "_init_"
 * @author loara
 */
public class Costructor extends Callable{
    private final String fclass;
    /**
     * 
     * @param t
     * @param modulo
     * @throws ParserException 
     */
    public Costructor(VScan<Token> t, String modulo)throws ParserException{
        super(t, modulo, true);
        fclass=super.dichs[0].getType();
    }
    public String className(){
        return fclass;
    }
    @Override
    public String getName(){
        return ((IdentToken)nome).getString();
    }
    @Override
    public void validate(Environment env, Dichiarazione[] varSt)throws CodeException{
        Template.addTemplateConditions(temp);
        Variabili vs=new Variabili(dichs, varSt, true, null);
        vs.setCostrVarName(dichs[0].getIdent());
        Environment.ret=Types.getIstance().find(retType, true);
        Environment.template=true;
        Environment.errors=errors;
        istr.validate(vs, env);
        Template.removeTemplateConditions(temp);
    }
    @Override
    protected void preCode(Segmenti text, Variabili var, Environment env, 
            Accumulator acc)throws CodeException{
        var.setCostrVarName(dichs[0].getIdent());
    }
    @Override
    public String memName(){
        return "init_"+getName();
    }
    public static String costrName(String fname){
        return "init_"+fname;
    }
}

