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

import java.util.HashMap;

/**
 * Attualmente inutile, in futuro potrebbe essere utile per memorizzare variabili
 * del compilatore
 * @author loara
 */
public class CVars extends HashMap<String, Integer>{
    private static CVars cv;
    private CVars(){
        super();
        put("INTDIM", 4);
        put("LONGDIM", 8);
        put("BOOLDIM", 1);
        put("PTDIM", 8);
        put("DOUBLEDIM", 8);
        put("CHARDIM", 1);
        put("MAXBYTE", 8);
    }
    public static CVars getIstance(){
        if(cv==null)
            cv=new CVars();
        return cv;
    }
}
