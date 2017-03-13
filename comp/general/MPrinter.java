/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.general;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author loara
 */
public class MPrinter extends Thread{
    private final InputStream inp;
    private final boolean err;
    public MPrinter(InputStream in, boolean er){
        inp=in;
        err=er;
    }
    @Override
    public void run(){
        try{
            BufferedReader br=new BufferedReader(new InputStreamReader(inp));
            String line = br.readLine();
            while(line !=null){
                if(err){
                    if(!line.contains("absolute address can not be RIP-relative"))//NASM bug
                        System.err.println(line);
                }
                else
                    System.out.println(line);
                line = br.readLine();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
