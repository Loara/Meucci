/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.general;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Dipendente da Master, si occupa della gestione di tutti i file
 * @author loara
 */
public class FileManager {
    
    public  static ArrayList<String> createdFile=new ArrayList<>();
    public static String cpath="";//percorso corrente
    public static ArrayList<String> resps=new ArrayList<>();//repo
    public static Path createAsmFile(String path)throws IOException{
        Path p=Paths.get(path+".asm").toAbsolutePath();
        //System.out.println(p.toString());
        Files.deleteIfExists(p);
        createdFile.add(path);
        return p;
    }
    public static Path findTFile(String name, boolean thrw)throws IOException{
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
}
