/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.parser.istruz;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.FElement;
import comp.code.Funz;
import comp.code.Segmenti;
import comp.code.vars.Variabili;
import comp.general.Info;
import comp.parser.Espressione;
import comp.parser.Istruzione;
import comp.parser.ParserException;
import comp.parser.expr.FunzExpr;
import comp.parser.expr.IdentArray;
import comp.parser.expr.IdentExpr;
import comp.parser.template.TemplateEle;

/**
 * effettua la chiamata del costruttore della sovracclasse
 * 
 * :super nomecostruttore (SENZA il nome dell'oggetto da costruire)
 * @author loara
 */
public class SuperIstr extends Istruzione{
    private final String cname;
    private final Espressione var;
    private final TemplateEle[] tem;
    private final Espressione[] pars;
    public SuperIstr(FunzExpr fe)throws ParserException{
        cname=fe.getName();
        var=fe.getValues()[0];
        tem=fe.template();
        pars=fe.getValues();
    }
    @Override
    public void validate(Variabili vars, Environment env)throws CodeException{
        if(!(var instanceof IdentArray) || !((IdentArray)var).isVariable())
            throw new CodeException("--");
        IdentArray fff=(IdentArray)var;
        FElement req=Funz.getIstance().
                requestCostructor(cname, tem, Info.paramTypes(pars, vars, true), true);
        if(!var.returnType(vars, true).ifEstende(req.trequest[0], true))
            throw new CodeException("-");
        String vvg=((IdentExpr)fff.getEsp()).val();
        if(!vvg.equals(vars.getCostrVarName()))
            throw new CodeException("---");
    }
    @Override
    public void toCode(Segmenti text, Variabili vars, Environment env, Accumulator acc)
            throws CodeException{
        if(!(var instanceof IdentArray) || !((IdentArray)var).isVariable())
            throw new CodeException("--");
        IdentArray fff=(IdentArray)var;
        FElement req=Funz.getIstance().
                requestCostructor(cname, tem, Info.paramTypes(pars, vars, true), true);
        if(!var.returnType(vars, true).ifEstende(req.trequest[0], true))
            throw new CodeException("-");
        String vvg=((IdentExpr)fff.getEsp()).val();
        if(!vvg.equals(vars.getCostrVarName()))
            throw new CodeException("---");
        Espressione[] ppp=new Espressione[pars.length+1];
        ppp[0]=vars.getCostrAsExpr();
        for(int i=1; i<ppp.length; i++)
            ppp[i]=pars[i-1];
        FunzExpr.perfCall(req, ppp, text, vars, env, acc);
    }
}
