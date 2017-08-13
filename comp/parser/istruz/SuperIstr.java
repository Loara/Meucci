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
import comp.parser.template.TemplateEle;

/**
 * effettua la chiamata del costruttore della sovracclasse
 * 
 * :super nomecostruttore (SENZA il nome dell'oggetto da costruire)
 * @author loara
 */
public class SuperIstr extends Istruzione{
    private final String cname;
    private final TemplateEle[] tem;
    private final Espressione[] pars;
    public SuperIstr(FunzExpr fe)throws ParserException{
        cname=fe.getName();
        tem=fe.template();
        pars=fe.getValues();
    }
    @Override
    public void validate(Variabili vars, Environment env)throws CodeException{
        FElement req=Funz.getIstance().
                requestCostructor(cname, tem, Info.paramTypes(pars, vars, true), true);
        if(!vars.getCostrAsExpr().returnType(vars, true).ifEstende(req.trequest[0], true))
            throw new CodeException("-");
    }
    @Override
    public void toCode(Segmenti text, Variabili vars, Environment env, Accumulator acc)
            throws CodeException{
        IdentArray fff=vars.getCostrAsExpr();
        FElement req=Funz.getIstance().
                requestCostructor(cname, tem, Info.paramTypes(pars, vars, true), false);
        if(!fff.returnType(vars, true).ifEstende(req.trequest[0], true))
            throw new CodeException("-");
        Espressione[] ppp=new Espressione[pars.length+1];
        ppp[0]=fff;
        for(int i=1; i<ppp.length; i++)
            ppp[i]=pars[i-1];
        vars.getVarStack().pushAll(text);
        if(req.isExternFile())
            Funz.getIstance().ext.add(req.modname);
        FunzExpr.perfCall(req, ppp, text, vars, env, acc);
    }
}
