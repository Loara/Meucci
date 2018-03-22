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
package comp.parser;

import comp.code.Accumulator;
import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Funz;
import comp.code.ModLoader;
import comp.code.ModLoader.MLdone;
import comp.code.Segmenti;
import comp.code.Types;
import comp.code.template.TNumbers;
import comp.code.vars.Variabili;
import comp.general.Lingue;
import comp.general.Master;
import comp.general.Stack;
import comp.general.VScan;
import comp.parser.istruz.IstrExe;
import comp.scanner.EolToken;
import comp.scanner.IdentToken;
import comp.scanner.PareToken;
import comp.scanner.SymbToken;
import comp.scanner.Token;
import comp.parser.istruz.MultiIstr;
import comp.scanner.VirgToken;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 * Ecco una guida su molte funzioni generate automaticamente:
 * <ul>
 * <li> _INIT_MOD_nomemodulo : Inizializza il modulo, in particolare richiama per ciascun tipo non
 * template e con vtable la funzione _VT_INIT_ e chiama l'eventuale static. Il main richiama quello del proprio modulo
 * che a sua volta richiama le dipendenze</li>
 * <li> _INIT_TEMPS : chiama la funzione _VT_INIT_ per tipi template con vtable. 
 * Richiamato dal main</li>
 * <li> _VT_INIT_nometipo : Inizializza la vtable. Tale funzione esiste solo per tipi che
 * hanno vtable</li>
 * <li> _INIT_nometipo : dato un puntatore a un tipo istanziato: Se il tipo ha vtable glie la fa puntare;
 * se non ce l'ha ma non è esplicito pone il campo pari a 0. Richiamato prima del 
 * costruttore ed esiste solo per tipi non espliciti
 * </ul>
 * @author loara
 */
