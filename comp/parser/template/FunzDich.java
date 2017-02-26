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
import comp.general.Info;
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
        public boolean hasUp(){
            return true;
        }
        @Override
        public boolean hasLow(){
            return true;
        }
        @Override
        public long upBound(){
            return 9;//maggiore stretto
        }
        @Override
        public long lowBound(){
            return 0;
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
        public SUM(TemplateEle[] params){
            super(params);
        }
        @Override
        public int dimension()throws CodeException{
            int maxy=0;
            int lev;
            for(TemplateEle te:params){
                lev=0;
                if(te instanceof NumDich){
                    lev=((NumDich)te).expDim();
                }
                else if(te instanceof ParamDich){
                    lev=TNumbers.getIstance().dimension(((ParamDich)te).getName());
                }
                else if(te instanceof FunzDich){
                    lev=((FunzDich)te).dimension();
                }
                else throw new CodeException("Parametro erroneo");
                maxy=maxy<lev ? lev : maxy;
            }
            return maxy;
        }
        @Override
        public int numParams(){
            return -2;
        }
        @Override
        public boolean hasLow()throws CodeException{
            for(TemplateEle te:params){
                if(te instanceof NumDich){
                    return true;
                }
                else if(te instanceof ParamDich){
                    if(!TNumbers.getIstance().find(((ParamDich)te).getName()).hasMin())
                        return true;
                }
                else if(te instanceof FunzDich){
                    if(!((FunzDich)te).hasUp())
                        return true;
                }
                else throw new CodeException("Parametro erroneo");
            }
            return false;
        }
        @Override
        public boolean hasUp()throws CodeException{
            for(TemplateEle te:params){
                if(te instanceof NumDich){
                    //Continua
                }
                else if(te instanceof ParamDich){
                    if(!TNumbers.getIstance().find(((ParamDich)te).getName()).hasMax())
                        return false;
                }
                else if(te instanceof FunzDich){
                    if(!((FunzDich)te).hasUp())
                        return false;
                }
                else throw new CodeException("Parametro erroneo");
            }
            return true;
        }
        @Override
        public long upBound()throws CodeException{
            long inibound=0;
            for(TemplateEle te:params){
                if(te instanceof NumDich){
                    inibound+=((NumDich)te).getNum();
                }
                else if(te instanceof ParamDich){
                    NumTemplate tn=TNumbers.getIstance().find(((ParamDich)te).getName());
                    if(tn.hasMax())
                        inibound+=tn.getMax()-1;
                }
                else if(te instanceof FunzDich){
                    inibound+=((FunzDich)te).upBound()-1;
                }
                else throw new CodeException("Parametro erroneo");
            }
            return inibound+1;//maggiore stretto
        }
        @Override
        public long lowBound()throws CodeException{
            long inibound=0;
            for(TemplateEle te:params){
                if(te instanceof NumDich){
                    inibound+=((NumDich)te).getNum();
                }
                else if(te instanceof ParamDich){
                    NumTemplate tn=TNumbers.getIstance().find(((ParamDich)te).getName());
                    if(tn.hasMin())
                        inibound+=tn.getMin()+1;
                }
                else if(te instanceof FunzDich){
                    inibound+=((FunzDich)te).lowBound()+1;
                }
                else throw new CodeException("Parametro erroneo");
            }
            return inibound-1;//maggiore stretto
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
        public PROD(TemplateEle[] params){
            super(params);
        }
        @Override
        public int numParams(){
            return -2;
        }
        @Override
        public int dimension()throws CodeException{
            int maxy=0;
            int lev;
            for(TemplateEle te:params){
                lev=0;
                if(te instanceof NumDich){
                    lev=((NumDich)te).expDim();
                }
                else if(te instanceof ParamDich){
                    lev=TNumbers.getIstance().dimension(((ParamDich)te).getName());
                }
                else if(te instanceof FunzDich){
                    lev=((FunzDich)te).dimension();
                }
                else throw new CodeException("Parametro erroneo");
                maxy=maxy<lev ? lev : maxy;
            }
            return maxy;
        }
        @Override
        public boolean hasLow()throws CodeException{
            for(TemplateEle te:params){
                if(te instanceof NumDich){
                    return true;
                }
                else if(te instanceof ParamDich){
                    if(TNumbers.getIstance().find(((ParamDich)te).getName()).hasMin())
                        return true;
                }
                else if(te instanceof FunzDich){
                    if(((FunzDich)te).hasLow())
                        return true;
                }
                else throw new CodeException("Parametro erroneo");
            }
            return false;
        }
        @Override
        public boolean hasUp()throws CodeException{
            for(TemplateEle te:params){
                if(te instanceof NumDich){
                    //Continua
                }
                else if(te instanceof ParamDich){
                    if(!TNumbers.getIstance().find(((ParamDich)te).getName()).hasMax())
                        return false;
                }
                else if(te instanceof FunzDich){
                    if(!((FunzDich)te).hasUp())
                        return false;
                }
                else throw new CodeException("Parametro erroneo");
            }
            return true;
        }
        @Override
        public long upBound()throws CodeException{
            long inibound=1;
            for(TemplateEle te:params){
                if(te instanceof NumDich){
                    inibound*=((NumDich)te).getNum();
                }
                else if(te instanceof ParamDich){
                    NumTemplate tn=TNumbers.getIstance().find(((ParamDich)te).getName());
                    if(tn.hasMax())
                        inibound*=tn.getMax()-1;
                }
                else if(te instanceof FunzDich){
                    inibound*=((FunzDich)te).upBound()-1;
                }
                else throw new CodeException("Parametro erroneo");
            }
            return inibound+1;//maggiore stretto
        }
        @Override
        public long lowBound()throws CodeException{
            long inibound=1;
            for(TemplateEle te:params){
                if(te instanceof NumDich){
                    inibound*=((NumDich)te).getNum();
                }
                else if(te instanceof ParamDich){
                    NumTemplate tn=TNumbers.getIstance().find(((ParamDich)te).getName());
                    if(tn.hasMin())
                        inibound*=tn.getMin()+1;
                }
                else if(te instanceof FunzDich){
                    inibound*=((FunzDich)te).lowBound()+1;
                }
                else throw new CodeException("Parametro erroneo");
            }
            return inibound-1;//maggiore stretto
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
        public boolean hasUp(){
            return true;
        }
        @Override
        public boolean hasLow(){
            return true;
        }
        @Override
        public long upBound(){
            return 0x100000000l;
        }
        @Override
        public long lowBound(){
            return 7;//maggiore di 8
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
    public void setParams(TemplateEle[] te){
        params=te;
    }
    public TemplateEle[] getParams(){
        return params;
    }
    public abstract void validate()throws CodeException;
    public abstract boolean hasUp()throws CodeException;
    public abstract boolean hasLow()throws CodeException;
    public abstract long upBound()throws CodeException;
    public abstract long lowBound()throws CodeException;
    public abstract int dimension()throws CodeException;
    public static FunzDich istance(String name, TemplateEle[] pars, VScan<Token> t)throws ParserException{
        FunzDich ret;
        switch(name){
            case "SIZEOF":
                ret=new SIZEOF(pars);
                break;
            case "SUM":
                ret=new SUM(pars);
                break;
            case "PROD":
                ret=new PROD(pars);
                break;
            case "DIMENSION":
                ret=new DIMENSION(pars);
                break;
            default:
                throw new ParserException("Funzione template non supportata dal compilatore", t);
        }
        if(ret.numParams()>0){
            if(pars.length!=ret.numParams())
                throw new ParserException("Numero parametri errati in funzione template", t);
        }
        else{
            if(pars.length<(-ret.numParams()))
                throw new ParserException("Numero parametri errati in funzione template", t);
        }
        return ret;
    }
}
