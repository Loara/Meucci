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
import comp.general.Lingue;
import comp.parser.expr.IdentEle;
import comp.parser.Membro;
import comp.parser.TypeName;

/**
 * Segmento di memorizzazione: vtors (temporaneo)
 * Formato nome: _VT_INIT_"nomeclasse"_
 * La vtable deve essere generata una sola volta, e le diverse istanze dell'oggetto devono
 * puntare ad essa
 * @author loara
 */
public class VTable {
    private final int dim;//RELATIVA, ovvero non conta le vtable ereditate
    public String cname;
    public TypeName extend;
    public Membro[] membs;
    public int[] allineamenti;
      
    public VTable(Membro[] mems, String classname, TypeName ext){
        extend=ext;
        cname=classname;
        int i=0, j=0;
        for(Membro m:mems)
            if(!m.explicit && !m.override && !m.shadow)
                i++;
        membs=new Membro[i];
        allineamenti=new int[i];
        i=0;
        for(Membro m:mems)
            if(!m.explicit && !m.override && !m.shadow){
                membs[i]=m;
                allineamenti[i]=j;
                if(m.read)
                    j+=1;
                else
                    j+=2;
                i++;
            }
        dim=j*Info.pointerdim;
    }
    public int dimension()throws CodeException{
        int prec;
        if(extend!=null){
            prec=Types.getIstance().find(extend, false).vt.dimension();
        }
        else
            prec=8;//distruttore
        return dim+prec;
    }
    public int getReadAcc(IdentEle val, Variabili var, Environment env)throws CodeException{
        int j=8, f;//distruttore
        if(extend!=null){
            VTable eee=Types.getIstance().find(extend, false).vt;
            int i=eee.getReadAcc(val, var, env);
            if(i>-1)
                return i;
            else
                j=eee.dimension();
        }
        for(int i=0; i<membs.length; i++){
            f=membs[i].compatible(val, var, false);
            if(f==0){
                j+=allineamenti[i]*Info.pointerdim;
                return j;
            }
            else if(f!=-1)
                throw new CodeException(Lingue.getIstance().format("m_cod_errpacc"));
        }
        return -1;
    }
    /**
     * 
     * @param val
     * @param var
     * @param env
     * @return 0 - OK; -1 - Non trovato
     * @throws CodeException 
     */
    public int getWriteAcc(IdentEle val, Variabili var, Environment env)throws CodeException{
        int j=8;//distruttore
        if(extend!=null){
            VTable eee=Types.getIstance().find(extend, false).vt;
            int i=eee.getWriteAcc(val, var, env);
            if(i>-1)
                return i;
            else
                j=eee.dimension();
        }
        for(int i=0; i<membs.length; i++){
            int rey=membs[i].compatible(val, var, false);
            if(rey==0){//E' garantita la scrittura
                j+=(allineamenti[i]+1)*Info.pointerdim;
                return j;
            }
            else
                throw new CodeException(Lingue.getIstance().format("m_cod_errpacc"));
        }
        return -1;
    }
    /**
     * Come i precedenti, solo non controlla la compatibilità
     * @param ident
     * @param get
     * @return
     * @throws CodeException 
     */
    public int index(String ident, boolean get)throws CodeException{
        int j=8;//distruttore
        if(extend!=null){
            VTable eee=Types.getIstance().find(extend, false).vt;
            int i=eee.index(ident, get);
            if(i>-1)
                return i;
            else
                j=eee.dimension();
        }
        for(int i=0; i<membs.length; i++){
            if(ident.equals(membs[i].getIdent())){
                if(get)
                    j+=allineamenti[i]*Info.pointerdim;
                else
                    j+=(allineamenti[i]+1)*Info.pointerdim;
                return j;
            }
        }
        return -1;
    }
}
