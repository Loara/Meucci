/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package comp.general;

/**
 * Serve un algoritmo di ordinamento STABILE
 * 
 * ci sono due array: uno contiene i valori su cui si basa l'ordinamento, l'altro 
 * gli indici dell'array originario
 * @author loara
 */
public class MergeSort {
    public static void merge(int be, int ce, int end, int[] values, int[] index){
        if(end==ce || be==ce)
            return;
        int[] supp = new int[end-be];
        int[] supi = new int[end-be];
        int i=be, j=ce;
        for(int k=0; k<supp.length; k++){
            if(i==ce){
                supp[k]=values[j];
                supi[k]=index[j];
                j++;
            }
            else if(j==end){
                supp[k]=values[i];
                supi[k]=index[i];
                i++;
            }
            else if(values[i]>=values[j]){
                //L'uguaglianza è necessaria per mantenere la STABILITÀ
                //altrimenti non è stabile
                supp[k]=values[i];
                supi[k]=index[i];
                i++;
            }
            else{
                supp[k]=values[j];
                supi[k]=index[j];
                j++;
            }
        }
        for(int k=0; k<supp.length; k++){
            values[be+k]=supp[k];
            index[be+k]=supi[k];
        }
    }
    public static void sort(int be, int end, int[] values, int[] index){
        int ce=(end+be)/2;
        if(ce-be > 1)
            sort(be, ce, values, index);
        if(end-ce > 1)
            sort(ce, end, values, index);
        merge(be, ce, end, values, index);
    }
    /**
     * Ordine decrescente
     * @param values
     * @param index 
     */
    public static void sort(int[] values, int[] index){
        if(values.length<=1)
            return;
        sort(0, values.length, values, index);
    }
}
