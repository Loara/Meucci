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
package comp.code;

import comp.code.vars.Variabili;
import comp.general.Info;
import comp.general.Stack;
import comp.parser.Espressione;
import comp.parser.expr.IdentEle;
import comp.parser.Membro;
import comp.parser.TypeDef;
import comp.parser.TypeName;
import comp.parser.expr.FunzExpr;
import comp.parser.expr.IdentArray;
import comp.parser.expr.NumExpr;
import comp.parser.template.NumDich;
import java.util.Objects;

/**
 * Questa classe rappresenta le informazioni sui tipi deducibili dagli header.
 * 
 * Struttura in memoria di un tipo (primitivo o no).
 * +-----------------------------+
 * |puntatore a vtable contenente|
 * |le funzioni membro           |
 * +-----------------------------+
 * |dati generali                |
 * +-----------------------------+
 * 
 * ....
 * 
 * AGGIORNAMENTO: se un membro NON è explicit, viene generata una funzione di accesso
 * automatica (in pratica viene chiamata sempre una funzione)
 * 
 * TypeElem non contiene membri override: tale parametro ha importanza solo per compilazione
 * @author loara
 */
public class TypeElem {
    public final String name;
    public final Membro[] subtypes;
    private final int[] allineamenti;//gli offset dei sottotipi allineati in memoria
    public final TypeName extend;
    public final VTable vt;
    public final boolean primitive, reference, number, external, 
            explicit;//se è definita in un altro modulo, explicit se non ha vtable e membri espliciti
    public final boolean template;//è true se e solo se è un parametro template
    private int dim;
    public TypeElem(TypeDef e, boolean ex)throws CodeException{
        primitive=false;
        reference=true;
        number=false;
        template=false;
        explicit=e.classExplicit();
        name=e.getName();
        Stack<Membro> bbi=new Stack<>(Membro.class);
        for(Membro tt:e.getDich()){
            if(!tt.override)
                bbi.push(tt);//escludere gli override
        }
        subtypes=bbi.toArray();
        allineamenti=new int[subtypes.length];
        extend=e.extend();
        vt=new VTable(subtypes, name, extend);
        external=ex;
        dim=-1;
        if(explicit && extend==null && subtypes.length==0)
            throw new CodeException("Tipo esplicito "+name+" vuoto");
    }
    public TypeElem(String n, TypeName e, Membro[] s, boolean ex, boolean explicit){
        name=n;
        extend=e;
        subtypes=s;
        template=false;
        allineamenti=new int[s.length];
        primitive=false;
        reference=true;
        this.explicit=explicit;
        number=false;
        external=ex;
        vt=new VTable(s, n, e);
        dim=-1;
    }
    /**
     * Per dichiarazioni template
     * @param n
     * @param e
     * @param s
     * @param ex
     * @param ref
     * @param num 
     * @throws comp.code.CodeException 
     */
    public TypeElem(String n, TypeName e, Membro[] s, boolean ex, boolean ref, boolean num)
    throws CodeException{
        template=true;
        if(num && ref)
            throw new CodeException("Parametri errati");
        name=n;
        extend=e;
        Stack<Membro> m=new Stack<>(Membro.class);
        for(int i=0; i<s.length; i++){//meglio non convertirlo in for-loop
            //per mantenere l'ordine
            if(!s[i].override)
                m.push(s[i]);
        }
        subtypes=m.toArray();
        allineamenti=new int[s.length];
        explicit=true;//Da ignorare per template
        if(ref){
            reference=true;
            primitive=false;
            number=false;
        }
        else if(num){
            reference=false;
            number=true;
            primitive=true;
        }
        else{
            reference=false;
            primitive=false;
            number=false;
        }
        external=ex;
        vt=new VTable(s, n, e);
        dim=-1;
    }
    TypeElem(String name){//tipo primitivo
        this.name=name;
        template=false;
        subtypes=new Membro[0];
        allineamenti=new int[0];
        extend=null;
        primitive=true;
        reference=name.equals(":null");//sono anche reference
        number=Info.varNum(name);
        dim=-1;
        external=false;
        explicit=true;
        vt=null;
    }
    /**
     * Da aggiustare
     * @param v
     * @throws CodeException 
     */
    public void allinea(boolean v)throws CodeException{//si occupa anche della dimensione
        int ofs;//indica dove sarebbe ideale mettere il dato membro
        if(extend!=null){
            TypeElem ext=Types.getIstance().find(extend, v);
            ofs=ext.dimension(v);
        }else{
            if(explicit)
                ofs=0;//no vtable
            else
                ofs=Info.pointerdim;//vtable
        }
        if(allineamenti.length==0){
            dim=ofs;
            return;
        }
        for(int i=0; i<allineamenti.length; i++){
            if(subtypes[i].ghost){
                allineamenti[i]=-1;
                continue;
            }
            int rd=Types.getIstance().find(subtypes[i].getType(), v).realDim();
            if(subtypes[i].packed!=null){
                if(!(subtypes[i].packed instanceof NumDich))
                    throw new CodeException("La lunghezza di "+subtypes[i].getIdent()
                            +" in "+name+" deve essere una costante");
                allineamenti[i]=ofs+Info.alignConv(ofs);//sempre allineato;
                ofs=allineamenti[i]+(rd*(int)((NumDich)subtypes[i].packed).getNum());
                //Errore, da gestire opportunamente
            }
            else{
                int next=ofs+rd;
                if(rd<=8){
                    if((next & 7)>(ofs & 7)){//correttamente allineato
                        allineamenti[i]=ofs;
                    }
                    else{
                        allineamenti[i]=ofs+Info.alignConv(ofs);
                    }
                }
                else
                    allineamenti[i]=ofs+Info.alignConv(ofs);
                ofs=allineamenti[i]+rd;
            }
        }
        dim=ofs;
    }
    public boolean xmmReg(){
        return Info.xmmReg(name);
    }
    @Override
    public boolean equals(Object e){
        if(!(e instanceof TypeElem))
            return false;
        return name.equals(((TypeElem)e).name);
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.name);
        return hash;
    }
    public int dimension(boolean v)throws CodeException{
        if(template)
            throw new CodeException("Impossibile calcolarne la dimensione");
        if(primitive)
            throw new CodeException("Tipo primitivo");
        if(dim==-1)
            allinea(v);
        return dim;
    }
    public boolean hasDim(){
        return !template && !primitive;
    }
    public int alignDimension(boolean v)throws CodeException{
        int d=dimension(v);
        return d+Info.alignConv(d);
    }
    public int realDim()throws CodeException{
        if(reference)
            return 8;
        else if(template)
            throw new CodeException("Impossibile risalire alla dimensione di un template");
        if(name.equals(":null"))
            return Info.pointerdim;
        int ind=Info.realDim(name);
        if(ind!=-1){
            return ind;
        }
        else throw new CodeException("Impossibile risalire alla vera dimensione di "+name);
    }
    public boolean hasRealDim(){
        return reference || !template;
    }
    
    /**
     * Ritorna il primo membro non override con quel nome
     * @param name
     * @param validate
     * @return
     * @throws CodeException 
     */
    
    public Membro information(String name, boolean validate)throws CodeException{
        Stack<Membro> m=new Stack<>(Membro.class);
        information0(name, validate, m);
        if(m.size()==1)
            return m.pop();
        else
            throw new CodeException("Trovati "+m.size()+" diverse interpretazioni di "
            +name);
    }
    /**
     * Come {@code information}, ma se non trova niente ritorna null e non genera errore
     * @param name
     * @param validate
     * @return
     * @throws CodeException 
     */
    public Membro informationSafe(String name, boolean validate)throws CodeException{
        Stack<Membro> m=new Stack<>(Membro.class);
        information0(name, validate, m);
        switch (m.size()) {
            case 1:
                return m.pop();
            case 0:
                return null;
            default:
                throw new CodeException("Trovati "+m.size()+" diverse interpretazioni di "
                        +name);
        }
    }
    private void information0(String name, boolean v, Stack<Membro> mems)throws CodeException{
        for(Membro d:subtypes){
            if(name.equals(d.getIdent()))
                mems.push(d);
            //Non è necessario controllare se siano override, in quanto vengono esclusi dal costruttore
        }
        if(extend!=null)
            Types.getIstance().find(extend, v).information0(name, v, mems);
    }
            /**
             * Se over è override di un membro, determina se l'override è stato
             * compilato correttamente.
             * @param over
     * @param v
             * @throws CodeException 
             */
    public void checkCorrectOverride(Membro over, boolean v)throws CodeException{
        Membro il=information(over.getIdent(), v);
        boolean correct=(il.getType().equals(over.getType()))
                &&(il.read==over.read)
                &&(il.shadow==over.shadow);
        if(!correct) throw new CodeException("Override illegale");
        if(il.explicit) throw new CodeException("Override illegale");
        if(il.shadow) throw new CodeException("Override illegale");
        correct=il.compatible(over.params);
        if(!correct) throw new CodeException("Override illegale");
    }
    public boolean hasVTable()throws CodeException{
        return !explicit;//Per via del distruttore
    }
    public boolean ifEstende(TypeElem tp, boolean v)throws CodeException{
        if(name.equals("void")||tp.name.equals("void"))
            return false;
        if(name.equals(tp.name))
            return true;
        if(name.equals(":null") && tp.isReference())
            return true;
        if(name.equals(":null")&& tp.name.equals("pt"))
            return true;
        if(extend==null)
            return false;
        return Types.getIstance().find(extend, v).ifEstende(tp, v);
    }
    public boolean ifEstende(TypeName t, boolean v)throws CodeException{
        return ifEstende(Types.getIstance().find(t, v), v);
    }
            /**
             * Se ritorna falso, non vuol dire che è primitivo, in quanto può essere
             * un template non definito.
             * @return 
             */
    public boolean isReference(){
        return reference;
    }
    public boolean isPrimitive(){
        return primitive;
    }
    public boolean isNum(){
        return number;
    }
    public boolean isTemplate(){
        return template;
    }
    public boolean isUnsignedNum(){
        return Info.unsignNum(name);//Un type-template num non può
        //essere (attualmente) unsigned
    }
    //Da modificare, in quanto bisogna vedere se il tipo in cui è definito è esterno o no
    public void canRead(String el, boolean v)throws CodeException{
        for(Membro m:subtypes){
            if(m.dich.getIdent().equals(el)){
                if(external && m.shadow)
                    throw new CodeException("Impossibile leggere "+el);
                return;
            }
        }
        if(extend!=null)
            Types.getIstance().find(extend, v).canWrite(el, v);
        else
            throw new CodeException("Membro non trovato: "+el);
    }
    public void canWrite(String el, boolean v)throws CodeException{
        for(Membro m:subtypes){
            if(m.dich.getIdent().equals(el)){
                if(external && m.shadow && m.read)
                    throw new CodeException("Impossibile scrivere "+el);
                return;
            }
        }
        if(extend!=null)
            Types.getIstance().find(extend, v).canWrite(el, v);
        else
            throw new CodeException("Membro non trovato: "+el+" in "+name);
    }
    public boolean directAccess(String ident, boolean dd)throws CodeException{
        for(Membro m:subtypes){
            if(m.dich.getIdent().equals(ident)){
                return !m.ghost && ((m.explicit || m.shadow) || (!dd && !external));
            }
        }
        if(extend!=null)
            return Types.getIstance().find(extend, false).directAccess(ident, dd);
        else
            throw new CodeException("Tipo non trovato");
    }
            /**
             * Stampa in {@code text} e {@code data} la lettura di un elemento. 
             * Non è un validate (richiede segmenti)
             * @param text
             * @param var
             * @param env
             * @param elem
             * @param acc
             * @param dd doubledot
             * @return
             * @throws CodeException 
             */
    public TypeElem getTypeElement(Segmenti text, Variabili var, Environment env, 
            IdentEle elem, Accumulator acc, boolean dd) throws CodeException{
        if(primitive)
            throw new CodeException(name+" e' un tipo primitivo");
        //Controlla se può leggerlo
        canRead(elem.getIdent(), false);
        Membro m=information(elem.getIdent(), false);
        TypeElem ter=Types.getIstance().find(m.getType(), false);
                
        if(directAccess(m.getIdent(), dd)){//vedere file doubledot
            int sc=getElement(elem.getIdent(), false);//se è ghost genera errore
            if(m.packed!=null){
                int rd=acc.saveAccumulator();
                String pointer;
                Espressione inded=elem.getVals()[0];
                if(inded instanceof IdentArray && ((IdentArray)inded).isNum()){
                    long v=((NumExpr)((IdentArray)inded).getEsp()).value();
                    v *= ter.realDim();
                    pointer="["+acc.getReg(rd).getReg()+"+"+(v+sc)+"]";
                }
                else{
                    inded.toCode(text, var, env, acc);
                    pointer="["+acc.getReg(rd).getReg()+"+"+acc.getAccReg().getReg()+"*"
                            +ter.realDim()+"+"+sc+"]";
                }
                if(ter.xmmReg()){
                    text.addIstruzione("movds", acc.getXAccReg().getReg(), pointer);
                }
                else{
                    text.addIstruzione("mov", acc.getReg(rd).getReg(ter.realDim()), pointer);
                }
                acc.restoreAccumulator(rd);
            }
            else{
                if(ter.xmmReg()){
                    text.addIstruzione("movds",acc.getXAccReg().getReg(),"["+
                            acc.getAccReg().getReg()+"+"+sc+"]");
                }
                else{
                    text.addIstruzione("mov",acc.getAccReg().getReg(ter.realDim())
                            ,"["+acc.getAccReg().getReg()+"+"+sc+"]");
                }
            }
        }
        else{
            acc.pushAll(text);//il pushall và prima, onde evitare corruzione stack
            int tic=acc.prenota();//puntatore vtable
            text.addIstruzione("mov",acc.getReg(tic).getReg(),
                    "["+acc.getAccReg().getReg()+"]");
            int sc2=vt.getReadAcc(elem, var, env);
            if(sc2==-1)
                throw new CodeException("Membro non trovato");
            text.addIstruzione("push",acc.getAccReg().getReg(), null);//indirizzo oggetto
            //i valori di default tanto sono già stati aggiunti
            //acc.libera(tic)lanciato automaticamente all'interno di perfCall
            FunzExpr.perfCall(tic, sc2, ter, elem.getVals(), text, var, env, acc);
        }
        return ter;
    }
    /**
     * input è il codice del registro (memorizzato tramite Accumulator) da cui prendere l'input.
     * 
     * <b>Viene distrutto dopo l'uso</b>
     * @param text
     * @param var
     * @param env
     * @param elem
     * @param input
     * @param acc
     * @param dd
     * @throws CodeException 
     */
    public void setValueElement(Segmenti text, Variabili var, Environment env, 
            IdentEle elem, int input, Accumulator acc, boolean dd)throws CodeException{
        if(primitive)
            throw new CodeException(name+" e' un tipo primitivo");
        canWrite(elem.getIdent(), false);
        Membro m=information(elem.getIdent(), false);
        TypeElem ter=Types.getIstance().find(m.getType(), false);
        if(directAccess(m.getIdent(), dd)){
            if(ter.xmmReg()){
                throw new CodeException("Tipi incompatibili: "+elem.getIdent()+" è reale");
            }
            int sc=getElement(elem.getIdent(), false);//se è ghost genera errore
            if(m.packed!=null){
                int rd=acc.saveAccumulator();
                String pointer;
                Espressione inded=elem.getVals()[0];
                if(inded instanceof IdentArray && ((IdentArray)inded).isNum()){
                    long v=((NumExpr)((IdentArray)inded).getEsp()).value();
                    v *= ter.realDim();
                    pointer="["+acc.getReg(rd).getReg()+"+"+(v+sc)+"]";
                }
                else{
                    inded.toCode(text, var, env, acc);
                    pointer="["+acc.getReg(rd).getReg()+"+"+acc.getAccReg().getReg()+"*"
                            +ter.realDim()+"+"+sc+"]";
                }
                text.addIstruzione("mov", pointer, 
                        acc.getReg(input).getReg(ter.realDim()));
                acc.restoreAccumulator(rd);
            }
            else{
                text.addIstruzione("mov", "["+acc.getAccReg().getReg()+"+"+sc+"]"
                            , acc.getReg(input).getReg(ter.realDim()));
            }
            acc.libera(input);
        }
        else{
            acc.pushAll(text, new int[]{input}, new int[0]);
            text.text.flush();
            int tic=acc.prenota();
            text.addIstruzione("mov",acc.getReg(tic).getReg(),
                    "["+acc.getAccReg().getReg()+"]");
            int sc2=vt.getWriteAcc(elem, var, env);
            if(sc2==-1)
                throw new CodeException("Membro "+elem.getIdent()+" in "+name+" non trovato");
            text.addIstruzione("push",acc.getAccReg().getReg(),null);//indirizzo oggetto
            text.addIstruzione("push",acc.getReg(input).getReg(), null);//valore da settare, sempre qword
            FunzExpr.perfCall(tic, sc2, Types.getIstance().find(new TypeName("void"), 
                    false), elem.getVals(), text, var, env, acc);
        }
                
    }
    public void setXValueElement(Segmenti text, Variabili var, Environment env, 
        IdentEle elem, int input, Accumulator acc, boolean dd)throws CodeException{
        if(primitive)
            throw new CodeException(name+" e' un tipo primitivo");
        canWrite(elem.getIdent(), false);
        Membro m=information(elem.getIdent(), false);
        TypeElem ter=Types.getIstance().find(m.getType(), false);
        if(directAccess(m.getIdent(), dd)){
            int sc=getElement(elem.getIdent(), false);
            if(!ter.xmmReg()){
                throw new CodeException("Tipi incompatibili: "+elem.getIdent()+
                        " non è reale");
            }
            if(m.packed!=null){
                int rd=acc.saveAccumulator();
                String pointer;
                Espressione inded=elem.getVals()[0];
                if(inded instanceof IdentArray && ((IdentArray)inded).isNum()){
                    long v=((NumExpr)((IdentArray)inded).getEsp()).value();
                    v *= 8;
                    pointer="["+acc.getReg(rd).getReg()+"+"+(v+sc)+"]";
                }
                else{
                    inded.toCode(text, var, env, acc);
                    pointer="["+acc.getReg(rd).getReg()+"+"+acc.getAccReg().getReg()+"*8"
                            +"+"+sc+"]";
                }
                text.addIstruzione("movds", pointer, acc.getXReg(input).getReg());
                acc.restoreAccumulator(rd);
            }
            else{
                text.addIstruzione("movsd", "["+acc.getAccReg().getReg()+"+"+sc+"]"
                        , acc.getXReg(input).getReg());
            }
        }
        else{
            acc.pushAll(text, new int[0], new int[]{input});
            int tic=acc.prenota();
            text.addIstruzione("mov",acc.getReg(tic).getReg(),
                    "["+acc.getAccReg().getReg()+"]");
            int sc2=vt.getWriteAcc(elem, var, env);
            if(sc2==-1)
                throw new CodeException("Membro non ritrovato");
            text.addIstruzione("push",acc.getAccReg().getReg(),null);//indirizzo oggetto
            text.addIstruzione("push",acc.getAccReg().getReg(), null);//valore da settare
            text.addIstruzione("movsd", "[rsp]", acc.getXReg(input).getReg());
            FunzExpr.perfCall(tic, sc2, ter, elem.getVals(), text, var, env, acc);
        }
                
    }
    public int getElement(String name, boolean v)throws CodeException{
        if(primitive)
            throw new CodeException(this.name+" è primitivo");
        if(dim==-1)
            allinea(v);
        for(int i=0; i<subtypes.length; i++){
            if(subtypes[i].dich.getIdent().equals(name)){
                //Gli override vengono esclusi dal costruttore
                if(subtypes[i].ghost)
                    throw new CodeException(subtypes[i].dich.getIdent()+" è ghost");
                return allineamenti[i];
            }
        }
        if(extend!=null){
            TypeElem aa=Types.getIstance().find(extend, v);
            int j=aa.getElement(name, v);
            if(j!=-1)
                return j;
        }
        return -1;
    }
}
