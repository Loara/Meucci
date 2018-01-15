modulo Strings depends Memory, Pointers{
	type explicit String{
		read uint lenght;
		shadow Rpointer[char] point;
	}
	costructor iniString(String this, uint len, pt data){
		this.lenght = len;
		this.point = (Rpointer[char]) data;
	}
	boolean :equals (String s, String tul){
		declare{
			pt ps pp;
			uint l;
		}
		if(s.lenght != tul.lenght)
			return false;
		ps=(pt)s.point;
		pp=(pt)tul.point;
		l=s.lenght;

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
	costructor allocaStr(String this, uint dim){
		//crea una stringa vuota formata da dim caratteri (escluso \0)
		declare{
			pt data;
		}
		data = allocate(dim+u1);
		( (pointer[char]) (data + dim) ).el = (char)u0b;
		:super iniString(dim, data);
	}
	char :at(String s, uint val){
		return Rsomma[char](s.point, val).el;
	}
	pt getCstr(String c){
		return (pt)c.point;
	}
	void cMoving(pt des, pt src, ulong nu){
		%a{
	mov	rdi,[rbp+16]
	mov	rsi,[rbp+24]
	mov	rbx,[rbp+32]
	mov	rcx,rbx
	shr	rcx,3
	cld
	rep	movsq
	mov	rcx,rbx
	and	rcx,7
	rep	movsb
		}
	}
	String substring(String src, uint begin, uint endd){
		declare{
			uint len;
			String ret;
		}
		if((begin > endd) || (endd > src.lenght))
			throw error;
		len = endd - begin;
		ret = :new allocaStr(len);
		if(len == u0){
			return ret;
		}
		cMoving((pt)ret.point, (pt)Rsomma[char](src.point, begin), (ulong)len);
		return ret;
	}
}
