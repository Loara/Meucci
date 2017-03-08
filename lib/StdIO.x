modulo StdIO depends Syscalls Strings Arrays{
	String codec;
	static{
		codec="0123456789abcdefghijklmnopqrstuvwxyz";
	}
	void print(String s){
		Pwrite(1, getCstr(s), (ulong)(s.lenght));
	}
	void newLine(){
		//System-dependent
		putChar((char)10);
	}
	void println(String s){
		print(s);
		newLine();
	}
	void print(int c, uint base){
		if(base <= u1B || (base > codec.lenght))
			return;
		if(c == 0){
			putChar('0');
			return;
		}
		uint value;
		if(c < 0){
			putChar('-');
			value=(uint)(-c);
		}
		else
			value=(uint)c;
		//Determina lunghezza stringa
		uint car = u1;
		{
			uint v = base;
			while(value >= v){
				car =+ u1;
				v =* base;
			}
		}
		Array[char] ar = :stack Array[char](car);
		//Conversione
		for(uint i = car - u1; i >= u0; i =- u1){
			ar.elem[i] = codec :at (value :mod base);
			value =/ base;
		}
		Pwrite(1, dataPointer[char](ar), (ulong)(ar.length));
		//Il bug era semplice da intuire: la chiamata automatica al distruttore
		//ha "sbagliato il tiro" in quanto esegue il puntatore e non l'istruzione
		//che punta 
	}
	void print(int c){
		print(c, u10);
	}
	void println(int a){
		print(a);
		newLine();
	}
}
