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

import comp.general.Info;

/**
 * I numeri sono presi da altreg di nasm
 * @author loara
 */
public enum Register {
    AX{
        @Override
        public int getInt(){
            return 0;
        }
    },
    BX{
        @Override
        public int getInt(){
            return 3;
        }
    },
    CX{
        @Override
        public int getInt(){
            return 1;
        }
    },
    DX{
        @Override
        public int getInt(){
            return 2;
        }
    },
    DI{
        @Override
        public int getInt(){
            return 7;
        }
    },
    SI{
        @Override
        public int getInt(){
            return 6;
        }
    },
    R8{
        @Override
        public int getInt(){
            return 8;
        }
    },
    R9{
        @Override
        public int getInt(){
            return 9;
        }
    },
    R10{
        @Override
        public int getInt(){
            return 10;
        }
    },
    R11{
        @Override
        public int getInt(){
            return 11;
        }
    },
    R12{
        @Override
        public int getInt(){
            return 12;
        }
    },
    R13{
        @Override
        public int getInt(){
            return 13;
        }
    },
    R14{
        @Override
        public int getInt(){
            return 14;
        }
    },
    R15{
        @Override
        public int getInt(){
            return 15;
        }
    },
    RSP{
        @Override
        public int getInt(){
            return 4;
        }
    },
    RBP{
        @Override
        public int getInt(){
            return 5;
        }
    };
    public String getReg(int dim){
        String ddim;
        switch(dim){
            case 1:
                ddim="b";
                break;
            case 2:
                ddim="w";
                break;
            case 4:
                ddim="d";
                break;
            default:
                ddim="";
        }
        return "r"+getInt()+ddim;
    }
    public abstract int getInt();
    /**
     * Dimensione del puntatore
     * @return 
     */
    public String getReg(){
        return getReg(Info.pointerdim);
    }
    /**
     * Ritorna il registro di dimensione adatta al tipo (dimensione allocata)
     * @param type
     * @return
     * @throws CodeException 
     */
    public String getReg(String type)throws CodeException{
        return getReg(Types.getIstance().find(type).realDim());
    }
}
