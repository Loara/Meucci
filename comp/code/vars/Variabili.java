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
        throw new CodeException("Impossibile trovare variabile "+name);
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
        throw new CodeException("Variabile "+ident+" non trovata");
    }
    /**
     * Ritorna variabile nell'accumulatore
     * @param text
     * @param acc
     * @param ident
     * @throws comp.code.CodeException 
     */
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
        throw new CodeException("Variabile "+ident+" non trovata");
    }
    
    public void getVar(IdentArray i, Segmenti text,
            Environment env, Accumulator acc)throws CodeException{
        i.getEsp().toCode(text, this, env, acc);
        IdentEle[] ee=i.getElems();
        if(ee.length>0){
            boolean[] dd=i.getDDots();
            TypeElem tp=i.getEsp().returnType(this, false)
                    .getTypeElement(text, this, env, ee[0], acc, dd[0]);
            for(int ii=1; ii<ee.length; ii++)
                tp=tp.getTypeElement(text, this, env, ee[ii], acc, dd[ii]);
        }       
    }
    /**
     * 
     * @param i
     * @param text
     * @param env
     * @param acc
     * @throws CodeException 
     */
    public void setVar(IdentArray i, Segmenti text,
            Environment env, Accumulator acc)throws CodeException{
        int l=i.getElems().length;
        if(l==0){
            if(i.getEsp() instanceof IdentExpr){
                IdentExpr id=(IdentExpr)i.getEsp();
                setVar(text, acc, id.val());
                return;
            }
            else throw new CodeException("Assegnamento ad una non-variabile");
        }
        int rd=acc.saveAccumulator();//valore da inserire
        IdentEle[] kk=i.getElems();
        boolean[] bb=i.getDDots();
        i.getEsp().toCode(text, this, env, acc);
        TypeElem ty=i.getEsp().returnType(this, false);
        for(int j=0; j<l-1; j++){
            ty=ty.getTypeElement(text, this, env, kk[j], acc, bb[j]);
        }
        ty.canWrite(kk[l-1].getIdent(), false);//sempre positivo
        ty.setValueElement(text, this, env, kk[l-1], rd, acc, bb[l-1]);
        //acc.restoreAccumulator(rd);
        //viene liberato automaticamente dal setValueElem
    }
    /**
     * da aggiustare, mette l'accumulatore X nella variabile
     * @param i
     * @param text
     * @param env
     * @param acc
     * @throws CodeException 
     */
    public void setXVar(IdentArray i, Segmenti text, Environment env, 
            Accumulator acc)throws CodeException{
        int l=i.getElems().length;
        int rd=acc.xsaveAccumulator();//valore da inserire, utilizzato prima
        if(l==0){
            if(i.getEsp() instanceof IdentExpr){
                IdentExpr id=(IdentExpr)i.getEsp();
                setVar(text, acc, id.val());
                return;
            }
            else throw new CodeException("Assegnamento ad una non-variabile");
        }
        IdentEle[] kk=i.getElems();
        boolean[] bb=i.getDDots();
        i.getEsp().toCode(text, this, env, acc);
        TypeElem ty=i.getEsp().returnType(this, false);
        for(int j=0; j<l-1; j++){
            ty=ty.getTypeElement(text, this, env, kk[j], acc, bb[j]);
        }
        ty.canWrite(kk[l-1].getIdent(), false);//sempre positivo
        ty.setXValueElement(text, this, env, kk[l-1], rd, acc, bb[l-1]);
        //acc.xrestoreAccumulator(rd);
    }
    /*
    Vedere se se ne può fare a meno
    public void setVar(IdentArray i, Segmenti text, Environment env, 
            Accumulator acc, long ji, int dimens)throws CodeException{
        int l=i.getElems().length;
        if(l==0){
            if(i.getEsp() instanceof IdentExpr){
                IdentExpr id=(IdentExpr)i.getEsp();
                setVar(text, id.val(), ji, dimens);
                return;
            }
            else throw new CodeException("Assegnamento ad una non-variabile");
        }
        int rd=acc.prenota();//valore da inserire
        text.addIstruzione("mov",acc.getReg(rd).getReg(dimens),String.valueOf(ji));
        IdentEle[] vt=i.getElems();
        boolean[] bb=i.getDDots();
        i.getEsp().toCode(text, this, env, acc);//funge da getvar
        TypeElem ty=i.getEsp().returnType(this, false);
        for(int j=0; j<l-1; j++){
            ty=ty.getTypeElement(text, this, env, vt[j], acc, bb[j]);
        }
        ty.canWrite(vt[l-1].getIdent(), false);//sempre positivo
        Register reg=acc.getReg(rd);
        acc.libera(rd);//viene rinforzato in setValue, usato per evitare inutili push e pop
        ty.setValueElement(text, this, env, vt[l-1], reg, acc, bb[l-1]);
        //acc.libera(rd);
    }
    */
    /**
     * mette ax nella variabile
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
        throw new CodeException("Variabile "+ident+" non trovata");
    }
    /*
    public void setVar(Segmenti text, String ident, long reg, int dim)throws CodeException{
        for(Var v:varSt){
            if(v.isIn(ident)){
                v.setVar(text, ident, reg, dim);
                return;
            }
        }
        throw new CodeException("Variabile "+ident+" non trovata");
    }
    
    public void clearVar(Segmenti text, String name, int dimens)throws CodeException{
        setVar(text, name, 0, dimens);//usato solo per le dichiarazioni
    }
    */
    public VarStack getVarStack(){
        return vars;
    }
    public GhostVar getGhostVar(){
        return gvar;
    }
    /*
     * Ritorna il tipo di un array di IdentEle. Utile in varie occasioni. (Non dovrebbe essere più utilizzato
     * @param id
     * @param v
     * @return 
     * @throws comp.code.CodeException 
     
    public TypeElem returnTypeAdvanced(IdentEle[] id, boolean v)throws CodeException{
        TypeElem ci=getType(id[0].getIdent());
        for(int i=1;i<id.length;i++){
            ci=Types.getIstance().find(ci.information(id[i].getIdent(), v).getType(), v);
        }
        return ci;
    }
*/
}
