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
package comp.parser;

import comp.general.Lingue;
import comp.general.VScan;
import comp.parser.template.ParamDich;
import comp.parser.template.Template;
import comp.parser.template.TypeDich;
import comp.scanner.IdentToken;
import comp.scanner.Token;

/**
 * Come FunzMem, ma per membri GPacked
 * Formato:
 * prima lettera: G - get S - set
 * nome variabile
 * @author loara
 */
public class GFunzMem extends Callable{
    private boolean getAcc;
    private String varName, classname;
    //private final FunzParam[] decValues;
    /**
     * A differenza di FunzMem, qui i parametri sono gli stessi, senza tipi di ritorno
     * 
     * type è il tipo del parametro a cui si accede
     * ctype è il tipo della classe che contiene il parametro -> classname
     * name è il nome del parametro -> varName
     * @param t
     * @param type
     * @param ctype
     * @param ctemplate
     * @param name
     * @param modulo
     * @throws ParserException 
     */
    public GFunzMem(VScan<Token> t, TypeName type, String ctype, 
            Template[] ctemplate, String name, String modulo)throws ParserException{
        super(t, modulo, false);
        noglobal=true;
        int riga=super.nome.getRiga();
        if(super.temp.length!=0)
            throw new ParserException(Lingue.getIstance().format("m_par_temacc"), riga);
        if(shadow)
            throw new ParserException(Lingue.getIstance().format("m_par_shderr"), riga);
        super.temp=ctemplate;
        if(nome instanceof IdentToken){
            String n=((IdentToken)nome).getString();
            switch (n) {
                case "get":
                    getAcc=true;
                    break;
                case "set":
                    getAcc=false;
                    break;
                default:
                    throw new ParserException(Lingue.getIstance().format("m_par_unkacc", n), riga);
            }
            varName=name;
        }
        else throw new ParserException(Lingue.getIstance().format("m_par_invnam"),riga);
        
        classname=ctype;
        FunzParam[] d2=new FunzParam[dichs.length+1];
        d2[0]=new FunzParam(new TypeName(ctype, Template.conversion(temp)), "this");
        System.arraycopy(dichs, 0, d2, 1, dichs.length);
        dichs=d2;
        /*
        if(dichs.length != 2)
            errore
        */
        
        TypeName arrayName;
        if(type.templates().length==0){
            arrayName=new TypeName("Array", new ParamDich(type.getName()));
        }
        else{
            arrayName=new TypeName("Array", new TypeDich(type.getName(), type.templates()));
        }
        
        if(!retType.equals(new TypeName("void")))
                throw new ParserException(Lingue.getIstance().format("m_par_nonret", nome, "void")
                        , riga);
        if(!dichs[1].dich.type.equals(arrayName))
            throw new ParserException(Lingue.getIstance().
                    format("m_par_notfnd", arrayName.getName(), nome),t);
    }
    protected GFunzMem(TypeName type, String ctype, Template[] ctemplates, String name, 
            String modulo, boolean acc){
        super(modulo);
        istr=null;
        temp=ctemplates;
        noglobal=true;
        getAcc=acc;
        classname=ctype;
        varName=name;
        if(acc){
            retType=type;
            dichs=new FunzParam[]{new FunzParam(new TypeName(ctype, Template.
                    conversion(ctemplates)), "this")};
        }
        else{
            retType=new TypeName("void");
            dichs=new FunzParam[]{new FunzParam(new TypeName(ctype, Template.
                    conversion(ctemplates)), "this"),new FunzParam(type, "aaa")};
        }
    }
    @Override
    public String getName(){
        return ((IdentToken)nome).getString();
    }
    @Override
    public String memName(){
        return (getAcc ? "G" : "S")+varName+"_"+classname;
    }
    public String varName(){
        return varName;
    }
    
    public boolean getAccess(){
        return getAcc;
    }
    /**
     *La classe in cui è dichiarato 
     * @return 
     */
    public String className(){
        return classname;
    }
}
