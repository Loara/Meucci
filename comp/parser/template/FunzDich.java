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
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.TNumbers;
import comp.general.Lingue;
import comp.general.VScan;
import comp.parser.ParserException;
import comp.parser.TypeName;
import comp.scanner.Token;
import java.io.Serializable;

/**
 *
 * @author loara
 */
public abstract class FunzDich implements Serializable, TemplateEle{
    public static class SIZEOF extends FunzDich{
        public SIZEOF(TemplateEle[] params){
            super(params);
        }
        @Override
        public int dimension(){
            return 0;//1 byte
        }
        @Override
        public int numParams(){
            return 1;
        }
        @Override
        public void validate()throws CodeException{
            if(this.params[0] instanceof ParamDich){
                Types.getIstance().find(
                        new TypeName(((ParamDich)params[0]).getName()), true);
            }
            else if(params[0] instanceof TypeDich){
                Types.getIstance().find(new TypeName((TypeDich)params[0]), true);
            }
            else throw new CodeException("Parametro errato");
        }
    };
    public static class SUM extends FunzDich{
        private final int d;
        public SUM(TemplateEle[] params, int odim){
            super(params);
            d=odim;
        }
        @Override
        public int dimension()throws CodeException{
            return d;
        }
        @Override
        public int numParams(){
            return -2;
        }
        @Override
        public void validate()throws CodeException{
            int len=params.length;
            if(len<2)throw new CodeException("Numero parametri erroneo");
            for(TemplateEle te:params){
                if(te instanceof ParamDich){
                    if(!TNumbers.getIstance().isIn(((ParamDich)te).getName()))
                        throw new CodeException("Numero non valido");
                }
                else if(te instanceof NumDich){
                    //Ok
                }
                else if(te instanceof FunzDich){
                    ((FunzDich)te).validate();
                }
                else throw new CodeException("Parametro errato");
            }
        }
    };
    public static class PROD extends FunzDich{
        private final int d;
        public PROD(TemplateEle[] params, int odim){
            super(params);
            d=odim;
        }
        @Override
        public int numParams(){
            return -2;
        }
        @Override
        public int dimension()throws CodeException{
            return d;
        }
        @Override
        public void validate()throws CodeException{
            int len=params.length;
            if(len<2)throw new CodeException("Numero parametri erroneo");
            for(TemplateEle te:params){
                if(te instanceof ParamDich){
                    if(!TNumbers.getIstance().isIn(((ParamDich)te).getName()))
                        throw new CodeException("Numero non valido");
                }
                else if(te instanceof NumDich){
                    //Ok
                }
                else if(te instanceof FunzDich){
                    ((FunzDich)te).validate();
                }
                else throw new CodeException("Parametro errato");
            }
        }
    };
    public static class DIMENSION extends FunzDich{
        public DIMENSION(TemplateEle[] params){
            super(params);
        }
        @Override
        public int dimension(){
            return 2;//4 byte
        }
        @Override
        public int numParams(){
            return 1;
        }
        @Override
        public void validate()throws CodeException{
            if(this.params[0] instanceof ParamDich){
                TypeElem tel=Types.getIstance().find(
                        new TypeName(((ParamDich)params[0]).getName()), true);
                if(!tel.reference)
                    throw new CodeException("Parametro non valido");
            }
            else if(params[0] instanceof TypeDich){
                TypeElem tel=Types.getIstance().find(new TypeName((TypeDich)params[0]), true);
                if(!tel.reference)
                    throw new CodeException("Parametro non valido");
            }
            else throw new CodeException("Parametro errato");
        }
    };
    public FunzDich(TemplateEle[] params){
        this.params=params;
    }
    protected TemplateEle[] params;
    //Se è negativo allora il numero di elementi minimo è pari all'opposto
    public abstract int numParams();
    /*
    public void setParams(TemplateEle[] te){
        params=te;
    }
    rischiosa data la nuova policy sui template
*/
    public TemplateEle[] getParams(){
        return params;
    }
    public abstract void validate()throws CodeException;
    public abstract int dimension()throws CodeException;
    public TypeElem retType()throws CodeException{
        int u=dimension();
        String tname;
        switch(u){
            case 0:
                tname="ubyte";
                break;
            case 1:
                tname="ushort";
                break;
            case 2:
                tname="uint";
                break;
            default:
                tname="ulong";
        }
        return Types.getIstance().find(tname);//Non ha bisogno di validate
    }
    public static FunzDich istance(String name, TemplateEle[] pars, VScan<Token> t)throws ParserException{
        FunzDich ret=null;
        switch(name){
            case "SIZEOF":
                ret=new SIZEOF(pars);
                break;
            case "DIMENSION":
                ret=new DIMENSION(pars);
                break;
        }
        if(ret==null){
            if(name.startsWith("SUM")){
                ret=new SUM(pars, Integer.parseInt(name.substring(3, 4)));
            }
            else if(name.startsWith("PROD")){
                ret=new PROD(pars, Integer.parseInt(name.substring(4, 5)));
            }
            else throw new ParserException(Lingue.getIstance().format("m_par_ftensp", name), t);
        }
        if(ret.numParams()>0){
            if(pars.length!=ret.numParams())
                throw new ParserException(Lingue.getIstance().format("m_par_ftenpe"), t);
        }
        else{
            if(pars.length<(-ret.numParams()))
                throw new ParserException(Lingue.getIstance().format("m_par_ftenpe"), t);
        }
        return ret;
    }
}
