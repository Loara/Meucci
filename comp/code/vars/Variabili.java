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

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.vars.VarStack.VarEle;
import comp.general.Lingue;
import comp.parser.Dichiarazione;
import comp.parser.FunzParam;
import comp.parser.expr.IdentArray;
import comp.parser.expr.IdentEle;
import comp.parser.expr.IdentExpr;

/**
 * Gestisce tutte le variabili, sia nello stack sia quelle esterne. Da ottimizzare
 * @author loara
 */
public class Variabili {
    private final VarStack vars;
    private final Var[] varSt;
    private final GhostVar gvar;
    private String costrVarName;
    public Variabili(FunzParam[] dichs, Dichiarazione[] stat, boolean validate, 
            Accumulator acc)throws CodeException{
        varSt=new Var[2];
        varSt[1]=new VarStatic(stat);
        gvar=new GhostVar();
        if(validate){
            vars=new VarStack(new FunzParam[0], acc);//Inutilizzato
            for(FunzParam fp:dichs)
                gvar.addVar(fp.dich.getRType(), fp.getIdent());
        }
        else
            vars=new VarStack(dichs, acc);
        varSt[0]=vars;
        costrVarName=null;
    }
    public void setCostrVarName(String vn){
        costrVarName=vn;
    }
    public String getCostrVarName(){
        return costrVarName;
    }
    public IdentArray getCostrAsExpr()throws CodeException{
        if(costrVarName==null)
            throw new CodeException("k---");
        return new IdentArray(costrVarName);
    }
    public void addVarStack(Dichiarazione var)throws CodeException{
        vars.addVar(var.getRType(), var.getIdent());
    }
    public void addGhostVar(Dichiarazione var)throws CodeException{
        gvar.addVar(var.getRType(), var.getIdent());
    }
    public VarEle allocStack(int i, boolean des)throws CodeException{
        return vars.allocAlign(i, des);
    }
    /*
    Usato solo in validate
    */
    public void testIsIn(String name)throws CodeException{
        if(gvar.isIn(name))
            return;
        for(Var v:varSt){
            if(v.isIn(name))
                return;
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_uknvarb", name));
    }
    /**
     * Se true cerca prima in GhostVar
     * @param ident
     * @return
     * @throws CodeException 
     */
    public TypeElem getType(String ident)throws CodeException{
        if(gvar.isIn(ident))
            return gvar.getType(ident);
        for(Var v:varSt){
            if(v.isIn(ident))
                return v.type(ident);
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_uknvarb", ident));
    }
    public void getVar(Segmenti text, Accumulator acc, String ident)throws CodeException{
        for(Var v:varSt){
            if(v.isIn(ident)){
                if(v.type(ident).xmmReg())
                    v.xgetVar(text, ident, acc.getXAccReg());
                else
                    v.getVar(text, ident, acc.getAccReg());
                return;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_uknvarb", ident));
    }
    /**
     * mette ax o xmm0 nella variabile
     * @param text
     * @param acc
     * @param ident
     * @throws comp.code.CodeException 
     */
    public void setVar(Segmenti text, Accumulator acc, String ident)throws CodeException{
        for(Var v:varSt){
            if(v.isIn(ident)){
                if(v.type(ident).xmmReg())
                    v.xsetVar(text, ident, acc.getXAccReg());
                else
                    v.setVar(text, ident, acc.getAccReg());
                return;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_uknvarb", ident));
    }
    public VarStack getVarStack(){
        return vars;
    }
    public GhostVar getGhostVar(){
        return gvar;
    }
}
