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
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.general.Lingue;
import comp.parser.Istruzione;
import comp.parser.ParserException;

/**
 *
 * @author loara
 */
public class TryIstr extends Istruzione{
    private final MultiIstr tr;
    private final String[] exname;
    private final MultiIstr[] mexc;
    private final MultiIstr defaul;
    public TryIstr(MultiIstr ty, String[] err, MultiIstr[] me, MultiIstr def, int r)throws ParserException{
        tr=ty;
        exname=err;
        mexc=me;
        defaul=def;
        eqErr(r, def!=null);
    }
    public TryIstr(MultiIstr ty, String[] err, MultiIstr[] me, int r)throws ParserException{
        this(ty, err, me, null, r);
    }
    private void eqErr(int y, boolean def)throws ParserException{
        if(!def && (exname == null || exname.length == 0))
            throw new ParserException(Lingue.getIstance().format("m_par_tryctc"), y);
        for(int i=0; i<exname.length; i++){
            for(int j=i+1; j<exname.length; j++){
                if(exname[i].equals(exname[j]))
                    throw new ParserException(Lingue.getIstance().format("m_par_eqlctc", exname[i]), y);
            }
        }
    }
    @Override
    public void validate(Variabili var, Environment env)throws CodeException{
        env.addTry("TRY", exname, defaul != null);//Non ha importanza il nome
        tr.validate(var, env);
        env.removeTry();
        for(MultiIstr m:mexc)
            m.validate(var, env);
        if(defaul != null)
            defaul.validate(var, env);
    }
    @Override
    public void toCode(Segmenti text, Variabili var, Environment env,
            Accumulator acc)throws CodeException{
        env.increment("TRY");
        int u=env.get("TRY");
        String n = "TRY"+u;
        env.addTry(n, exname, defaul != null);
        var.getVarStack().addTryBlock(n);
        tr.toCode(text, var, env, acc);
        var.getVarStack().removeTryBlock(text);
        env.removeTry();
        text.addIstruzione("jmp", n+"_END", null);
        for(int i =0; i<mexc.length; i++){
            text.addLabel(Environment.encode(n, exname[i]));
            mexc[i].toCode(text, var, env, acc);
            text.addIstruzione("jmp", n+"_END", null);
        }
        if(defaul != null){
            text.addLabel(Environment.encode(n));
            defaul.toCode(text, var, env, acc);
        }
        text.addLabel(n+"_END");
    }
    /*
    Va bene sia in toCode che in validate
    */
    public static boolean checkThrows(String err, Environment env){
        String eh=env.getErrorHandler(err);
        if(eh!=null)
            return true;
        return Info.isIn(err, Environment.errors);
    }
    public static boolean checkThrows(String[] errs, Environment env){
        for(String err:errs){
            if(!checkThrows(err, env))
                return false;
        }
        return true;
    }
}
