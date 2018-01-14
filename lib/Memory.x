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
	shadow Block createBlock(uint i){
		declare
			pt g;
			Block ret;
		enddec
		if(i+u8<u8)//Overflow
			return null;
		if(i <= u8)
			i = u16;
		else
			i = i + (- i & u7) + u8;//Sicurezza che dim non verrà toccato
		g=sbrk((long)i);
		ret=(Block)g;
		ret.dim=i;
		ret.next=null;
		return ret;
	}
	shadow Block requestBlock(uint dim){
		declare
			Block b;
		enddec
		if(base==null)
			return createBlock(dim);
		b=base;
		if(base.dim>=dim){
			base=base.next;
			return b;
		}
		while(b.next!=null){
			if(b.next.dim>=dim){
				declare
					Block ret;
				enddec
				ret=b.next;
				b.next=b.next.next;
				return ret;
			}
			b=b.next;
		}
		return createBlock(dim);
	}
	pt allocate(uint dim){
		declare
			Block b;
		enddec
		b=requestBlock(dim);
		b.next=null;//Per sicurezza
		return (pt)b+u8;//Allineamento e preservare dim
	}
	void free(pt ob){
		declare
			Block b;
		enddec
		b=(Block)(ob-u8);
		b.next=base;
		base=b;
	}
}
