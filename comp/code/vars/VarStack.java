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
import comp.code.Register;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.XReg;
import comp.general.Info;
import comp.parser.FunzParam;
import comp.parser.TypeName;
import java.util.ArrayList;

/**
 * Variabili stack, NOTA: prima si incrementa rsp, poi si aggiunge la variabile
 * Un VarStack non dovrebbe essere utilizzato nel caso di validate
 * @author loara
 */
public class VarStack extends Var{
    public static class VarEle{
        public VarEle(boolean a, boolean des, String b, TypeName t, int c, int alld)
                throws CodeException{
            var=a;//se è una variabile o un blocco
            destroyable=des;
            name=b;
            pos=c;
            if(t==null){
                type=null;
            }
            else{
                type=Types.getIstance().find(t, false);
            }
            if(alld<0)
                allocatedSpace=-alld;
            else
                allocatedSpace=alld;
        }
        public final boolean var, destroyable;
        public final String name;
        public final int pos, allocatedSpace;//di quanto è stato decrementato l'rsp per allocare spazio
        public final TypeElem type;
    }
    private final ArrayList<VarEle> al;
    private int rsp, rbp, maxdim, dimargs;
    private final int topstack=0;//non è importante quanti vale, purchè sia multiplo di 8
    private final Accumulator acc;
    private void incPOS(int dim){//cresce verso il basso
        rsp-=dim;
    }
    /*
    Alloca spazio per variabili primitive, Per allocare spazio con :static, utilizzare
    allocAlign
    ritorna la quantità di memoria utilizzata in modulo
    */
    private int incPOSAlign(int dim){
        int cpos=rsp;
        if(dim<=8){
            if(((cpos - dim) & 7) <= (cpos & 7) || ((cpos & 7) ==0)){
                //mantiene allineamento
                rsp-=dim;
            }
            else{
                rsp-=Info.decConv(rsp);
                rsp-=dim;
            }
        }
        else{
            rsp-=Info.decConv(rsp);
            rsp-=Info.alignConv(dim)+dim;
        }
        return cpos-rsp;
    }
    private void decPOS(int dim){
        rsp+=dim;
    }
    /*
    rbp=8 byte 64 bit
    ebp=4 byte 32 bit
    bp=2 byte 16 bit
    solo questi valori sono ammessi
    rsp punta al primo valore UTILIZZATO, non libero
    */
    public VarStack(FunzParam[] args, Accumulator acc)throws CodeException{
        al=new ArrayList<>();
        rsp=topstack;
        maxdim=0;
        rbp=0;
        initArgs(args);
        incPOS(8);//return adress
        incPOS(8);//rbp
        rbp=rsp;//vedere sopra
        this.acc=acc;
    }
    /**
     * Come il precedente, solo no parametri passati per stack
     * @param acc
     * @throws CodeException 
     */
    public VarStack(Accumulator acc)throws CodeException{
        al=new ArrayList<>();
        rsp=topstack;
        maxdim=0;
        rbp=0;
        initArgs(null);
        incPOS(8);
        incPOS(8);
        rbp=rsp;
        this.acc=acc;
    }
    /**
     * Attenzione!!! in 64-bit è possibile effettuare il push solo per dimensioni di
     * 8 byte: i 4 e 2 byte degli argomenti (e solo degli argomenti, in quanto 
     * unici ad effettuare push) vanno gestiti come 8 byte
     * @param args
     * @throws CodeException 
     */
    private void initArgs(FunzParam[] args)throws CodeException{
        dimargs=0;
        if(args==null)
            return;
        for (FunzParam arg : args) {
            incPOS(8);
            dimargs+=8;
            VarEle ve=new VarEle(true, false, arg.getIdent(), arg.dich.getRType(), rsp, 8);
            al.add(ve);
        }
    }
    /**
     * Aggiunge variabile. Controlla anche l'allineamento
     * @param type
     * @param ident
     * @throws comp.code.CodeException
     */
    public void addVar(TypeName type, String ident)throws CodeException{
        int adim =incPOSAlign(Types.getIstance().find(type, false).realDim());//utilizzare solo in toCode
        VarEle ve=new VarEle(true, false, ident, type, rsp, adim);//si possono sempre modificare
        int dimInter=rbp-rsp;
        if(dimInter>maxdim)
            maxdim=dimInter;
        al.add(ve);
    }
    /**
     * Alloca spazio (in byte) allineato nello stack. Dato che lo stack cresce verso il basso,
     * mentre l'accesso della memoria è verso l'alto, solo l'ultimo byte deve essere allineato.
     * @param i 
     * @param destructor 
     * @return Valore da Sottrarre a rbp per ottenere la risorsa
     * @throws comp.code.CodeException
     */
    public int allocAlign(int i, boolean destructor)throws CodeException{
        if(i<=0)
            return -1;
        int initRSP=rsp;
        rsp-=i;
        rsp-=Info.decConv(rsp);
        al.add(new VarEle(false, destructor, "alloca", null, rsp, rsp-initRSP));
        int dimInter=rbp-rsp;//positivo
        if(dimInter>maxdim)
            maxdim=dimInter;
        return dimInter;
    }
    /**
     * ritorna la posizione di {@code te} rispetto all'rbp
     * (per la posizione assoluta và aggiunto all'rbp;
     * @param te
     * @return 
     */
    private int relativePOS(VarEle te){
        return te.pos-rbp;
    }
    @Override
    public boolean isIn(String ident){
        return al.stream().anyMatch((v) -> (v.var&&v.name.equals(ident)));
    }
    @Override
    public TypeElem type(String ident)throws CodeException{
        VarEle f=null;
        for(VarEle v:al){
            if(v.var&&v.name.equals(ident)){
                f=v;
                break;
            }
        }
        if(f==null)
            throw new CodeException("Errore: "+ident+" non dichiarata.");
        return f.type;
    }
    public VarEle get(String ident)throws CodeException{
        VarEle f=null;
        for(int i=al.size()-1; i>=0; i--){//meglio
            if(al.get(i).var&&al.get(i).name.equals(ident)){
                f=al.get(i);
                break;
            }
        }
        if(f==null)
            throw new CodeException("Variabile sconosciuta :"+ident);
        return f;
    }
    public static int pushDim(int adim){
        return 8;
    }
    public String varInfo(String n, int d)throws CodeException{
        VarEle ve=get(n);
        if(ve.type.realDim()!=d)
            throw new CodeException("Dimensioni errate "+ve.type.name+" "+ve.type.realDim()+" "+d);
        String r;
        switch(ve.type.realDim()){
            case 1:
                r="byte";
                break;
            case 2:
                r="word";
                break;
            case 4:
                r="dword";
                break;
            case 8:
                r="qword";
                break;
            default:
                r="";
        }
        int sc=ve.pos-rbp;
            if(sc>=0)
                r+=" [rbp+"+sc+"]";
            else
                r+=" [rbp"+sc+"]";
        return r;
    }
    @Override
    public void getVar(Segmenti text, String ident, Register reg)throws CodeException{
        VarEle ve=get(ident);
        if(ve.type.xmmReg())
            throw new CodeException("Varore reale");
        String u;
        int i=relativePOS(ve);
        if(i>=0)
            u="[rbp+"+i+"]";
        else
            u="[rbp"+i+"]";
        getGVar(text, u, reg, ve.type.realDim());
    }
    @Override
    public void setVar(Segmenti text, String ident, Register reg)throws CodeException{
        VarEle ve=get(ident);
            String u;
            int i=relativePOS(ve);
            if(i>=0)
                u="[rbp+"+i+"]";
            else
                u="[rbp"+i+"]";
        if(ve.type.xmmReg())
            throw new CodeException("Valore reale");
        setGVar(text, u, reg, ve.type.realDim());
    }
    @Override
    public void xgetVar(Segmenti text, String ident, XReg reg)throws CodeException{
        VarEle ve=get(ident);
        String u;
        int i=relativePOS(ve);
        if(i>=0)
            u="[rbp+"+i+"]";
        else
            u="[rbp"+i+"]";
        if(!ve.type.xmmReg())
            throw new CodeException("Varore reale");
        getXVar(text, u, reg);
    }
    @Override
    public void xsetVar(Segmenti text, String ident, XReg reg)throws CodeException{
        VarEle ve=get(ident);
            String u;
            int i=relativePOS(ve);
            if(i>=0)
                u="[rbp+"+i+"]";
            else
                u="[rbp"+i+"]";
        if(!ve.type.xmmReg())
            throw new CodeException("Valore non reale");
        setXVar(text, u, reg);
    }
    public int getDimArgs(){
        return dimargs;
    }
    public int internalVarsMaxDim(){
        return maxdim;
    }
    /**
     * aggiunge un blocco di variabili. tutte le variabili dichiarate in seguito rimarranno nel blocco
     * fino alla sua eliminazione
     */
    public void addBlock(){
        try{
            al.add(new VarEle(false, false, "block", null, rsp, 0));
        }
        catch(CodeException e){}//inutilizzato
    }
    private void distruggi(VarEle e, Segmenti seg)throws CodeException{
        int i=relativePOS(e);
        String u;
        if(i>=0)
            u="[rbp+"+i+"]";
        else
            u="[rbp"+i+"]";
        int pr=acc.prenota();
        seg.addIstruzione("lea", acc.getReg(pr).getReg(), u);
        seg.addIstruzione("push", acc.getReg(pr).getReg(), null);
        seg.addIstruzione("call", u, null);
        acc.libera(pr);
    }
    /**
     * rimuove un blocco e le variabili al suo interno. Per salvaguardare il loro scope
     * @param seg
     * @throws comp.code.CodeException
     */
    public void removeBlock(Segmenti seg)throws CodeException{
        VarEle e=al.remove(al.size()-1);
        decPOS(e.allocatedSpace);
        while(!(e.name.equals("block") && !e.var)){
            e=al.remove(al.size()-1);
            if(e.destroyable){
                distruggi(e, seg);
            }
            decPOS(e.allocatedSpace);
        }
    }
    /**
     * Distrugge tutto lo spazio allocato e distruttibile, senza eliminarli dallo stack.
     * Da chiamare immediatamente prima dei return o delle eccezioni.
     * @param seg
     * @throws CodeException 
     */
    public void destroyAll(Segmenti seg)throws CodeException{
        for(VarEle ve:al){
            if(ve.destroyable)
                distruggi(ve, seg);
        }
    }
}
