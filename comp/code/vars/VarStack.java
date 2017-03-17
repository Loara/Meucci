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
        public final boolean var;
        public boolean destroyable;
        public final String name;
        public final int pos;//posizione RELATIVA a rbp (posizione reale = rbp - pos)
        public final int allocatedSpace;//di quanto è stato decrementato l'rsp per allocare spazio
        public final TypeElem type;
    }
    private final ArrayList<VarEle> al;
    private int rrsp, maxdim, dimargs;//rsp = rbp - rrsp => rrsp = rbp - rsp
    private final Accumulator acc;
    private boolean remainAlign(int cp, int dim, int pd){
        //ritorna true se dopo l'aggiunta rimane allineato
        //Bisogna stare attenti in quanto cp è l'rrsp e non l'rsp
        //anche se sono entrambi alineati allo stesso modo.
        
        //Per essere allineato sia cp che cp+dim devono giacere nello stesso blocco
        //di dimensione pd. se k*pd <= cp < (k+1)*pd allora k*pd <= cp+dim <= (k+1)*pd
        if(dim == 0)
            return true;
        int rcp = (cp & (pd-1));// == cp % pd essendo pd potenza di 2
        return dim <= (pd-rcp);
    }
    /*
    Alloca spazio per variabili primitive, Per allocare spazio con :stack, utilizzare
    allocAlign
    ritorna la quantità di memoria EFFETTIVAMENTE incrementata
    */
    private int incPOSAlign(int dim){
        int cpos=rrsp;
        int pd =Info.pointerdim;
        if(dim <= pd){
            if(remainAlign(rrsp, dim, pd)){
                rrsp+=dim;
            }
            else{
                //prima aggiungi lo spazio vuoto sotto, poi inserisci la variabile
                //la variabile infatti non è richiesta essere allineata
                rrsp += Info.alignConv(rrsp);
                rrsp += dim;
            }
        }
        else{
            //La cima deve essere allineata (pensa agli xmm)
            rrsp += dim;
            rrsp += Info.alignConv(rrsp);
        }
        return rrsp - cpos;
    }
    private void decPOS(int dim){
        rrsp+=dim;
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
        maxdim=0;
        initArgs(args);
        rrsp = 0;//all'inizio rsp punta esattamente a ciò che punta rbp
        this.acc=acc;
    }
    /**
     * Come il precedente, solo no parametri passati per stack
     * @param acc
     * @throws CodeException 
     */
    public VarStack(Accumulator acc)throws CodeException{
        al=new ArrayList<>();
        maxdim=0;
        initArgs(null);
        rrsp = 0;
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
        int len = args.length;
        /*
        rrsp non ancora utilizzato
        Sia i l'i-esimo parametro (da sinistra) passato alla funzione.
        Allora la sua posizione assoluta è
        rbp + 8*(len - i - 1) + 16 = rbp + 8*(len -i + 1)
        la posizione relativa diviene allora
        8*(i - len - 1).
        */
        for (int i=0; i<len; i++) {
            dimargs+=8;
            VarEle ve=new VarEle(true, false, args[i].getIdent()
                    , args[i].dich.getRType(), Info.pointerdim * (i-len-1), Info.pointerdim);
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
        VarEle ve=new VarEle(true, false, ident, type, rrsp, adim);//si possono sempre modificare
        if(rrsp>maxdim)
            maxdim=rrsp;
        al.add(ve);
    }
    /**
     * Alloca spazio (in byte) con inizio allineato, da usare ad esempio 
     * su blocchi creati tramite :stack
     * @param i 
     * @param destructor 
     * @return Valore da Sottrarre a rbp per ottenere la risorsa
     * @throws comp.code.CodeException
     */
    public VarEle allocAlign(int i, boolean destructor)throws CodeException{
        if(i<=0)
            throw new CodeException("Dimensione nulla");
        int initRSP=rrsp;
        rrsp += i;
        rrsp += Info.alignConv(rrsp);
        VarEle ret = new VarEle(false, destructor, "alloca", null, rrsp, rrsp-initRSP);
        al.add(ret);
        if(rrsp>maxdim)
            maxdim=rrsp;
        return ret;//ritorna ret e non la posizione per poter gestire meglio le eccezioni
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
    public String varInfo(VarEle ve)throws CodeException{
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
        int sc = -ve.pos;//posizione = rbp - ve.pos
            if(sc == 0)
                r += " [rbp]";//impossibile, ma non si sa mai
            else if(sc > 0)
                r+=" [rbp+"+sc+"]";
            else
                r+=" [rbp"+sc+"]";
        return r;
    }
    /*
    Come VarInfo, solo non si interessa della dimensione del blocco (ideale per blocchi
    allocati con :stack)
    */
    public String varPos(VarEle ve){
        String r;
        int sc = -ve.pos;//posizione = rbp - ve.pos
            if(sc == 0)
                r = "[rbp]";//impossibile, ma non si sa mai
            else if(sc > 0)
                r = "[rbp+"+sc+"]";
            else
                r = "[rbp"+sc+"]";
        return r;
    }
    @Override
    public void getVar(Segmenti text, String ident, Register reg)throws CodeException{
        VarEle ve=get(ident);
        if(ve.type.xmmReg())
            throw new CodeException("Valore reale");
        String u = varInfo(ve);
        getGVar(text, u, reg, ve.type.realDim());
    }
    @Override
    public void setVar(Segmenti text, String ident, Register reg)throws CodeException{
        VarEle ve=get(ident);
        if(ve.type.xmmReg())
            throw new CodeException("Valore reale");
        String u = varInfo(ve);
        setGVar(text, u, reg, ve.type.realDim());
    }
    @Override
    public void xgetVar(Segmenti text, String ident, XReg reg)throws CodeException{
        VarEle ve=get(ident);
        if(!ve.type.xmmReg())
            throw new CodeException("Varore reale");
        String u = varInfo(ve);
        getXVar(text, u, reg);
    }
    @Override
    public void xsetVar(Segmenti text, String ident, XReg reg)throws CodeException{
        VarEle ve=get(ident);
        if(!ve.type.xmmReg())
            throw new CodeException("Valore non reale");
        String u = varInfo(ve);
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
            al.add(new VarEle(false, false, "block", null, rrsp, 0));
        }
        catch(CodeException e){}//inutilizzato
    }
    private void distruggi(VarEle e, Segmenti seg)throws CodeException{
        String u = varPos(e);//u è il primo byte dell'oggetto
        int pr=acc.prenota();
        seg.addIstruzione("lea", acc.getReg(pr).getReg(), u);
        seg.addIstruzione("push", acc.getReg(pr).getReg(), null);
        seg.addIstruzione("mov", acc.getReg(pr).getReg(), u);
        seg.addIstruzione("call", "["+acc.getReg(pr).getReg()+"]", null);
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
