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

import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Funz;
import comp.code.Funz.FElement;
import comp.code.Meth;
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.general.Info;
import comp.general.Lingue;
import comp.general.Stack;
import comp.general.VScan;
import comp.parser.template.Template;
import comp.parser.template.TemplateEle;
import comp.scanner.EolToken;
import comp.scanner.IdentToken;
import comp.scanner.PareToken;
import comp.scanner.Token;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author loara
 */
public class TypeDef implements Serializable{
    protected String nome, modulo;
    protected TypeName ext;
    protected Membro[] types;
    protected FMorg[] ffm;
    protected boolean explicit;
    /*
    protected transient Costructor[] cc;//I costruttori vanno gestiti come funzioni indipendenti
    //quindi non vengono aggiunti
    */
    protected Template[] tt;
    public TypeDef(VScan<Token> t, String modulo, Stack<Callable> costr,
            Stack<Callable> Tcostr)throws ParserException{
        if(!(t.get() instanceof IdentToken) || !((IdentToken)t.get()).getString().equals("type"))
            throw new ParserException(Lingue.getIstance().format("m_par_invtyp"), t);
        t.nextEx();
        if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("explicit")){
            explicit=true;
            t.nextEx();
        }
        else
            explicit=false;
        if(!(t.get() instanceof IdentToken))
            throw new ParserException(Lingue.getIstance().format("m_par_invnam"), t);
        this.modulo=modulo;
        nome=((IdentToken)t.get()).getString();
        Info.isForbitten(nome, t.get().getRiga());
        t.nextEx();
        tt=Template.parseTemp(t);
        if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("extends")){
            if(!(t.get(1) instanceof IdentToken))
                throw new ParserException(Lingue.getIstance().format("m_par_exterr"), t);
            t.nextEx();
            ext=new TypeName(t);
        }
        Stack<Membro> s=new Stack<>(Membro.class);
        Stack<FMorg> fmm=new Stack<>(FMorg.class);
        boolean ifTem=tt.length!=0;
        if(t.get() instanceof PareToken && ((PareToken)t.get()).s=='{'){
            t.nextEx();
            boolean hasDes=false;
            while(!(t.get() instanceof PareToken && ((PareToken)t.get()).s=='}')){
                if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("init")){
                    Costructor ccc=new Costructor(t, nome, tt, modulo);
                    if(ifTem)
                        Tcostr.push(ccc);
                    else
                        costr.push(ccc);
                    if(!(t.get() instanceof EolToken))
                        throw new ParserException(Lingue.getIstance().format("m_par_dotcom"), t);
                    t.nextEx();
                }
                else if(t.get() instanceof IdentToken && ((IdentToken)t.get()).getString().equals("end")){
                    Destructor ccc=new Destructor(t, nome, tt, modulo);
                    if(hasDes)
                        throw new ParserException(Lingue.getIstance().format("m_par_onedis"), t);
                    else if(explicit)
                        throw new ParserException(Lingue.getIstance().format("m_par_expdis"), t);
                    else{
                        hasDes=true;
                        if(tt.length==0)//Per chiamate di sovradistruttori
                            costr.push(ccc);
                        else
                            Tcostr.push(ccc);
                    }
                    if(!(t.get() instanceof EolToken))
                        throw new ParserException(Lingue.getIstance().format("m_par_dotcom"), t);
                    t.nextEx();
                }
                else{
                    Membro dich=new Membro(t, explicit);
                    s.push(dich);
                    FMorg ftt;
                    if(t.get() instanceof PareToken && ((PareToken)t.get()).s=='{'){
                        ftt=new FMorg(t, dich.getType(), nome, tt, dich.getIdent(), modulo);
                    }
                    else{
                        ftt=new FMorg(dich.getType());
                    }
                    //Generazione automatica
                    if(!dich.explicit && !dich.ghost && !dich.override && !dich.shadow){
                        if(ftt.get==null){
                            if(dich.params.length>0)
                                throw new ParserException(Lingue.getIstance().format("m_par_ngenac"), t);
                            ftt.get=new DefFunzMem(dich.getType(), nome, tt, dich.getIdent(), modulo, true);
                        }
                        if(ftt.set==null){
                            if(dich.params.length>0)
                                throw new ParserException(Lingue.getIstance().format("m_par_ngenac"), t);
                            if(!dich.read)
                                ftt.get=new DefFunzMem(dich.getType(), nome, tt, dich.getIdent(), modulo, false);
                        }
                    }
                    fmm.push(ftt);
                    if(!t.reqSpace(2))//punto e virgola e successivo
                        throw new FineArrayException();
                    if(!(t.get() instanceof EolToken))
                        throw new ParserException(Lingue.getIstance().format("m_par_dotcom"), t);
                    t.nextEx();
                }
            }
            types=s.toArray();
            ffm=fmm.toArray();
            t.next();
        }
        else throw new ParserException(Lingue.getIstance().format("m_par_grftyp"), t);
    }
    /**
     * Per ClassList
     * @param o
     * @return 
     */
    @Override
    public boolean equals(Object o){
        if(o instanceof TypeDef)
            return nome.equals(((TypeDef)o).nome);
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.nome);
        return hash;
    }
    /**
     * Scrive l'oggetto in memoria, determina costruttori ed inizializatori
     * @param seg
     * @param varSt 
     * @param env 
     * @param tparams 
     * @throws comp.code.CodeException 
     */
    public void toCode(Segmenti seg, Dichiarazione[] varSt, Environment env, TemplateEle... tparams)throws CodeException{
        if(tparams.length!=tt.length)
            throw new CodeException("Parametri incompatibili");
        Environment.template=tt.length!=0;
        String rax=Register.AX.getReg();
        String rbx=Register.BX.getReg();
        String rnome=Meth.className(nome, tparams);//Il nome del relativo TypeElem
        if(explicit){
            if(ext!=null){
                if(!Types.getIstance().find(ext, false).explicit)
                    throw new CodeException("Una classe esplicita non può estenderne una non esplicita");
            }
        }
        else{
            if(ext!=null){
                if(Types.getIstance().find(ext, false).explicit)
                    throw new CodeException("Una classe non esplicita non può estenderne una esplicita");
            }
        }
        if(ext != null){
            Types.getIstance().find(ext, false);//Controllare la correttezza
        }
        for(Membro type:types)
            type.chechPack(false);
        if(hasVTable()){//Se classe esplicita non ha bisogno di generare vtable
            generateVT(seg, env, varSt, tparams);
            seg.addLabel("_INIT_"+rnome);//assegna la vtable all'oggetto nello stack
            Funz.getIstance().glob.add("_INIT_"+rnome);
            seg.addIstruzione("enter","0","0");
            seg.addIstruzione("mov",rax,"[rbp+16]");
            seg.addIstruzione("lea",rbx,"[_VT_"+rnome+"]");
            seg.addIstruzione("mov","["+rax+"]",rbx);
            seg.addIstruzione("leave", null, null);
            seg.addIstruzione("ret", String.valueOf(Info.pointerdim), null);
        }
        else{
            for (FMorg ffm1 : ffm) {
                if (ffm1.get != null || ffm1.set != null) {
                    throw new CodeException("Funzioni accesso non valide");                
                }
            }
            if(!explicit){
                seg.addLabel("_INIT_"+rnome);//assegna la vtable all'oggetto nello stack
                Funz.getIstance().glob.add("_INIT_"+rnome);
                seg.addIstruzione("enter","0","0");
                seg.addIstruzione("mov",rax,"[rbp+16]");
                seg.addIstruzione("xor",rbx,rbx);
                seg.addIstruzione("mov","["+rax+"]",rbx);
                seg.addIstruzione("leave", null, null);
                seg.addIstruzione("ret", String.valueOf(Info.pointerdim), null);
            }
        }
    }
    public void validate(Dichiarazione[] varSt, Environment env)throws CodeException{
        Template.addTemplateConditions(tt);
        if(explicit){
            if(ext!=null){
                if(!Types.getIstance().find(ext, true).explicit)
                    throw new CodeException("Una classe esplicita non può estenderne una non esplicita");
            }
        }
        else{
            if(ext!=null){
                if(Types.getIstance().find(ext, true).explicit)
                    throw new CodeException("Una classe non esplicita non può estenderne una esplicita");
            }
        }
        if(ext != null){
            Types.getIstance().find(ext, true);//Controllare la correttezza
        }
        //Le classi esplicite garantiscono che i membri lo siano
        //I packed sono explicit
        for(int i=0; i<types.length; i++){//mantiene l'ordine di salvataggio
            if(ffm[i].get!=null){
                if(types[i].explicit || types[i].shadow)
                    throw new CodeException("Funzioni accesso non valide");
                ffm[i].get.validate(env, varSt);
            }
            else{
                if(types[i].ghost)
                    throw new CodeException("Funzioni accesso non valide");
            }
            if(ffm[i].set!=null){
                if(types[i].explicit || types[i].shadow || types[i].read)
                    throw new CodeException("Funzioni accesso non valide");
                ffm[i].set.validate(env, varSt);
            }
            else{
                if(!types[i].read && types[i].ghost)
                    throw new CodeException("Funzioni accesso non valide");
            }
            if(types[i].override){
                if(ext==null)
                    throw new CodeException("Funzioni accesso non valide");
                TypeElem th=Types.getIstance().find(ext, true);
                th.checkCorrectOverride(types[i], true);                
            }
        }
        Template.removeTemplateConditions(tt);
            //I controlli di parametro di packed sono già fatti
            /*
            if(types[i].explicit || types[i].shadow){
                if(ffm[i]!=null)
                    throw new CodeException("Funzioni accesso non valide");
                types[i].chechPack(true);
                continue;
            }
            else if(types[i].ghost){
                if(ffm[i]==null || ffm[i].get==null)
                    throw new CodeException("Funzione accesso non valida");
                if(!types[i].read && ffm[i].set==null)
                    throw new CodeException("Funzione accesso non valida");
            }
            else if(types[i].override){
                if(ext==null)
                    throw new CodeException("Uso improprio del modificatore override");
                TypeElem th=Types.getIstance().find(ext, true);
                th.checkCorrectOverride(types[i], true);
            }
            else if(types[i].read){
                //non explicit, nè override, nè ghost
                if(ffm[i]!=null && ffm[i].set!=null)
                    throw new CodeException("Funzione set in membro read");
            }
            if(!types[i].override && ext!=null){
                TypeElem th=Types.getIstance().find(ext, true);
                if(th.informationSafe(types[i].getIdent(), true)!=null)
                    throw new CodeException("Ri-dichiarazione di membro senza override");
            }
            if(ffm[i]==null){
                if(types[i].params.length!=0)
                    throw new CodeException("Impossibile generare funzioni di accesso di default");
            }
            else{
                if(ffm[i].get!=null){
                    ffm[i].get.validate(env, varSt);
                }
                else if(types[i].params.length!=0)
                    throw new CodeException("Impossibile generare funzioni di accesso di default");
                //funzione set
                if(ffm[i].set!=null){
                    ffm[i].set.validate(env, varSt);
                }
                else if(types[i].params.length!=0)
                    throw new CodeException("Impossibile generare funzioni di accesso di default");
            }
            */
        
    }
    /**
     * scrive funzione che si occupa dell'inizializzazione vtables. Chiamato solo da non explicit
     * @param seg 
     * @throws comp.code.CodeException 
     */
    private void generateVT(Segmenti seg, Environment env, Dichiarazione[] varSt,
            TemplateEle... vparams)throws CodeException{
        String rax=Register.AX.getReg();
        String rbx=Register.BX.getReg();
        TypeElem v=Types.getIstance().find(new TypeName(nome, vparams), false);
        //alloca stat. la vtable
        String rnome=Meth.className(nome, vparams);
        String vtab="_VT_"+rnome;
        seg.bss.add(vtab+":\tresb\t"+v.vt.dimension());
        
        //genera una nuova funzione che serve per inizializzare la VTABLE
        //viene chiamata dall'inizializzatore del modulo
        //Per via del distruttore, la vtable non è mai pari a 0
        
        seg.addLabel("_VT_INIT_"+rnome);
        seg.addIstruzione("lea", rax, "["+vtab+"]");
        seg.addIstruzione("call", "_VT_INIT_E_"+rnome, null);
        seg.addIstruzione("ret", null, null);
        Funz.getIstance().glob.add("_VT_INIT_"+rnome);
        Funz.getIstance().glob.add("_VT_INIT_E_"+rnome);
        seg.addLabel("_VT_INIT_E_"+rnome);//richiamato da sovrattipi
        seg.addIstruzione("enter","0","0");//indirizzo vtable in r0
        //Environment.template=tt.length!=0;    Già inserito in toCode
        if(ext!=null){
            TypeName rext=Types.getIstance().translate(ext);
            if(ext.templates().length !=0 || Types.getIstance().find(ext, false).external
                    || vparams.length!=0)
                Funz.getIstance().ext.add("_VT_INIT_E_"+Meth.className(rext));
            seg.addIstruzione("call", "_VT_INIT_E_"+Meth.className(rext), null);
        }
        int rd;
        TypeElem th=Types.getIstance().find(new TypeName(nome, vparams), false);
        //Il distruttore è presente all'interno del file
        FElement fe=Funz.getIstance().requestDestructor(nome, vparams, false);
        if(fe.isExternFile())
            Funz.getIstance().ext.add(fe.modname);
        seg.addIstruzione("lea", rbx, "["+fe.modname+"]");
        seg.addIstruzione("mov", "["+rax+"]", rbx);
        
        for(int i=0; i<types.length; i++){//mantiene l'ordine di salvataggio
            if(types[i].explicit || types[i].shadow){
                if(ffm[i].get!=null || ffm[i].set!=null)
                    throw new CodeException("Funzioni accesso non valide");
                continue;//non bisogna generare vtable
            }
            rd=th.vt.index(types[i].getIdent(), true);//L'elemento sta in vtable (almeno il get)
            if(types[i].ghost){
                if(types[i].read){
                    if(ffm[i]==null || ffm[i].get==null || ffm[i].set!=null)
                        throw new CodeException("Funzioni accesso errate a membro ghost");
                    seg.addIstruzione("lea",rbx,"["+Meth.modName(ffm[i].get, vparams)+"]");
                    seg.addIstruzione("mov","["+rax+"+"+rd+"]",rbx);
                }
                else{
                    if(ffm[i]==null || ffm[i].get==null || ffm[i].set==null)
                        throw new CodeException("Funzioni accesso errate a membro ghost");
                    seg.addIstruzione("lea",rbx,"["+Meth.modName(ffm[i].get, vparams)+"]");
                    seg.addIstruzione("mov","["+rax+"+"+rd+"]",rbx);
                    seg.addIstruzione("lea",rbx,"["+Meth.modName(ffm[i].set, vparams)+"]");
                    seg.addIstruzione("mov","["+rax+"+"+(rd+Info.pointerdim)+"]",rbx);
                }
            }
            else if(types[i].override){
                th.checkCorrectOverride(types[i], false);
                if(ffm[i]!=null){
                    if(ffm[i].get!=null){
                        seg.addIstruzione("lea",rbx,"["+Meth.modName(ffm[i].get, vparams)+"]");
                        seg.addIstruzione("mov","["+rax+"+"+rd+"]",rbx);
                    }
                    if(ffm[i].set!=null){
                        seg.addIstruzione("lea",rbx,"["+Meth.modName(ffm[i].set, vparams)+"]");
                        seg.addIstruzione("mov","["+rax+"+"+(rd+Info.pointerdim)+"]",rbx);
                    }
                }
            }
            else{
                String[] yte=templateNames();
                if(types[i].read){
                    if(ffm[i]!=null && ffm[i].set!=null)
                        throw new CodeException("Funzione set in membro read");
                    if(ffm[i]!=null && ffm[i].get!=null)
                        seg.addIstruzione("lea",rbx,"["+Meth.modName(ffm[i].get, vparams)+"]");
                    else
                        seg.addIstruzione("lea", rbx, "["+Meth.generate(types[i], nome, true, modulo, vparams, yte)+"]");
                    seg.addIstruzione("mov","["+rax+"+"+rd+"]",rbx);
                }
                else{
                    if(ffm[i]!=null && ffm[i].get!=null)
                        seg.addIstruzione("lea",rbx,"["+Meth.modName(ffm[i].get, vparams)+"]");
                    else
                        seg.addIstruzione("lea", rbx, "["+Meth.generate(types[i], nome, true, modulo, vparams, yte)+"]");
                    seg.addIstruzione("mov","["+rax+"+"+rd+"]",rbx);
                    if(ffm[i]!=null && ffm[i].get!=null)
                        seg.addIstruzione("lea",rbx,"["+Meth.modName(ffm[i].get, vparams)+"]");
                    else
                        seg.addIstruzione("lea", rbx, "["+Meth.generate(types[i], nome, false, modulo, vparams, yte)+"]");
                    seg.addIstruzione("mov","["+rax+"+"+(rd+Info.pointerdim)+"]",rbx);
                }
            }
        }
        seg.addIstruzione("leave", null, null);
        seg.addIstruzione("ret", null, null);
        //Fine inizializzatore vtable
        //Generazione effettiva delle funzioni
        //i controlli sono già stati effettuati
        for(int i=0; i<types.length; i++){
            if(types[i].explicit || types[i].shadow)
                continue;
            if(types[i].ghost){
                ffm[i].get.toCode(seg, varSt, env, vparams);
                if(ffm[i].set!=null)
                    ffm[i].set.toCode(seg, varSt, env, vparams);
            }
            else if(types[i].override){
                if(ffm[i]!=null){
                    if(ffm[i].get!=null)
                        ffm[i].get.toCode(seg, varSt, env, vparams);
                    if(ffm[i].set!=null)
                        ffm[i].set.toCode(seg, varSt, env, vparams);
                }
            }
            else{
                //rd=th.getElement(types[i].getIdent(), false);
                if(ffm[i]!=null && ffm[i].get!=null)
                    ffm[i].get.toCode(seg, varSt, env, vparams);
                /*
                else
                    generateAccessFunz(seg, types[i], true, rd, vparams);
                */
                if(ffm[i]!=null && ffm[i].set!=null)
                    ffm[i].set.toCode(seg, varSt, env, vparams);
                /*
                else if(!types[i].read)
                    generateAccessFunz(seg, types[i], false, rd, vparams);
                */
            }
        }
    }
    public void println(int i){
        String h=Info.cSpace(i);
        System.out.println(h+"Type: "+nome);
        System.out.println(h+"Dichiarazioni:");
        for(Membro e:types){
            System.out.println(h+" "+e.toString());
        }
        System.out.println(h+"FunzMem:");
    }
    public String getName(){
        return nome;
    }
    public Membro[] getDich(){
        return types;
    }
    public FMorg[] getMems(){
        return ffm;
    }
    public TypeName extend(){
        if(ext==null)
            return null;
        return ext;
    }
    public Template[] templates(){
        return tt;
    }
    public String[] templateNames(){
        String[] n=new String[tt.length];
        for(int i=0; i<tt.length; i++){
            n[i]=tt[i].getIdent();
        }
        return n;
    }
    public String modulo(){
        return modulo;
    }
    public boolean classExplicit(){
        return explicit;
    }
    public boolean hasVTable()throws CodeException{
        return !explicit;
    }
}
