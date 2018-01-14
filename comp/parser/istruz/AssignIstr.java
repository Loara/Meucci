/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.parser.istruz;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.vars.Variabili;
import comp.general.Lingue;
import comp.parser.Espressione;
import comp.parser.Istruzione;
import comp.parser.Membro;
import comp.parser.TypeName;
import comp.parser.expr.IdentArray;
import comp.parser.expr.IdentEle;
import comp.parser.template.Template;

/**
 *
 * @author loara
 */
public class AssignIstr extends Istruzione{
    private final Espressione sin, des;
    public AssignIstr(Espressione l, Espressione r){
        sin=l;
        des=r;
    }

    @Override
    public void toCode(Segmenti text, Variabili var, Environment env, Accumulator acc) throws CodeException {
        if(des instanceof IdentArray && isGP((IdentArray)des, var, false)){
            IdentArray ddes = (IdentArray)des;
            int len =ddes.getElems().length;
            TypeElem te = ddes.getVar(len-1, text, var, env, acc);
            IdentEle vy = ((IdentArray)des).getElems()[len-1];
            Membro m =te.information(vy.getIdent(), false);
            if(!m.gpacked){
                throw new CodeException(Lingue.getIstance().format("m_cod_notgpak", m.dich.getIdent()));
            }
            te.canRead(m.getIdent(), false);
            TypeName my = m.getType();
            TypeName ar = new TypeName("Array", Template.typeToTemplate(my));
            if(!(sin.returnType(var, false).ifEstende(ar, false))){
                throw new CodeException(Lingue.getIstance().format("m_cod_invassg"));
            }
            int rey = acc.saveAccumulator();
            sin.toCode(text, var, env, acc);
            acc.restoreAccumulatorB(rey);
            te.readArrayGP(text, var, env, vy, rey, acc);
        }
        else if(sin instanceof IdentArray && isGP((IdentArray)sin, var, false)){
            IdentArray ssin =(IdentArray)sin;
            int len = ssin.getElems().length;
            des.toCode(text, var, env, acc);
            int rey = acc.saveAccumulator();
            TypeElem te = ssin.getVar(len-1, text, var, env, acc);
            IdentEle vy = ((IdentArray)des).getElems()[len-1];
            Membro m =te.information(vy.getIdent(), false);
            if(!m.gpacked){
                throw new CodeException(Lingue.getIstance().format("m_cod_notgpak", m.dich.getIdent()));
            }
            te.canWrite(m.getIdent(), false);
            TypeName my = m.getType();
            TypeName ar = new TypeName("Array", Template.typeToTemplate(my));
            if(!(des.returnType(var, false).ifEstende(ar, false))){
                throw new CodeException(Lingue.getIstance().format("m_cod_invassg"));
            }
            te.writeArrayGP(text, var, env, vy, rey, acc);
        }
        else throw new CodeException(Lingue.getIstance().format("m_cod_copyerr"));
    }
    @Override
    public void validate(Variabili var, Environment env) throws CodeException {
        sin.validate(var);
        des.validate(var);
        if(sin instanceof IdentArray && isGP((IdentArray)sin, var, true)){
            int len =((IdentArray)sin).getElems().length;
            TypeElem te = ((IdentArray)sin).partialReturnType(var, len, true);
            Membro m =te.information(((IdentArray)sin).getElems()[len-1].getIdent(), true);
            if(!m.gpacked){
                throw new CodeException(Lingue.getIstance().format("m_cod_notgpak", m.dich.getIdent()));
            }
            te.canWrite(m.getIdent(), true);
            TypeName my = m.getType();
            TypeName ar = new TypeName("Array", Template.typeToTemplate(my));
            if(!(des.returnType(var, true).ifEstende(ar, true))){
                throw new CodeException(Lingue.getIstance().format("m_cod_invassg"));
            }
        }
        else if(des instanceof IdentArray && isGP((IdentArray)des, var, true)){
            int len =((IdentArray)des).getElems().length;
            TypeElem te = ((IdentArray)des).partialReturnType(var, len, true);
            Membro m =te.information(((IdentArray)des).getElems()[len-1].getIdent(), true);
            if(!m.gpacked){
                throw new CodeException(Lingue.getIstance().format("m_cod_notgpak", m.dich.getIdent()));
            }
            te.canRead(m.getIdent(), true);
            TypeName my = m.getType();
            TypeName ar = new TypeName("Array", Template.typeToTemplate(my));
            if(!(sin.returnType(var, true).ifEstende(ar, true))){
                throw new CodeException(Lingue.getIstance().format("m_cod_invassg"));
            }
        }
        else throw new CodeException(Lingue.getIstance().format("m_cod_copyerr"));
    }
    private boolean isGP(IdentArray exp, Variabili var, boolean v)throws CodeException{
        int len =exp.getElems().length;
        if(len==0)
            return false;
        TypeElem te = exp.partialReturnType(var, len-1, v);
        Membro m = te.information(exp.getElems()[len-1].getIdent(), v);
        return m.gpacked;
    }
}
