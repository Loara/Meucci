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
    private final boolean[] doubledot;
    public IdentArray(Espressione ex, IdentEle[] ie, boolean[] dd)throws ParserException{
        if(ie==null || ie.length==0){
            elems=new IdentEle[0];
            doubledot=new boolean[0];
            if(ex instanceof IdentExpr || ex instanceof NumExpr || ex instanceof TemplExpr)
                chiam=ex;
            else throw new ParserException("Non è un IdentExpr o un numero", 0);
        }
        else{
            if(ie.length!=dd.length)throw new ParserException("Lunghezze discordanti", 0);
            elems=ie;
            chiam=ex;
            doubledot=dd;
        }
    }
    public IdentArray(String it){
        chiam=new IdentExpr(it);
        elems=new IdentEle[0];
        doubledot=new boolean[0];
    }
    @Override
    public TypeElem returnType(Variabili var, boolean v)throws CodeException{
        TypeElem tp=chiam.returnType(var, v);
        for(IdentEle ee: elems){
            tp=Types.getIstance().find(tp.information(ee.getIdent(), v).getType(), v);
        }
        return tp;
    }
    @Override
    public void validate(Variabili vars)throws CodeException{
        chiam.validate(vars);
        TypeElem ty=chiam.returnType(vars, true);
        for (IdentEle elem : elems) {
            Membro m = ty.information(elem.getIdent(), true);
            for (Espressione val : elem.getVals()) {
                val.validate(vars);
            }
            if (m.compatible(elem, vars, true)!=0) {
                throw new CodeException("Parametri di accesso al parametro " + elem.getIdent() + " non validi");
            }
            ty.canRead(elem.getIdent(), true);
            ty = Types.getIstance().find(ty.information(elem.getIdent(), true).getType(), true);
        }
    }
    @Override
    public void toCode(Segmenti text, Variabili vars, Environment env, Accumulator acc)
    throws CodeException{
        if(elems.length==0)
            chiam.toCode(text, vars, env, acc);//gestisce true, false, null, etc.
        else
            vars.getVar(this, text, env, acc);//gestisce anche i numeri
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
        if(chiam instanceof TemplExpr){
            return true;
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
    public boolean[] getDDots(){
        return doubledot;
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
}
