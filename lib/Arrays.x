modulo Arrays depends Memory, public Pointers{
	type Array[typ T]{
		read uint length;
		shadow pointer[T] memory;
		ghost T elem[uint]{
			T get(uint index){
				if(index < this.length){
					pointer[T] val=somma[T](this.memory, index);
					return val.el;
				}
				else{
					throw outOfBounds;
				}
			}
			void set(T va, uint index){
				if(index < this.length){
					pointer[T] val=somma[T](this.memory, index);
					val.el=va;
				}
			}
		};
		end(){

		};
	}
	costructor iniArray[typ T](Array[T] this, uint size, pointer[T] data){
		this.memory = data;
		this.length = size;
	}
	type DynArray[typ T] extends Array[T]{
		end(){
			free((pt)this.memory);
		};
	}
	costructor iniDynArray[typ T](DynArray[T] this, uint size){
		:super iniArray[T](size, (pointer[T])allocate((uint)#SIZEOF(T)*size));
	}
	type explicit StaticArray[typ T, num L 2]{
		T elem packed L;
	}
	costructor iniStaticArray[typ T, num L 2](StaticArray[T, L] this){

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
		Array[T] ar = :new iniDynArray[T](L);
		for(uint i=u0; i<L; i =+ u1)
			ar.elem[i]=sa.elem[i];
		return ar;
	}
	pt dataPointer[typ T](Array[T] a){
		return (pt)a.memory;
	}
}
