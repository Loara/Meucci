modulo Memory depends Syscalls{
	//Algoritmo di allocazione molto semplice
	//e per niente efficiente
	//in lavorazione un algoritmo buddy
	type explicit Block{
		shadow uint dim;
		shadow Block next;
	}
	Block base;
	static{
		base=null;
	}
	Block createBlock(uint i){
		if(i+u8<u8)//Overflow
			return null;
		if(i <= u8)
			i = u16;
		else
			i = i + (- i & u7) + u8;//Sicurezza che dim non verrà toccato
		pt g=sbrk((long)i);
		Block ret=(Block)g;
		ret.dim=i;
		ret.next=null;
		return ret;
	}
	Block requestBlock(uint dim){
		if(base==null)
			return createBlock(dim);
		Block b=base;
		if(base.dim>=dim){
			base=base.next;
			return b;
		}
		while(b.next!=null){
			if(b.next.dim>=dim){
				Block ret=b.next;
				b.next=b.next.next;
				return ret;
			}
			b=b.next;
		}
		return createBlock(dim);
	}
	pt allocate(uint dim){
		Block b=requestBlock(dim);
		b.next=null;//Per sicurezza
		return (pt)b+u8;//Allineamento e preservare dim
	}
	void free(pt ob){
		Block b=(Block)(ob-u8);
		b.next=base;
		base=b;
	}
}
