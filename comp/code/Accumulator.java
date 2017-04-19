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
import static comp.code.Register.*;
import static comp.code.XReg.*;
import comp.general.Info;
import comp.general.Lingue;
import comp.general.Stack;

/**
 * Gestisce i registri temporanei per lo svolgimento delle operazioni. Completata un'istruzione
 * deve essere resettato.
 * 
 * Esistono due modi per prenotare un registro:
 * Il primo utilizza semplicemente il metodo prenota, ed a ogni pushall viene pushato nello stack
 * Il secondo invece viene utilizzato per evitare di accedere alla memoria. 
 * Non viene influenzato da pushAll o popall, viene utilizzato solamente tramite VarStack
 * per ridurre accessi in memoria (in futuro tutti i Var)
 * @author loara
 */
public class Accumulator {
    private final Register[] regs;
    private final XReg[] xregs;
    private final int[] occup, xoc;
    private int acc, xacc;
    public static class AcElem{
        //int reg;
        //Non ha senso ricordare da quale registro è stato pushato il valore
        //L'importante è il numero associato a quel valore
        int val;
        boolean xmm;
        public AcElem(int v, boolean x){
            val=v;
            xmm=x;
        }
    }
    private final Stack<AcElem> qv;
    public Accumulator(){
        regs=new Register[]{AX, BX, CX, DX, DI, SI, R8, R9, R10, R11, R12, R13, R14, R15};
        xregs=new XReg[]{XMM0, XMM1, XMM2, XMM3, XMM4, XMM5, XMM6, XMM7, XMM8, XMM9, XMM10
        , XMM11, XMM12, XMM13, XMM14, XMM15};
        occup=new int[regs.length];
        xoc=new int[xregs.length];
        for(int i=0; i<regs.length; i++){
            occup[i]=-1;
        }
        for(int i=0; i<xregs.length; i++){
            xoc[i]=-1;
        }
        qv=new Stack<>(Integer.class);
        /*
        all'inizio gli accumulatori sono ax e xmm0
        */
        acc=0;
        xacc=0;
    }
    private int generatore(){
        int max=0;
        for(int i=0; i<occup.length; i++){
            if(occup[i]!= -1 && occup[i]>max)
                max=occup[i];
        }
        return max + 1;
    }
    private int Xgeneratore(){
        int max=0;
        for(int i=0; i<occup.length; i++){
            if(xoc[i]!= -1 && xoc[i]>max)
                max=xoc[i];
        }
        return max + 1;
    }
    public int prenota()throws CodeException{
        int j;
        for(int i=0; i<regs.length; i++){
            if(i==acc)
                continue;
            if(occup[i]==-1){
                j = generatore();
                occup[i] = j;
                return j;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_endregs"));
    }
    public void libera(int i)throws CodeException{
        if(i<0)
            throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
        for(int j=0; j<regs.length; j++){
            if(occup[j]==i){
                occup[j]=-1;
                return;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
    }
    public int xprenota()throws CodeException{
        int j = Xgeneratore();
        for(int i=0; i<xregs.length; i++){
            if(i==xacc)
                continue;
            if(xoc[i]==-1){
                xoc[i]=j;
                return j;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_endregs"));
    }
    public void xlibera(int i)throws CodeException{
        if(i==0)
            return;
        if(i<0)
            throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
        for(int j=0; j<xregs.length; j++){
            if(xoc[j]==i){
                xoc[j]=-1;
                return;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
    }
    /**
     * Se i registri rr sono prenotato, li sposta in altri registri
     * @param text
     * @param rr
     * @throws CodeException 
     */
    public void moveFrom(Segmenti text, Register... rr)throws CodeException{
        if(rr.length<=0)
            return;
        for(Register r:rr){
            int in=-1;
            for(int a=0; a<regs.length; a++){
                if(regs[a]==r){
                    in=a;
                    break;
                }
            }
            if(occup[in]==-1)
                continue;
            int ou=-1;
            for(int a=0; a<occup.length; a++){
                if(a==acc)
                    continue;
                if(Info.isIn(regs[a], rr))
                    continue;
                if(occup[a]==-1){
                    ou=a;
                    break;
                }
            }
            if(ou==-1)
                throw new CodeException(Lingue.getIstance().format("m_cod_endregs"));
            occup[ou]=occup[in];
            occup[in]=-1;
            text.addIstruzione("mov", regs[ou].getReg(), r.getReg());
            text.text.flush();//per sicurezza
        }
    }
    public int saveAccumulator()throws CodeException{
        int ret=generatore();
        occup[acc]=ret;
        for(int i=0; i<regs.length; i++){
            if(occup[i]==-1){
                acc=i;
                return ret;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_endregs"));
    }
    public int saveAccumulator(Register r)throws CodeException{
        int ret=generatore();
        occup[acc]=ret;
        for(int i=0; i<regs.length; i++){
            if(regs[i]==r){
                if(occup[i]!=-1)
                    throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
                acc=i;
                return ret;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_endregs"));
    }
    public void restoreAccumulator(int fd)throws CodeException{
        for(int i=0; i<occup.length; i++){
            if(occup[i]==fd){
                occup[i]=-1;
                acc=i;
                return;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
    }
    /**
    * come il precedente, ma fd punta al registro prima utilizzato come accumulatore.
    * và liberato con libera
     * @param fd
     * @throws comp.code.CodeException
    */
    public void restoreAccumulatorB(int fd)throws CodeException{
        for(int i=0; i<occup.length; i++){
            if(occup[i]==fd){
                occup[i]=-1;
                occup[acc]=fd;
                acc=i;
                return;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
    }
    /*
    esempio di ciclo save - restoreB
    1
    
    occup[i]=-1
    occup[j]=-1
    acc=j
    
    save
    
    occup[i]=-1
    occup[j]=fd
    acc=i
    
    restoreB
    
    occup[i]=fd
    occup[j]=-1
    acc=j
    
    oppure
    restore
    
    occup[i]=-1
    occup[j]=-1
    acc=j
    */
    public int xsaveAccumulator()throws CodeException{
        int ret=Xgeneratore();
        xoc[xacc]=ret;
        for(int i=0; i<xregs.length; i++){
            if(xoc[i]==-1){
                xacc=i;
                return ret;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_endregs"));
    }
    public void xrestoreAccumulator(int fd)throws CodeException{
        for(int i=0; i<xoc.length; i++){
            if(xoc[i]==fd){
                xoc[i]=-1;
                xacc=i;
                return;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
    }
    public void xrestoreAccumulatorB(int fd)throws CodeException{
        for(int i=0; i<xoc.length; i++){
            if(xoc[i]==fd){
                xoc[i]=-1;
                xoc[xacc]=fd;
                xacc=i;
                return;
            }
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
    }
    public Register getReg(int rd)throws CodeException{
        if(rd<0)
            throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
        for(int i=0; i<occup.length; i++){
            if(occup[i]==rd)
                return regs[i];
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
    }
    public Register getAccReg(){
        return regs[acc];
    }
    public XReg getXReg(int rd)throws CodeException{
        if(rd<0)
            throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
        for(int i=0; i<xoc.length; i++){
            if(xoc[i]==rd)
                return xregs[i];
        }
        throw new CodeException(Lingue.getIstance().format("m_cod_invdesc"));
    }
    public XReg getXAccReg(){
        return xregs[xacc];
    }
    /**
     * Potenzialmente pericoloso. Usare quando non servono controlli
     * @param reg 
     */
    public void setAccReg(Register reg){
        for(int i=0; i<regs.length; i++){
            if(regs[i]==reg){
                acc=i;
                break;
            }
        }
    }
    /**
     * Potenzialmente pericoloso. Usare quando non servono controlli
     * @param reg 
     */
    public void setXAccReg(XReg reg){
        for(int i=0; i<xregs.length; i++){
            if(xregs[i]==reg){
                xacc=i;
                break;
            }
        }
    }
    public int pushAll(Segmenti text)throws CodeException{
        return pushAll(text, new int[0], new int[0]);
    }
    /**
     * 
     * @param text
     * @param salva i registri da salvare, ma verrano comunque eliminati al popAll
     * @param xsalva
     * @return quante volte è stato effettuato il push di 8 byte
     * @throws CodeException 
     */
    public int pushAll(Segmenti text, int[] salva, int[] xsalva)throws CodeException{
        qv.push(null);//termine, all'inizio data la struttura a stack
        int ret=0;
        for(int i=0; i<xoc.length; i++){
            if(xoc[i]>-1){
                boolean find=false;
                for(int u:xsalva){
                    if(xoc[i] == u){
                        find=true;
                        break;
                    }
                }
                if(find)
                    continue;
                text.addIstruzione("sub","rsp","8");
                text.addIstruzione("movsd","qword[rsp]",xregs[i].getReg());
                ret++;
                qv.push(new AcElem(xoc[i], true));
                xoc[i]=-1;
            }
        }
        for(int i=0; i<occup.length; i++){
            if(occup[i]>-1){
                boolean find=false;
                for(int u:salva){
                    if(occup[i] == u){
                        find=true;
                        break;
                    }
                }
                if(find)
                    continue;
                text.addIstruzione("push",regs[i].getReg(),null);
                ret++;
                qv.push(new AcElem(occup[i], false));
                occup[i]=-1;
            }
        }
        return ret;
    }
    /*
    I registri rax, xmm0 e rdx DEVONO essere lasciati inalterati, in quanto contengolo
    rispettivamente valore di ritorno (gp o xmm) e condizione di errore
    ritorna il numero di pop do 8 byte
    */
    public int popAll(Segmenti text)throws CodeException{
        for(int i=0; i<occup.length; i++){
            occup[i]=-1;
        }
        for(int i=0; i<xoc.length; i++){
            xoc[i] = -1;
        }
        if(qv.size()==0)
            return 0;
        int ret=0;
        AcElem p=qv.pop();
        int j = 1;//esclude rax
        int xj = 1;//esclude xmm0
        while(p!=null){
            if(p.xmm){
                if(xj==xacc)
                    xj++;
                if(xj>=xregs.length)
                    throw new CodeException(Lingue.getIstance().format("m_cod_endregs"));
                xoc[xj]=p.val;
                text.addIstruzione("movsd",xregs[xj].getReg(),"qword[rsp]");
                text.addIstruzione("add","rsp","16");
                ret++;
                xj++;
            }
            else{
                while(regs[j]==Register.DX || j==acc)
                    j++;
                if(j>=regs.length)
                    throw new CodeException(Lingue.getIstance().format("m_cod_endregs"));
                occup[j]=p.val;
                text.addIstruzione("pop",regs[j].getReg(), null);
                ret++;
                j++;
            }
            p=qv.pop();
        }
        return ret;
    }
}
