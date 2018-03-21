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
package comp.parser.expr;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.TNumbers;
import comp.code.vars.Variabili;
import comp.general.Lingue;
import comp.parser.Espressione;
import comp.parser.Membro;
import comp.parser.ParserException;
import comp.parser.template.ParamDich;

/**
 * Contiene anche i NumExpr
 * @author loara
 */
public class IdentArray extends Espressione{
    private Espressione chiam;
    private final IdentEle[] elems;
    public IdentArray(Espressione ex, IdentEle[] ie)throws ParserException{
        if(ie==null || ie.length==0){
            elems=new IdentEle[0];
            if(ex instanceof IdentExpr || ex instanceof NumExpr || ex instanceof TemplExpr)
                chiam=ex;
            //else throw new ParserException("Non è un IdentExpr o un numero", 0);
            //Gestito da ExprGen
        }
        else{
            elems=ie;
            chiam=ex;
        }
    }
    public IdentArray(String it){
        chiam=new IdentExpr(it);
        elems=new IdentEle[0];
    }
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        TypeElem tp=chiam.returnType(var, v);
        for(IdentEle ee: elems){
            Membro m = tp.information(ee.getIdent(), v);
            tp=Types.getIstance().find(m.getType(), v);
        }
        return tp;
    }
    public TypeElem partialReturnType(Variabili var, int deep, boolean v)throws CodeException{
        if(deep >elems.length)
            throw new CodeException("Internal error, please report it");
        //deep == length equiv to classic returnType
        TypeElem tp=chiam.returnType(var, v);
        for(int i=0; i<deep; i++){
            Membro m = tp.information(elems[i].getIdent(), v);
            tp=Types.getIstance().find(m.getType(), v);
        }
        return tp;
    }
    @Override
    public void validate(Variabili vars)throws CodeException{
        chiam.validate(vars);
        TypeElem ty=chiam.returnType(vars, true);
        for (int i = 0; i< elems.length; i++) {
            Membro m = ty.information(elems[i].getIdent(), true);
            for (Espressione val : elems[i].getVals()) {
                val.validate(vars);
            }
            if (m.compatible(elems[i], vars, true)!=0) {
                throw new CodeException("Parametri di accesso al parametro " + elems[i].getIdent() + " non validi");
            }
            ty.canRead(elems[i].getIdent(), true);
            ty = Types.getIstance().find(ty.information(elems[i].getIdent(), true).getType(), true);
        }
    }
    @Override
    public void toCode(Segmenti text, Variabili vars, Environment env, Accumulator acc)
    throws CodeException{
        chiam.toCode(text, vars, env, acc);
        if(elems.length==0)
            return;
        TypeElem tp=chiam.returnType(vars, false);
        for(int ii=0; ii<elems.length; ii++)
            tp=tp.getTypeElement(text, vars, env, elems[ii], acc);
    }
    /**
     * Da non confondere con returnType. Da utilizzare solamente in congiunzione di numValue
     * @return 
     */
    public boolean isNum(){
        if(elems.length!=0)
            return false;
        if(chiam instanceof NumExpr)
            return true;
        if(chiam instanceof IdentExpr){
            return TNumbers.getIstance().isIn(((IdentExpr)chiam).val());
        }
        return chiam instanceof TemplExpr;
    }
    public boolean isVariable(){
        if(elems.length!=0)
            return false;
        if(chiam instanceof NumExpr)
            return false;
        if(chiam instanceof IdentExpr){
            return !TNumbers.getIstance().isIn(((IdentExpr)chiam).val());
        }
        else return false;
    }
    public long numValue()throws CodeException{
        if(elems.length!=0)
            throw new CodeException("Non è un numero");
        if(chiam instanceof NumExpr)
            return ((NumExpr)chiam).value();
        if(chiam instanceof IdentExpr)
            return TNumbers.getIstance().obtain(new ParamDich(((IdentExpr)chiam).val())).getNum();
        else throw new CodeException("Non è un numero");
    }
    @Override
    public void println(int i){
        
    }
    public Espressione getEsp(){
        return chiam;
    }
    public IdentEle[] getElems(){
        return elems;
    }
    public void canRead(Variabili var, boolean validate)throws CodeException{
        TypeElem typ=chiam.returnType(var, validate);
        for (IdentEle elem : elems) {
            Membro mem = typ.information(elem.getIdent(), validate);
            if (mem.compatible(elem, var, validate) != 0) {
                throw new CodeException("Parametro accesso membro non valido");
            }
            typ.canRead(elem.getIdent(), validate);
            typ=Types.getIstance().find(mem.getType(), validate);
        }
    }
    public void canWrite(Variabili var, boolean validate)throws CodeException{
        if(elems.length==0){
            if(chiam instanceof NumExpr)
                throw new CodeException("Impossibile scrivere su numero");
            else{
                //IdentExpr
                if(validate && TNumbers.getIstance().isIn(((IdentExpr)chiam).val()))
                    throw new CodeException("Impossibile scrivere su numero");
            }
            return;//Altrimenti normale
        }
        TypeElem typ=chiam.returnType(var, validate);
        for (int i=0; i<elems.length-1; i++) {
            Membro mem = typ.information(elems[i].getIdent(), validate);
            if (mem.compatible(elems[i], var, validate) != 0) {
                throw new CodeException("Parametro accesso membro non valido");
            }
            typ.canRead(elems[i].getIdent(), validate);
            typ=Types.getIstance().find(mem.getType(), validate);
        }
        int i=elems.length-1;
        Membro mem = typ.information(elems[i].getIdent(), validate);
        if (mem.compatible(elems[i], var, validate) != 0) {
            throw new CodeException("Parametro accesso membro non valido");
        }
        typ.canWrite(elems[i].getIdent(), validate);
    }
    public TypeElem getVar(int deep, Segmenti text, Variabili vars,
            Environment env, Accumulator acc)throws CodeException{
        chiam.toCode(text, vars, env, acc);
        int m = deep < elems.length ? deep : elems.length;
        TypeElem tp=chiam.returnType(vars, false);
        for(int ii=0; ii<m; ii++)
            tp=tp.getTypeElement(text, vars, env, elems[ii], acc);
        return tp;
    }
    public TypeElem getVar(Segmenti text, Variabili vars,
            Environment env, Accumulator acc)throws CodeException{
        return this.getVar(elems.length, text, vars, env, acc);
    }
    public void setVar(Segmenti text, Variabili var,
            Environment env, Accumulator acc)throws CodeException{
        if(elems.length==0){
            if(chiam instanceof IdentExpr){
                IdentExpr id=(IdentExpr)chiam;
                var.setVar(text, acc, id.val());
                return;
            }
            else throw new CodeException(Lingue.getIstance().format("m_cod_invassg"));
        }
        int rd=acc.saveAccumulator();//valore da inserire
        chiam.toCode(text, var, env, acc);
        TypeElem ty=chiam.returnType(var, false);
        for(int j=0; j<elems.length-1; j++){
            ty=ty.getTypeElement(text, var, env, elems[j], acc);
        }
        ty.canWrite(elems[elems.length-1].getIdent(), false);//sempre positivo
        ty.setValueElement(text, var, env, elems[elems.length-1], rd, acc);
        //acc.restoreAccumulator(rd);
        //viene liberato automaticamente dal setValueElem
    }
    public void setXVar(Segmenti text, Variabili var, Environment env, 
            Accumulator acc)throws CodeException{
        int rd=acc.xsaveAccumulator();//valore da inserire, utilizzato prima
        if(elems.length==0){
            if(chiam instanceof IdentExpr){
                IdentExpr id=(IdentExpr)chiam;
                var.setVar(text, acc, id.val());
                return;
            }
            else throw new CodeException(Lingue.getIstance().format("m_cod_invassg"));
        }
        chiam.toCode(text, var, env, acc);
        TypeElem ty=chiam.returnType(var, false);
        for(int j=0; j<elems.length-1; j++){
            ty=ty.getTypeElement(text, var, env, elems[j], acc);
        }
        ty.canWrite(elems[elems.length-1].getIdent(), false);//sempre positivo
        ty.setXValueElement(text, var, env, elems[elems.length-1], rd, acc);
        //acc.xrestoreAccumulator(rd);
    }
}
