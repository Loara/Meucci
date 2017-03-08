modulo Arrays depends Memory{
	type Array[typ T]{
		explicit read uint length;
		shadow pointer[T] memory;
		ghost T elem[uint]{
			T get(uint index){
				if(index < this.length){
					pointer[T] val=somma[T](this.memory, index);
					return val.el;
				}
				else{
					%a{
	xor	r0,r0
					}
				}
			}
			void set(T va, uint index){
				if(index < this.length){
					pointer[T] val=somma[T](this.memory, index);
					val.el=va;
				}
			}
		};
		init(uint size){
			pt memory=allocate((uint)#SIZEOF(T)*size);
			this.memory=(pointer[T])memory;
			this.length=size;
		};
		end(){
			free((pt)this.memory);
		};
	}
	type explicit StaticArray[typ T, num L 2]{
		T elem packed L;
		init(){

		};
	}
	void copy[typ a](Array[a] dest, Array[a] source, uint dstart, uint sstart, uint len){
		//La seguente riga di codice è errata a causa dell'associatività
		//if((dstart+len)>=dest.length || (sstart+len)>=source.length)
		//La seguente è corretta
		if(dstart+len >= dest.length || (sstart+len >= source.length))
			return;
		for(uint i=u0; i<len; i =+ u1){
			dest.elem[dstart+i] = source.elem[sstart+i];
		}
	}
	Array[T] toArray[typ T, num L 2](StaticArray[T, L] sa){
		Array[T] ar = :new Array[T](L);
		for(uint i=u0; i<L; i =+ u1)
			ar.elem[i]=sa.elem[i];
		return ar;
	}
	pt dataPointer[typ T](Array[T] a){
		return (pt)a.memory;
	}
}
