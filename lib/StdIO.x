modulo StdIO depends public Syscalls, public Strings, Arrays{
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
	void print(ulong c, uint base){
		declare{
			ulong b v;
			uint car i;
			Array[char] ar;
		}
		if(base <= u1B || (base > codec.lenght))
			return;
		if(c == u0l){
			putChar('0');
			return;
		}
		b = (ulong)base;
		//Determina lunghezza stringa
		car = u1;
		v = c;
		while(v >= b){
			car =+ u1;
			v =/ b;
		}
		ar = :stack iniDynArray[char](car);
		//Conversione
		for(i = car - u1; i >= u0; i =- u1){
			ar.elem[i] = codec :at (uint)(c :mod b);
			c =/ b;
		}
		Pwrite(1, dataPointer[char](ar), (ulong)(ar.length));
	}
	void print(long c, uint base){
		if(c < 0l){
			putChar('-');
			print((ulong)(-c), base);
		}
		else
			print((ulong)c, base);
	}
	void print(long a){
		print(a, u10);
	}
	void print(ulong a){
		print(a, u10);
	}
	void print(int a){
		print((long)a, u10);
	}
	void print(uint a){
		print((ulong)a, u10);
	}
	void print(short a){
		print((long)a, u10);
	}
	void print(ushort a){
		print((ulong)a, u10);
	}
	void print(byte a){
		print((long)a, u10);
	}
	void print(ubyte a){
		print((ulong)a, u10);
	}
	void println(long a){
		print(a, u10);
		newLine();
	}
	void println(ulong a){
		print(a, u10);
		newLine();
	}
	void println(int a){
		print((long)a, u10);
		newLine();
	}
	void println(uint a){
		print((ulong)a, u10);
		newLine();
	}
	void println(short a){
		print((long)a, u10);
		newLine();
	}
	void println(ushort a){
		print((ulong)a, u10);
		newLine();
	}
	void println(byte a){
		print((long)a, u10);
		newLine();
	}
	void println(ubyte a){
		print((ulong)a, u10);
		newLine();
	}
}
