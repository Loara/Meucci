modulo Pointers{
	type explicit pointer[typ U]{
		U el;
	}
	type explicit Rpointer[typ U]{
		read U el;
	}
	pt + (pt p, uint vel){
		%a{
	mov	r0,[rbp+16]
	mov	r3d,[rbp+24]
	add	r0,r3
		}
	}
	pt - (pt p, uint vel){
		%a{
	mov	r0,[rbp+16]
	mov	r3d,[rbp+24]
	sub	r0,r3
		}
	}
	pt + (pt p, long vel){
		%a{
	mov	r0,[rbp+16]
	mov	r3,[rbp+24]
	add	r0,r3
		}
	}
	long offset(pt a, pt b){
		%a{
	mov	r0,[rbp+16]
	mov	r3,[rbp+24]
	sub	r0,r3
		}
	}
	pointer[U] somma[typ U](pointer[U] p, uint index){
		pt rey=(pt)p+(uint)(index*#SIZEOF(U));
		return (pointer[U])rey;
	}
	Rpointer[V] Rsomma[typ V](Rpointer[V] p, uint index){
		pt rey=(pt)p+(uint)(index*#SIZEOF(V));
		return (Rpointer[V])rey;
	}
}