public class Modulo {
    public final String nome;
    public final String[] deps;
    public final Boolean[] publ;
    public final TypeDef[] type;
    public final Callable[] ca;
    public final Dichiarazione[] internal;//variabili interne, non si esportano, sono shadow
    public final MultiIstr Static;//inizializzatore codice
    public Modulo(VScan<Token> t)throws ParserException{
        if(!t.reqSpace(4))
            throw new FineArrayException();
        if(t.get().isIdent("modulo")){
            t.nextEx();
            if(t.get() instanceof IdentToken)
                nome=((IdentToken)t.get()).getString();
            else throw new ParserException("Nome mod non valido", t);
            t.nextEx();
            //dipendenze
            if(t.get().isIdent("depends")){
                Stack<String> de=new Stack<>(String.class);
                Stack<Boolean> pb=new Stack<>(Boolean.class);
                do{
                    t.nextEx();
                    if(!(t.get() instanceof IdentToken)){
                        throw new ParserException("", t);
                    }
                    if(((IdentToken)t.get()).getString().equals("public")){
                        pb.push(true);
                        t.nextEx();
                    }
                    else
                        pb.push(false);
                    if(!(t.get() instanceof IdentToken)){
                        throw new ParserException("", t);
                    }
                    de.push(((IdentToken)t.get()).getString());
                    t.nextEx();
                }
                while(t.get() instanceof VirgToken);
                deps=de.toArray();
                publ=pb.toArray();
            }
            else{
                deps=new String[0];
                publ=new Boolean[0];
            }
            
            if(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='{'))
                throw new ParserException(Lingue.getIstance().format("m_par_invmod", nome), t);
            t.nextEx();
            Stack<TypeDef> td=new Stack<>(TypeDef.class);
            Stack<Callable> cal=new Stack<>(Callable.class);
            Stack<Dichiarazione> di=new Stack<>(Dichiarazione.class);
            MultiIstr mit=null;
            while(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='}')){
                if(!(t.get() instanceof IdentToken))
                    throw new ParserException(Lingue.getIstance().format("m_par_errele"), t);
                IdentToken id=(IdentToken)t.get();
                if(id.getString().equals("static")){
                    if(mit!=null)
                        throw new ParserException(Lingue.getIstance().format("m_par_stcerr"),t);
                    t.nextEx();
                    Istruzione i=IstrExe.toIstr(t);
                    if(!(i instanceof MultiIstr))
                        throw new ParserException(Lingue.getIstance().format("m_par_invist"), t);
                    mit=(MultiIstr)i;
                    continue;
                }
                if(id.getString().equals("type")){
                    TypeDef tyde=new TypeDef(t, nome, cal);
                    td.push(tyde);
                    continue;
                }
                if(id.getString().equals("costructor")){
                    Costructor co=new Costructor(t, nome);
                    cal.push(co);
                    continue;
                }
                int iniz=t.getInd();
                if(id.getString().equals("shadow")){
                    t.nextEx();
                }
                TypeName tn=new TypeName(t);
                if(t.get() instanceof SymbToken){
                    t.setInd(iniz);
                    OpDef op=new OpDef(t, nome);
                    cal.push(op);
                }
                else if(t.get() instanceof IdentToken){
                    if(t.get(1) instanceof EolToken){
                        di.push(new Dichiarazione(tn, ((IdentToken)t.get()).getString()));
                        t.nextEx();
                        t.nextEx();
                        continue;
                    }
                    t.setInd(iniz);
                    Funzione f=new Funzione(t, nome);
                    cal.push(f);
                }
            }
            t.next();//così, per sfizio;
            type=td.toArray();
            ca=cal.toArray();
            internal=di.toArray();
            Static=mit;
        }
        else throw new ParserException(Lingue.getIstance().format("m_par_errmod"), t);
    }
    private void writeInternal(PrintWriter pw)throws CodeException{
        String u;
        for(Dichiarazione di:internal){
            switch(Types.getIstance().find(di.getRType(), false).realDim()){
                case 1:
                    u="db";
                    break;
                case 2:
                    u="dw";
                    break;
                case 4:
                    u="dd";
                    break;
                default:
                    u="dq";
            }
            pw.println("V"+di.getIdent()+":\t"+u+"\t0");
        }
    }
    private void settaAmbiente()
            throws CodeException, IOException, ClassNotFoundException{
        Funz.getIstance().clearAll();
        Types.getIstance().clearAll();
        TNumbers.getIstance().clearAll();
        Environment.currentModulo=nome;
        HashSet<MLdone> loadedModules=new HashSet<>();
        ModLoader.getIstance().importAllModules(loadedModules, nome, nome);
        loadedModules.stream().map((m) -> {
            Types.getIstance().loadAll(m.typ);
            return m;
        }).forEach((m) -> {
            Funz.getIstance().loadAll(m.fun);
        });
        /*
        loadedModules.stream().map((m) -> {
            Types.getIstance().loadAll(m.typ);
            return m;
        }).map((m) -> {
            Types.getIstance().loadAllTemplates(m.Ttyp);
            return m;
        }).map((m) -> {
            Funz.getIstance().loadAll(m.fun);
            return m;
        }).forEach((m) -> {
            Funz.getIstance().loadAllTemplates(m.Tcal);
        });
        //Anche gli shadow vanno nell'intestazione
        */
    }
    public void Codifica(PrintWriter out, Master mas)
            throws CodeException, IOException, ClassNotFoundException{
        //prima carica informazioni
        settaAmbiente();
        //---
        Segmenti text=new Segmenti();
        Environment env=new Environment();//è unico per ogni file
        boolean main=false;//entry-point
        if(Static!=null){
            text.addLabel("static");
            text.add("");//enter
            int i=text.text.size()-1;
            Accumulator acc=new Accumulator();
            Variabili vars=new Variabili(new FunzParam[0], internal, false, acc);
            Static.toCode(text, vars, env, acc);
            text.text.toArrayList().set(i, "\tenter\t"+vars.getVarStack().internalVarsMaxDim()+",0");
            if(!text.text.toArrayList().get(text.text.size()-2).equals("\tleave")){
                text.addIstruzione("leave", null, null);
                text.addIstruzione("ret", null, null);
            }
        }
        for(Callable cal:ca){
            if(!main && cal.getName().equals("main"))
                main=true;
            cal.toCode(text, internal, env);
        }
        for(TypeDef td:type){
            td.toCode(text, internal, env);
        }
        /*
        Types.getIstance().getClassList().notifiche().forEach((Notifica t) -> {
            //Per i template
            Funz.getIstance().ext.add("_VT_INIT_"+Meth.className(t.nome, t.parametri));
            //Tra le notifiche niente classi esplicite
        });
        */
        text.closeAll();//effettua il flush a tutti
        out.println("\tglobal\t_INIT_MOD_"+nome+":function");
        for(String dep:deps)
            out.println("\textern\t_INIT_MOD_"+dep);
        Funz.getIstance().glob.stream().forEach((gl) -> {
            out.println("\tglobal\t"+gl);
        });
        if(main){
            out.println("\tglobal\t_start:function");
            out.println("\textern\tSyscalls~@exit");
            out.println("\textern\t_INIT_TEMPS");//Inizializza i templates
        }
        Funz.getIstance().ext.stream().forEach((ex) -> {
            out.println("\textern\t"+ex);
        });
        
        out.println("\tsection\t.vtors\tprogbits alloc exec nowrite align=16");
        //inizializzatore modulo
        initModulo(out);
        text.stat.stream().forEach((t) -> {
            out.println(t);
        });
        text.vtors.toArrayList().stream().forEach((t) ->{
            out.println(t);
        });
        
        out.println("\tsection\t.data");
        out.println("_init:\tdb\t0");//variabile inizializzazione
        writeInternal(out);
        text.data.stream().forEach((t) -> {
            out.println(t);
        });
        
        out.println("\tsection\t.rodata");
        text.rodata.stream().forEach((t) ->{
            out.println(t);
        });
        
        out.println("\tsection\t.bss");
        text.bss.stream().forEach((t) ->{
            out.println(t);
        });
        
        out.println("\tsection\t.text");
        text.text.toArrayList().stream().forEach((t) -> {
            out.println(t);
        });
        if(main)
            standardStart(out);
        Funz.getIstance().clearGE();
    }
    private void initModulo(PrintWriter out)throws CodeException{
        out.println("_INIT_MOD_"+nome+":");
        out.println("\tbts\tword [_init],0");//test 0 bit, and set it to 1
        out.println("\tjc\t__iv");//if cf==1 jump
        for(String t:deps)
            out.println("\tcall\t_INIT_MOD_"+t);
        for(TypeDef i:type){
            if(i.hasVTable())
                out.println("\tcall\t_VT_INIT_"+i.getName());
        }
        if(Static!=null)
            out.println("\tcall\tstatic");
        out.println("__iv:");
        out.println("\tret");  
    }
    private void standardStart(PrintWriter out){
            out.println("_start:");
            out.println("\tnop");
            out.println("\tcall\t_INIT_MOD_"+nome);
            out.println("\tcall\t_INIT_TEMPS");
            out.println("\tcall\t"+nome+"~@main");
            out.println("\tcall\tSyscalls~@exit");
            out.println("\tret");        
    }
}
