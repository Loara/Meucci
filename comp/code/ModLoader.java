/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.code;

import comp.code.Funz.FElement;
import static comp.general.FileManager.cpath;
import static comp.general.FileManager.findTFile;
import static comp.general.FileManager.resps;
import comp.general.Lingue;
import comp.parser.Callable;
import comp.parser.Membro;
import comp.parser.Modulo;
import comp.parser.TypeDef;
import comp.parser.TypeName;
import comp.parser.template.NumDich;
import comp.parser.template.TemplateEle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Carica i vari moduli dai file, evita di ricaricare più volte lo stesso file
 * @author loara
 */
public class ModLoader {
    public static class MLdone{
        public MLdone(){
            name="";
        }
        public String name;
        public boolean external;
        public String[] dep;
        public Boolean[] pb;
        public TypeElem[] typ;
        public FElement[] fun;
        public TypeDef[] Ttyp;
        public Callable[] Tcal;
        @Override
        public boolean equals(Object o){
            return (o instanceof MLdone) && (name.equals(((MLdone)o).name));
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + Objects.hashCode(this.name);
            return hash;
        }
    }
    private final HashSet<MLdone> modLoaded;
    public ModLoader(){
        modLoaded=new HashSet<>();
    }
    private static ModLoader th;
    public static ModLoader getIstance(){
        if(th==null)
            th=new ModLoader();
        return th;
    }
    public MLdone getIfIn(String name){
        for(MLdone my:modLoaded){
            if(my.name.equals(name))
                return my;
        }
        return null;
    }
    /**
     * Cerca il file del manifesto dai responsitory
     * @param name
     * @return
     * @throws IOException 
     */
    public Path findMod(String name)throws IOException{
        Path p=Paths.get(cpath+"/"+name+".in");
        if(Files.exists(p) && Files.isReadable(p))
            return p;
        else if(!Files.exists(p)){
            for(String resp:resps){
                p=Paths.get(resp+"/"+name+".in");
                if(Files.exists(p)){
                    if(Files.isReadable(p))
                        return p;
                    else
                        throw new IOException(Lingue.getIstance().format("m_nofile", name, "in"));
                }
            }
            throw new IOException(Lingue.getIstance().format("m_nofile", name, "in"));
        }
        else
            throw new IOException(Lingue.getIstance().format("m_nofile", name, "in"));
    }
    public void exportModulo(Modulo mod)throws IOException, CodeException{//attenzione:non esportare funzioni shadow
        Path na=Paths.get(mod.nome+".in");
        try(ObjectOutputStream out=new ObjectOutputStream(new BufferedOutputStream
        (Files.newOutputStream(na, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)))){
            //out.writeUTF(mod.nome);non interessa il nome
            out.writeInt(mod.deps.length);
            for(String i:mod.deps)
                out.writeUTF(i);
            for(Boolean b:mod.publ)
                out.writeBoolean(b);
            out.writeInt(mod.type.length);
            for(TypeDef td:mod.type){
                exportType(new TypeElem(td, false), out);
            }
            //Vanno scritte solo le funzioni non shadow
            int num=0;
            for(Callable c:mod.ca){
                if(!c.isShadow())
                    num++;
            }
            out.writeInt(num);
            for(Callable c:mod.ca){
                if(!c.isShadow())
                    exportCall(new Funz.FElement(c, false), out);
            }
        }
        writeTemplates(mod);
    }
    /**
     * Aggiunge i Tipi e i Callable ai Funz e Types (anche template), e lo fa ricorsivamente.
     * Prima di utilizzarlo assicurarsi di lanciare clearAll ai suddetti
     * 
     * L'HashSet è necessario per evitare di caricare più volte lo stesso modulo
     * @param mods
     * @param name modulo da caricare
     * @param oname modulo che effettua il caricamento
     * @throws IOException 
     * @throws java.lang.ClassNotFoundException 
     */
    public void importModulo(Collection<MLdone> mods, String name, String oname)throws IOException, ClassNotFoundException{
        MLdone ii=getIfIn(name);
        boolean ext=!(name.equals(oname));
        if(ii==null){
            ii=new MLdone();
            Path p=findMod(name);
            try(ObjectInputStream in=new ObjectInputStream(new BufferedInputStream
            (Files.newInputStream(p, StandardOpenOption.READ)))){
                int i=in.readInt();
                ii.dep=new String[i];
                ii.pb=new Boolean[i];
                for(int j=0; j<i; j++)
                    ii.dep[j]=in.readUTF();
                for(int j=0; j<i; j++)
                    ii.pb[j]=in.readBoolean();
                i=in.readInt();
                ii.typ=new TypeElem[i];
                for(int j=0; j<i; j++)
                    ii.typ[j]=importType(in, ext);
                i=in.readInt();
                ii.fun=new FElement[i];
                for(int j=0; j<i; j++){
                    ii.fun[j]=importCall(in, ext);
                }
            }
            readTemplates(name, ii);
            modLoaded.add(ii);
        }
        ii.external=ext;
        if(!mods.contains(ii)){
            mods.add(ii);
            for(int k=0; k<ii.dep.length; k++){
                if(ii.pb[k]){
                    importModulo(mods, ii.dep[k], oname);
                }
            }
        }
    }
    private void exportType(TypeElem te, ObjectOutputStream out)throws IOException{
        out.writeUTF(te.name);
        out.writeBoolean(te.explicit);
        out.writeInt(te.subtypes.length);
        for(Membro mem:te.subtypes){
            int perms=0;
            //non ci sono override
            if(mem.explicit)
                perms=1;
            else if(mem.ghost)
                perms=2;
            perms*=3;
            if(mem.shadow)
                perms+=1;
            if(mem.read)
                perms+=2;
            out.writeByte(perms & 0xFF);
            out.writeObject(mem.getType());
            out.writeUTF(mem.getIdent());
            out.writeInt(mem.params.length);
            for(TypeName t:mem.params)
                out.writeObject(t);
            if(mem.packed==null)
                out.writeBoolean(false);
            else{
                out.writeBoolean(true);
                //Sicuramente NumDich
                out.writeLong(((NumDich)mem.packed).getNum());
            }
        }
        if(te.extend!=null){
            out.writeBoolean(true);
            out.writeObject(te.extend);
        }
        else
            out.writeBoolean(false);
    }
    private TypeElem importType(ObjectInputStream in, boolean ext)throws IOException,
            ClassNotFoundException{
        String name=in.readUTF();
        boolean explic=in.readBoolean();
        int i=in.readInt();
        Membro[] mem=new Membro[i];
        for(int j=0; j<i; j++){
            int perms=in.readByte()&0xFF;
            TypeName ty=(TypeName)in.readObject();
            String na=in.readUTF();
            int len=in.readInt();
            TypeName[] par=new TypeName[len];
            for(int k=0; k<len; k++)
                par[k]=(TypeName)in.readObject();
            TemplateEle pack=null;
            if(in.readBoolean())
                pack=new NumDich(in.readLong(), 2);
            boolean shadow=(perms % 3)==1;
            boolean read=(perms % 3)==2;
            perms=perms/3;
            boolean explicit=(perms % 3)==1;
            boolean ghost=(perms % 3)==2;
            mem[j]=new Membro(ty, na, par, shadow, read, explicit, ghost, pack);
        }
        TypeName ex=null;
        if(in.readBoolean())
            ex=(TypeName)in.readObject();
        return new TypeElem(name, ex, mem, ext, explic);
    }
    private void exportCall(Funz.FElement fe, ObjectOutputStream out)throws IOException{
        out.writeUTF(fe.name);
        out.writeUTF(fe.modname);
        out.writeObject(fe.ret);
        out.writeInt(fe.trequest.length);
        for(TypeName t:fe.trequest)
            out.writeObject(t);
        out.writeBoolean(fe.oper);
        int y = fe.errors.length;
        out.writeInt(y);
        for(int i = 0; i<y; i++){
            out.writeUTF(fe.errors[i]);//Conta l'ordine
        }
    }
    private Funz.FElement importCall(ObjectInputStream in, boolean ext)throws IOException,
            ClassNotFoundException{
        String name=in.readUTF();
        String modname=in.readUTF();
        TypeName ret;
        ret=(TypeName)in.readObject();
        int i=in.readInt();
        TypeName[] ty=new TypeName[i];
        for(int j=0; j<i; j++)
            ty[j]=(TypeName)in.readObject();
        boolean op=in.readBoolean();
        String[] err = new String[in.readInt()];
        for(int u =0; u<err.length; u++)
            err[u]=in.readUTF();
        return new Funz.FElement(name, modname, ty, ret, op, ext, false, err);
    }
    private void writeTemplates(Modulo mod)throws IOException{
        if(mod.Tca.length==0 && mod.Ttype.length==0)
            return;
        Path p=Paths.get(cpath+"/"+mod.nome+".tin");
        try(ObjectOutputStream out=new ObjectOutputStream(new 
            BufferedOutputStream(Files.newOutputStream(p, StandardOpenOption.WRITE, 
                StandardOpenOption.CREATE)))){
            out.writeObject(mod.Ttype);
            out.writeObject(mod.Tca);
        }
    }
    private void readTemplates(String mod, MLdone m)throws IOException{
        Path p=findTFile(mod, false);
        if(p==null){
            m.Ttyp=new TypeDef[0];
            m.Tcal=new Callable[0];
            return;
        }
        try(ObjectInputStream in=new ObjectInputStream(new 
            BufferedInputStream(Files.newInputStream(p, StandardOpenOption.READ)))){
            m.Ttyp=(TypeDef[])in.readObject();
            m.Tcal=(Callable[])in.readObject();
        }
        catch(ClassNotFoundException ex){
            throw new IOException(ex);
        }
    }
}
