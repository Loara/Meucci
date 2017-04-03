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
package comp.general;

import comp.code.CodeException;
import comp.code.Environment;
import comp.code.Funz;
import comp.code.Funz.FElement;
import comp.code.Meth;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.Notifica;
import comp.code.template.Substitutor;
import comp.code.template.TNumbers;
import comp.parser.Callable;
import comp.parser.Dichiarazione;
import comp.parser.Membro;
import comp.parser.Modulo;
import comp.parser.ParserException;
import comp.parser.TypeDef;
import comp.parser.TypeName;
import comp.parser.template.NumDich;
import comp.parser.template.TemplateEle;
import comp.scanner.Analyser;
import comp.scanner.Analyser.ScanException;
import comp.scanner.Token;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Gestisce tutti i passi della compilazione
 * @author loara
 */
public class Master {
    
    public static String currentFile;
    
    public ArrayList<String> createdFile;
    private String cpath;//percorso corrente
    public final ArrayList<String> resps;
    private String[] oname;
    public boolean compile, assemble, link;
    
    public static void main(String[] args)throws Exception{
        Master m=new Master();
        if(args.length>1){
            System.out.println(Lingue.getIstance().format("m_option"));
            return;
        }
        Stack<String> fil=new Stack<>(String.class);
        m.compile=true;
        m.assemble=true;
        m.link=true;
        m.detect(args, fil);
        m.compila(fil.toArray());
        if(!m.assemble || !m.compile)
            return;
        MPrinter mp;
        MPrinter ep;
        for(String f:m.createdFile){
            Process p=Runtime.getRuntime().exec(new String[]{"nasm", "-felf64", "-o"+f+".o", f+".asm"});
            mp= new MPrinter(p.getInputStream(), false);
            ep= new MPrinter(p.getErrorStream(), true);
            mp.start();
            ep.start();
            int ret=p.waitFor();
            if(ret!=0){
               deleteAll(0, m.createdFile);
               return;
            }
        }
        if(!m.link){
            deleteAll(2, m.createdFile);
            return;
        }
        String[] s=new String[1+m.createdFile.size()];
        s[0]="ld";
        for(int i=0; i<m.createdFile.size(); i++){
            s[1+i]=m.createdFile.get(i)+".o";
        }
        Process pr=Runtime.getRuntime().exec(s);
        mp= new MPrinter(pr.getInputStream(), false);
        ep= new MPrinter(pr.getErrorStream(), true);
        mp.start();
        ep.start();
        int ret=pr.waitFor();
        deleteAll(0, m.createdFile);
    }
    private void detect(String[] params, Stack<String> fil){
        for (String param : params) {
            if (param.equals("-p")) {
                compile=false;
            } else if (param.equals("-c")) {
                assemble=false;
            }
            else if(param.equals("-a"))
                link=false;
            else if(param.equals("/"))
                return;
            else if(param.endsWith(".x")){
                fil.push(param);
            }
        }
    }
    private static void deleteAll(int v, ArrayList<String> createdFiles)
    throws IOException{
        /*
        v=0 elimina tutto
        v=1 lascia i .asm
        v=2 lascia i .o
        */
        for(String t:createdFiles){
            if(v!=1)
                Files.deleteIfExists(Paths.get(t+".asm"));
            if(v!=2)
                Files.deleteIfExists(Paths.get(t+".o"));
        }
    }
    public Master(){
        resps=new ArrayList<>();
        resps.add("/usr/include/meucci");
    }
    public void compila(String[] files)throws Exception{
        createdFile=new ArrayList<>();
        oname=new String[files.length];
        cpath=System.getProperty("user.dir");
        Modulo[] mods=new Modulo[files.length];
        for(int i=0; i<oname.length; i++){
            oname[i]=files[i].substring(files[i].lastIndexOf("/")+1, files[i].length()-2);
            currentFile=oname[i];
            mods[i]=parser(new VScan<>(analyze(Paths.get(files[i]))));
        }
        if(!compile)
            return;
        HashSet<Notifica> Tty=new HashSet<>(), Tfun=new HashSet<>();
        Types.getIstance().getClassList().setHashNotif(Tty);
        Funz.getIstance().getFunzList().setHashNotif(Tfun);
        for(Modulo m:mods){
            currentFile=m.nome;
            codifica(m);
        }
        codificaTemplate(Tty, Tfun);
    }
    public List<Token> analyze(Path p)throws IOException, ScanException{
        ArrayList<Character> c=new ArrayList<>();
        try(BufferedReader read=Files.newBufferedReader(p)){
            int i=read.read();
            while(i!=-1){
                c.add((char)i);
                i=read.read();
            }
        }
        VScan<Character> sc=new VScan<>(c);
        ArrayList<Token> t=new ArrayList<>();
        Analyser an=new Analyser();
        an.analyze(sc, t);
        return t;
    }
    public Modulo parser(VScan<Token> t)throws ParserException, IOException, CodeException{
        Modulo m=new Modulo(t);
        exportModulo(m);
        return m;
    }
    public void codifica(Modulo m)throws IOException, CodeException, ClassNotFoundException{
        Path p=this.createAsmFile(m.nome);
        try(PrintWriter pw=new PrintWriter(Files.newBufferedWriter(p, StandardOpenOption.WRITE, 
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))){
            pw.println("\tDEFAULT\tREL");
            pw.println("\t%use\taltreg");
            m.Codifica(pw, this);//effettua il clean di Types e Funz
        }
    }
    public void codificaTemplate(HashSet<Notifica> tty, HashSet<Notifica> tfun)throws CodeException,
            IOException, ClassNotFoundException{
        boolean type;
        String modulo;
        Notifica selected;
        Stack<String> st=new Stack<>(String.class);
        while(true){
            selected=getNoSigned(tty);
            if(selected==null){
                selected=getNoSigned(tfun);
                if(selected==null)
                    break;
                else
                    type=false;
            }
            else
                type=true;
            modulo=selected.modulo;
            rey reb=readTemplates(modulo);
            while(true){
                if(type){
                    TypeDef h=null;
                    for(TypeDef td:reb.td){
                        if(td.getName().equals(selected.nome)){
                            h=td;
                            break;
                        }
                    }
                    if(h==null)
                        throw new IOException("Tipo template non trovato");
                    codificaTType(h, selected.parametri);
                    if(h.hasVTable()){
                        st.push("_VT_INIT_"+Meth.className(selected.nome, selected.parametri));
                    }
                    selected.segnato=true;
                }
                else{
                    Callable h=null;
                    for(Callable td:reb.ca){
                        if(td.getName().equals(selected.nome)){
                            h=td;
                            break;
                        }
                    }
                    if(h==null)
                        throw new IOException("Funzione template non trovato");
                    codificaTFunz(h, selected.parametri);
                    selected.segnato=true;
                }
                selected=getNoSigned(tty, modulo);
                if(selected==null){
                    selected=getNoSigned(tfun, modulo);
                    if(selected==null)
                        break;
                    else
                        type=false;
                }
                else
                    type=true;
            }
        }
        Path Tp=createAsmFile("_INIT_TEMPS");
        try(PrintWriter pw=new PrintWriter(Files.newBufferedWriter(
                Tp, StandardOpenOption.WRITE, StandardOpenOption.CREATE))){
            String[] va=st.toArray();
            pw.println("\tDEFAULT\tREL");
            pw.println("\tglobal\t_INIT_TEMPS:function");
            for(String s:va)
                pw.println("\textern\t"+s);
            pw.println("_INIT_TEMPS:");
            for(String s:va){
                pw.println("\tcall\t"+s);
            }
            pw.println("\tret");
        }
    }
    public void codificaTType(TypeDef type, TemplateEle[] temps)
    throws CodeException, IOException, ClassNotFoundException{
        Path pa=createAsmFile(Meth.className(type.getName(), temps));
        try(PrintWriter out=new PrintWriter(Files.newBufferedWriter(pa, StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE))){
            settaAmbiente(type.modulo());
            Substitutor sub=new Substitutor();
            sub.addAll(type.templateNames(), temps);
            Types.getIstance().setSubstitutor(sub);
            Funz.getIstance().setSubstitutor(sub);
            TNumbers.getIstance().setSubstuitutor(sub);
            out.println("\tDEFAULT\tREL");
            out.println("\t%use\taltreg");
            Environment env=new Environment();
            Segmenti seg=new Segmenti();
            Environment.currentModulo=type.modulo();
            type.toCode(seg, new Dichiarazione[0], env, temps);
            seg.closeAll();
            Funz.getIstance().glob.stream().forEach((gl) -> {
                out.println("\tglobal\t"+gl);
            });
            Funz.getIstance().ext.stream().forEach((ex) -> {
                out.println("\textern\t"+ex);
            });
            
            out.println("\tsection\t.data");
            seg.data.stream().forEach((t) -> {
                out.println(t);
            });
            
            out.println("\tsection\t.rodata");
            seg.rodata.stream().forEach((t) ->{
                out.println(t);
            });
        
            out.println("\tsection\t.bss");
            seg.bss.stream().forEach((t) ->{
                out.println(t);
            });
        
            out.println("\tsection\t.text");
            seg.text.toArrayList().stream().forEach((t) -> {
                out.println(t);
            });
            Funz.getIstance().clearGE();
        }
    }
    public void codificaTFunz(Callable funz, TemplateEle[] temps)
    throws CodeException, IOException, ClassNotFoundException{
        Path pa=createAsmFile(Meth.modName(funz, temps));
        try(PrintWriter out=new PrintWriter(Files.newBufferedWriter(pa, StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE))){
            settaAmbiente(funz.getModulo());
            Substitutor sub=new Substitutor();
            sub.addAll(funz.templateNames(), temps);
            Types.getIstance().setSubstitutor(sub);
            Funz.getIstance().setSubstitutor(sub);
            TNumbers.getIstance().setSubstuitutor(sub);
            out.println("\tDEFAULT\tREL");
            out.println("\t%use\taltreg");
            Environment env=new Environment();
            Segmenti seg=new Segmenti();
            Environment.currentModulo=funz.getModulo();
            funz.toCode(seg, new Dichiarazione[0], env, temps);
            seg.closeAll();
            Funz.getIstance().glob.stream().forEach((gl) -> {
                out.println("\tglobal\t"+gl);
            });
            Funz.getIstance().ext.stream().forEach((ex) -> {
                out.println("\textern\t"+ex);
            });
            
            out.println("\tsection\t.data");
            seg.data.stream().forEach((t) -> {
                out.println(t);
            });
            
            out.println("\tsection\t.rodata");
            seg.rodata.stream().forEach((t) ->{
                out.println(t);
            });
        
            out.println("\tsection\t.bss");
            seg.bss.stream().forEach((t) ->{
                out.println(t);
            });
        
            out.println("\tsection\t.text");
            seg.text.toArrayList().stream().forEach((t) -> {
                out.println(t);
            });
            Funz.getIstance().clearGE();
        }
    }
    //Setta Funz e Types per le funzioni template
    private void settaAmbiente(String modulo)throws CodeException, IOException, ClassNotFoundException{
        Types.getIstance().clearAll();
        Funz.getIstance().clearAll();
        TNumbers.getIstance().clearAll();
        HashSet<String> ml=new HashSet<>();
        importModulo(modulo, modulo, ml);
    }
    private Notifica getNoSigned(HashSet<Notifica> nos){
        for(Notifica no:nos){
            if(!no.segnato)
                return no;
        }
        return null;
    }
    private Notifica getNoSigned(HashSet<Notifica> nos, String modulo){
        for(Notifica no:nos){
            if(!no.segnato && no.modulo.equals(modulo))
                return no;
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
    public Path createAsmFile(String path)throws IOException{
        Path p=Paths.get(path+".asm");
        Files.deleteIfExists(p);
        createdFile.add(path);
        return p;
    }
    public Path findTFile(String name, boolean thrw)throws IOException{
        Path p=Paths.get(cpath+"/"+name+".tin");
        if(Files.exists(p) && Files.isReadable(p))
            return p;
        else if(!Files.exists(p)){
            for(String resp:resps){
                p=Paths.get(resp+"/"+name+".tin");
                if(Files.exists(p)){
                    if(Files.isReadable(p))
                        return p;
                    else
                        throw new IOException(Lingue.getIstance().format("m_nofile", name, "tin"));
                }
            }
            if(thrw)
                throw new IOException(Lingue.getIstance().format("m_nofile", name, "tin"));
            else
                return null;
        }
        else
            throw new IOException(Lingue.getIstance().format("m_nofile", name, "tin"));
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
            out.writeInt(mod.type.length);
            for(TypeDef td:mod.type){
                exportType(new TypeElem(td, false), out);
            }
            out.writeInt(mod.ca.length);
            for(Callable c:mod.ca)
                exportCall(new FElement(c, false), out);
        }
        writeTemplates(mod);
    }
    /**
     * Aggiunge i Tipi e i Callable ai Funz e Types (anche template), e lo fa ricorsivamente.
     * Prima di utilizzarlo assicurarsi di lanciare clearAll ai suddetti
     * 
     * L'HashSet è necessario per evitare di caricare più volte lo stesso modulo
     * @param name modulo da caricare
     * @param oname modulo che effettua il caricamento
     * @param ml
     * @throws IOException 
     * @throws java.lang.ClassNotFoundException 
     */
    public void importModulo(String name, String oname, HashSet<String> ml)throws IOException, ClassNotFoundException{
        if(ml.contains(name))
            return;//se già caricato
        boolean ext=!(name.equals(oname));
        Path p=findMod(name);
        String[] de;
        try(ObjectInputStream in=new ObjectInputStream(new BufferedInputStream
        (Files.newInputStream(p, StandardOpenOption.READ)))){
            int i=in.readInt();
            de=new String[i];
            for(int j=0; j<i; j++)
                de[j]=in.readUTF();
            i=in.readInt();
            for(int j=0; j<i; j++)
                Types.getIstance().load(importType(in, ext));
            i=in.readInt();
            for(int j=0; j<i; j++){
                FElement fe=importCall(in, ext);
                Funz.getIstance().load(fe);
            }
        }
        rey re=readTemplates(name);//Ora lo cerca
        Types.getIstance().getClassList().addAll(re.td);
        Funz.getIstance().getFunzList().addAll(re.ca);
        ml.add(name);
        for(String dep:de)
            importModulo(dep, oname, ml);
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
    private void exportCall(FElement fe, ObjectOutputStream out)throws IOException{
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
    private FElement importCall(ObjectInputStream in, boolean ext)throws IOException,
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
        return new FElement(name, modname, ty, ret, op, ext, false, err);
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
    private rey readTemplates(String mod)throws IOException{
        Path p=findTFile(mod, false);
        rey ret=new rey();
        if(p==null){
            ret.td=new TypeDef[0];
            ret.ca=new Callable[0];
            return ret;
        }
        try(ObjectInputStream in=new ObjectInputStream(new 
            BufferedInputStream(Files.newInputStream(p, StandardOpenOption.READ)))){
            ret.td=(TypeDef[])in.readObject();
            ret.ca=(Callable[])in.readObject();
        }
        catch(ClassNotFoundException ex){
            throw new IOException(ex);
        }
        return ret;
    }
    public static class rey{
        TypeDef[] td;
        Callable[] ca;
    }
}
