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
package comp.code.vars;

import comp.code.CodeException;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.XReg;
import comp.general.Lingue;
import comp.parser.Dichiarazione;
import comp.parser.TypeName;
import java.util.ArrayList;

/**
 * Variabili globali
 * @author loara
 */
public class VarStatic extends Var{
    public static class StaticEle{
        public final String name;
        public final TypeElem type;
        public StaticEle(String n, TypeName t)throws CodeException{
            name=n;
            type=Types.getIstance().find(t, false);//Non ci possono essere template
        }
        public String writtenName(){
            return "V"+name;
        }
    }
    private ArrayList<StaticEle> vars;
    public VarStatic(){
        vars=new ArrayList<>();
    }
    public VarStatic(Dichiarazione[] dichs)throws CodeException{
        this();
        for(Dichiarazione dich:dichs){
            vars.add(new StaticEle(dich.getIdent(), dich.getRType()));
        }
    }
    public void addVar(Dichiarazione dich)throws CodeException{
        vars.add(new StaticEle(dich.getIdent(), dich.getRType()));
    }
    /**
     * Lancia un'eccezione se exc=true, altrimenti ritorna null;
     * @param ident
     * @return
     * @throws CodeException 
     */
    @Override
    public TypeElem type(String ident)throws CodeException{
        StaticEle f=null;
        for(StaticEle v:vars){
            if(v.name.equals(ident)){
                f=v;
                break;
            }
        }
        if(f==null) throw new CodeException(Lingue.getIstance().format("m_cod_uknvarb", ident));
        return f.type;
    }
    public StaticEle get(String ident, boolean exc)throws CodeException{
        StaticEle f=null;
        for(StaticEle v:vars){
            if(v.name.equals(ident)){
                f=v;
                break;
            }
        }
        if(exc&&f==null)
            throw new CodeException(Lingue.getIstance().format("m_cod_uknvarb", ident));
        return f;
    }
    @Override
    public boolean isIn(String ie){
        return vars.stream().anyMatch((v) -> (v.name.equals(ie)));
    }
    @Override
    public void getVar(Segmenti text, String ident, Register reg)throws CodeException{
        StaticEle ve=get(ident, true);
        String u="["+ve.writtenName()+"]";
        getGVar(text, u, reg, ve.type.realDim());
    }
    @Override
    public void setVar(Segmenti text, String ident, Register reg)throws CodeException{
        StaticEle ve=get(ident, true);
        String u="["+ve.writtenName()+"]";
        setGVar(text, u, reg, ve.type.realDim());
    }
    @Override
    public void xgetVar(Segmenti text, String ident, XReg reg)throws CodeException{
        StaticEle ve=get(ident, true);
        String u="["+ve.writtenName()+"]";
        getXVar(text, u, reg);
    }
    @Override
    public void xsetVar(Segmenti text, String ident, XReg reg)throws CodeException{
        StaticEle ve=get(ident, true);
        String u="["+ve.writtenName()+"]";
        setXVar(text, u, reg);
    }
}
