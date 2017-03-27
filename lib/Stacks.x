modulo Stacks depends Memory StdIO{
	type explicit StackEle[typ Y]{
		init(Y e, StackEle[Y] b){
			this.ele=e;
			this.next=b;
		};
		shadow Y ele;
		shadow StackEle[Y] next;
	}
	type Stack[typ o]{
		explicit read uint dim;
		shadow StackEle[o] valv;
		init(){
			this.dim = u0;
			this.valv = null;
		};
		end(){
			StackEle[o] cur = this.valv;
			StackEle[o] next;
			while(cur != null){
				next = cur.next;
				:destroy cur;
				cur = next;
			}
		};
	}
	void push[typ T](Stack[T] st, T element){
		StackEle[T] g = :new StackEle[T](element, st.valv);
		st.valv = g;
		st.dim =+ u1;
	}
	T pop[typ T](Stack[T] st)errors empty{
		if(st.dim > u0){
			st.dim =- u1;
			StackEle[T] g = st.valv;
			st.valv = g.next;
			T ret = g.ele;
			:destroy g;
			return ret;
		}
		throw empty;
	}
}
