modulo Strings depends Memory Pointers{
	type explicit String{
		explicit read uint lenght;
		shadow Rpointer[char] point;
		init(uint l, pt data){
			this.lenght=l;
			this.point=(Rpointer[char])data;
		};
	}
	boolean :equals (String s, String tul){
		if(s.lenght != tul.lenght)
			return false;
		pt ps=(pt)s.point;
		pt pp=(pt)tul.point;
		uint l=s.lenght;

		%a{
	mov	rdi,[rbp-8]
	mov	rsi,[rbp-16]
	xor	rcx,rcx
	xor	al,al
	mov	ecx,[rbp-20]
	shr	ecx,3
	cld
	repe	cmpsq
	jne	NOEQ
	mov	ecx,[rbp-20]
	and	ecx,7
	repe	cmpsb
	sete	al
NOEQ:
		}

	}
	String allocaStr(uint dim){
		//crea una stringa vuota formata da dim caratteri (escluso \0)
		pt data=allocate(dim+u1);
		return :new String(dim, data);
	}
	char :at(String s, uint val){
		return Rsomma[char](s.point, val).el;
	}
	pt getCstr(String c){
		return (pt)c.point;
	}
}
