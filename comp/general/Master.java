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
import comp.code.FElement;
import comp.code.Meth;
import comp.code.ModLoader;
import comp.code.ModLoader.MLdone;
import comp.code.Segmenti;
import comp.code.TypeElem;
import comp.code.Types;
import comp.code.template.Notifica;
import comp.code.template.Substitutor;
import comp.code.template.TNumbers;
import comp.parser.Callable;
import comp.parser.Dichiarazione;
import comp.parser.Modulo;
import comp.parser.ParserException;
import comp.parser.TypeDef;
import comp.parser.template.TemplateEle;
import comp.scanner.Analyser;
import comp.scanner.Analyser.ScanException;
import comp.scanner.Token;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static comp.general.FileManager.createAsmFile;

/**
 * Gestisce tutti i passi della compilazione
 * @author loara
 */
public class Master {
    public static String currentFile;
    private String[] oname;
    public boolean compile, assemble, link;
    
    public static void main(String[] args)throws Exception{
        Master m=new Master();
        if(args.length<1){
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
        for(String f:FileManager.createdFile){
            Process p=Runtime.getRuntime().exec(new String[]{"nasm", "-felf64", "-o"+f+".o", f+".asm"});
            mp= new MPrinter(p.getInputStream(), false);
            ep= new MPrinter(p.getErrorStream(), true);
            mp.start();
            ep.start();
            int ret=p.waitFor();
            if(ret!=0){
               deleteAll(0, FileManager.createdFile);
               return;
            }
        }
        if(!m.link){
            deleteAll(2, FileManager.createdFile);
            return;
        }
        String[] s=new String[1+FileManager.createdFile.size()];
        s[0]="ld";
        for(int i=0; i<FileManager.createdFile.size(); i++){
            s[1+i]=FileManager.createdFile.get(i)+".o";
        }
        Process pr=Runtime.getRuntime().exec(s);
        mp= new MPrinter(pr.getInputStream(), false);
        ep= new MPrinter(pr.getErrorStream(), true);
        mp.start();
        ep.start();
        int ret=pr.waitFor();
        deleteAll(0, FileManager.createdFile);
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
        FileManager.resps.add("/usr/include/meucci");
    }
    public void compila(String[] files)throws Exception{
        FileManager.createdFile=new ArrayList<>();
        oname=new String[files.length];
        FileManager.cpath=System.getProperty("user.dir");
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
        ModLoader.getIstance().exportModulo(m);
        return m;
    }
    public void codifica(Modulo m)throws IOException, CodeException, ClassNotFoundException{
        Path p=FileManager.createAsmFile(m.nome);
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
        MLdone modulo;
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
            modulo=ModLoader.getIstance().getIfIn(selected.modulo);
            while(true){
                if(type){
                    TypeDef h=null;
                    for(TypeDef td:modulo.Ttyp){
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
                    for(Callable td:modulo.Tcal){
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
                selected=getNoSigned(tty, modulo.name);
                if(selected==null){
                    selected=getNoSigned(tfun, modulo.name);
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
        Path pa=FileManager.createAsmFile(Meth.modName(funz, temps));
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
        Environment.currentModulo=modulo;
        HashSet<MLdone> ml=new HashSet<>();
        ModLoader.getIstance().importAllModules(ml, modulo, modulo);
        ml.stream().map((m) -> {
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
}
